package com.example.rps;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Beka Tsotsoria
 */
public class WebSocketRpsClient implements RpsClient {

    private final WebSocketStompClient client;

    private StompSession session;

    private int responseTimeout;

    public WebSocketRpsClient() {
        this(3);
    }

    public WebSocketRpsClient(int responseTimeout) {
        this.responseTimeout = responseTimeout;
        client = new WebSocketStompClient(new SockJsClient(
            Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))));
        client.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Override
    public void connect(String host, int port) {
        String endpoint = "ws://" + host + ":" + port + "/ws";
        session = waitForResponse(client.connect(endpoint, new StompSessionHandlerAdapter() {
        }), "Failed to connect to server on endpoint:" + endpoint);
    }

    @Override
    public List<String> getCurrentGames() {
        SettableListenableFuture<List<String>> games = new SettableListenableFuture<>();
        session.subscribe("/game/available", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new ParameterizedTypeReference<List<String>>() {
                }.getType();
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                games.set((List<String>) payload);
            }
        });
        return waitForResponse(games, "Failed to retrieve list of current games");
    }

    @Override
    public void newGame(String name) {
        session.send("/game/create", name);
    }

    @Override
    public GameSession joinGame(String gameName, String playerId) {
        // TODO: handle the case when game does not exist
        session.send(String.format("/game/%s/join", gameName), playerId);
        return new WebSocketGameSession(gameName, playerId, session, this);
    }

    @Override
    public void disconnect() {
        session.disconnect();
    }

    <T> T waitForResponse(Future<T> f, String errMsg) {
        try {
            return f.get(responseTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(errMsg, e);
        }
    }
}
