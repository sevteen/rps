package com.example.rps;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Beka Tsotsoria
 */
public class WebSocketGameSession implements GameSession {

    private String gameName;
    private String playerId;
    private StompSession session;

    private List<String> availableMoves;

    public WebSocketGameSession(String gameName, String playerId, StompSession session, WebSocketRpsClient client) {
        this.gameName = gameName;
        this.playerId = playerId;
        this.session = session;
        fetchAvailableMoves(gameName, session, client);
    }

    private void fetchAvailableMoves(String gameName, StompSession session, WebSocketRpsClient client) {
        SettableListenableFuture<List<String>> moves = new SettableListenableFuture<>();
        session.subscribe(String.format("/game/%s/moves", gameName), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                moves.set((List<String>) payload);
            }
        });
        availableMoves = client.waitForResponse(moves, "Failed to retrieve list of available moves");
    }

    @Override
    public List<String> getAvailableMoves() {
        return availableMoves;
    }

    @Override
    public GameSession onPlayersChange(PlayersChangeListener listener) {
        session.subscribe(String.format("/topic/game/%s/players", gameName), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders headers, Object payload) {
                listener.onPlayersChange((List<String>) payload);
            }
        });
        return this;
    }

    @Override
    public GameSession onRoundResult(RoundResultListener listener) {
        session.subscribe(String.format("/topic/game/%s/result", gameName), new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return RoundResult.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                listener.onResult((RoundResult) payload);
            }
        });
        return this;
    }

    @Override
    public void makeMove(String move) {
        if (!isMoveAvailable(move)) {
            throw new IllegalArgumentException("Move \"" + move + "\" is not available");
        }
        session.send(String.format("/game/%s/move/%s", gameName, playerId), move);
    }

    private boolean isMoveAvailable(String move) {
        return availableMoves.contains(move);
    }

    @Override
    public void leave() {
        session.send(String.format("/game/%s/leave", gameName), playerId);
    }
}
