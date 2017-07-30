package com.example.rps;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Beka Tsotsoria
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ServerIT {

    @Value("${local.server.port}")
    private int port;

    private String url;

    private WebSocketStompClient stompClient;

    @Before
    public void setUp() throws Exception {
        url = "ws://localhost:" + port + "/ws";
        stompClient = new WebSocketStompClient(new SockJsClient(
            Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    public void shouldReceiveListOfGamesWhenSubscribing() throws Exception {
        StompSession session1 = startSession();
        createGame(session1, "theGame");

        StompSession session2 = startSession();

        SetHandler gamesHandler = new SetHandler();
        session2.subscribe("/game/available", gamesHandler);

        assertThat(gamesHandler.getSet()).isEqualTo(new HashSet<>(Arrays.asList("theGame")));
    }

    @Test
    public void creatingNewGameShouldChangeListOfGames() throws Exception {
        StompSession session = startSession();

        SetHandler gamesHandler = new SetHandler();
        session.subscribe("/topic/games", gamesHandler);

        createGame(session, "theGame");
        assertThat(gamesHandler.getSet()).isEqualTo(new HashSet<>(Arrays.asList("theGame")));

        createGame(session, "anotherGame");
        assertThat(gamesHandler.getSet()).isEqualTo(new HashSet<>(Arrays.asList("theGame", "anotherGame")));
    }

    @Test
    public void creating2GamesWithSameNameShouldNotChangeListOfGames() throws Exception {
        StompSession session = startSession();

        SetHandler gamesHandler = new SetHandler();
        session.subscribe("/topic/games", gamesHandler);

        createGame(session, "theGame");
        assertThat(gamesHandler.getSet()).isEqualTo(new HashSet<>(Arrays.asList("theGame")));

        createGame(session, "theGame");
        assertThat(gamesHandler.getSet()).isEqualTo(new HashSet<>(Arrays.asList("theGame")));
    }

    @Test
    public void canJoinTheGame() throws Exception {
        StompSession session = startSession();

        createGame(session, "theGame");

        ListHandler playersHandler = new ListHandler();

        session.subscribe("/topic/game/theGame/players", playersHandler);
        session.send("/game/theGame/join", "thePlayer");

        List<String> players = playersHandler.getPlayers();
        assertThat(players).hasSize(1);

        assertThat(players.get(0)).isEqualTo("thePlayer");
    }

    @Test
    public void firstPlayerShouldBeNotifiedWhenAnotherPlayerJoinsTheGame() throws Exception {
        StompSession session1 = startSession();
        createGame(session1, "theGame");

        StompSession session2 = startSession();

        ListHandler playersHandler = new ListHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        Thread.sleep(50);

        List<String> players = playersHandler.getPlayers();
        assertThat(players).hasSize(2);

        assertThat(players.get(0)).isEqualTo("thePlayer1");

        assertThat(players.get(1)).isEqualTo("thePlayer2");
    }

    @Test
    public void playerShouldBeNotifiedWhenAnotherPlayerLeavesTheGame() throws Exception {
        StompSession session1 = startSession();
        createGame(session1, "theGame");

        StompSession session2 = startSession();

        ListHandler playersHandler = new ListHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        // Wait a little bit to make sure "leave" message is sent after "join".
        // This is due to the fact that messages are processed concurrently by spring
        Thread.sleep(100);

        session2.send("/game/theGame/leave", "thePlayer2");

        // Ensure all the messages are received by handler
        Thread.sleep(100);

        List<List<String>> allPlayers = playersHandler.getAll();

        // List of players updated 3 times:
        // 1. when thePlayer1 joins
        // 2. when thePlayer2 joins
        // 3. when thePlayer2 leaves
        assertThat(allPlayers).hasSize(3);

        List<String> players = allPlayers.get(2);

        assertThat(players.get(0)).isEqualTo("thePlayer1");
    }

    @Test
    public void playerShouldLeaveGameAutomaticallyWhenWebSocketIsDisconnected() throws Exception {
        StompSession session1 = startSession();
        createGame(session1, "theGame");

        StompSession session2 = startSession();

        ListHandler playersHandler = new ListHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        Thread.sleep(100);

        session2.disconnect();

        // Ensure all the messages are received by handler
        Thread.sleep(100);

        List<List<String>> allPlayers = playersHandler.getAll();

        // List of players updated 3 times:
        // 1. when thePlayer1 joins
        // 2. when thePlayer2 joins
        // 3. when thePlayer2's session closes
        assertThat(allPlayers).hasSize(3);

        List<String> players = allPlayers.get(2);

        assertThat(players.get(0)).isEqualTo("thePlayer1");
    }

    @Test
    public void shouldReceiveListOfAvailableMovesWhenSubscribed() throws Exception {
        StompSession session = startSession();
        createGame(session, "theGame");

        SetHandler movesHandler = new SetHandler();
        session.subscribe("/game/theGame/moves", movesHandler);

        assertThat(movesHandler.getSet()).contains("rock", "paper", "scissors");
    }

    @Test
    public void canPerformSingleRound() throws Exception {
        StompSession session1 = startSession();
        createGame(session1, "theGame");

        StompSession session2 = startSession();

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        Thread.sleep(50);

        RoundHandler roundHandler = new RoundHandler();
        session1.subscribe("/topic/game/theGame/result", roundHandler);

        session1.send("/game/theGame/move/thePlayer1", "paper");

        session2.send("/game/theGame/move/thePlayer2", "rock");

        RoundResult result = roundHandler.getResult();
        assertThat(result.getWinnerIds().get(0)).isEqualTo("thePlayer1");
        assertThat(result.getWeaponUsed("thePlayer1")).isEqualTo("paper");
    }

    private void createGame(StompSession session, String name) throws InterruptedException {
        session.send("/game/create", name);
        Thread.sleep(50);
    }

    private StompSession startSession() throws InterruptedException, ExecutionException, TimeoutException {
        return stompClient.connect(url,
            new StompSessionHandlerAdapter() {
            }).get(1, SECONDS);
    }

    private class SetHandler implements StompFrameHandler {

        private volatile CompletableFuture<Set<String>> future = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Set.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public synchronized void handleFrame(StompHeaders headers, Object payload) {
            future.complete((Set<String>) payload);
        }

        public Set<String> getSet() throws InterruptedException, ExecutionException, TimeoutException {
            Set<String> games = future.get(3, SECONDS);
            synchronized (this) {
                future = new CompletableFuture<>();
            }
            return games;
        }
    }

    private class ListHandler implements StompFrameHandler {

        private CompletableFuture<List<String>> future = new CompletableFuture<>();
        private List<List<String>> all = new ArrayList<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return new ParameterizedTypeReference<List<String>>() {
            }.getType();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders headers, Object payload) {
            future.complete((List<String>) payload);
            all.add((List<String>) payload);
        }

        public List<String> getPlayers() throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(3, SECONDS);
        }

        public List<List<String>> getAll() {
            return all;
        }
    }

    private class RoundHandler implements StompFrameHandler {

        private CompletableFuture<RoundResult> future = new CompletableFuture<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return RoundResult.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            future.complete((RoundResult) payload);
        }

        public RoundResult getResult() throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(3, SECONDS);
        }
    }
}
