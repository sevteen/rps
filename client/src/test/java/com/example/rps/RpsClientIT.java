package com.example.rps;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Beka Tsotsoria
 */
public class RpsClientIT {

    private static final int SERVER_PORT = 2727;

    private RpsClient client;

    private ConfigurableApplicationContext server;

    @Before
    public void setUp() throws Exception {
        server = Server.start(new String[]{"--server.port=" + SERVER_PORT});
        client = new WebSocketRpsClient();
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void canConnect() throws Exception {
        connect();
        // pass
    }

    @Test
    public void canCreateNewGame() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        List<String> games = client.getCurrentGames();

        assertThat(games).isEqualTo(Arrays.asList("game"));
    }

    @Test
    public void canReceiveAvailableMoves() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        List<String> availableMoves = client.joinGame("game", "player").getAvailableMoves();

        assertThat(availableMoves).contains("rock", "paper", "scissors");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReceiveListOfPlayersWhenAnotherJoins() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        List<String> players = new ArrayList<>();
        PlayersChangeListener l = mock(PlayersChangeListener.class);
        doAnswer(invocation -> players.addAll((List<String>) invocation.getArguments()[0])).when(l).onPlayersChange(any());

        client.joinGame("game", "player").onPlayersChange(l);
        client.joinGame("game", "player2");

        Thread.sleep(50);

        assertThat(players).contains("player", "player2");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReceiveListOfPlayersWhenBotJoins() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        List<String> players = new ArrayList<>();
        PlayersChangeListener l = mock(PlayersChangeListener.class);
        doAnswer(invocation -> players.addAll((List<String>) invocation.getArguments()[0])).when(l).onPlayersChange(any());

        client.joinGame("game", "player").onPlayersChange(l);
        client.joinBot("game");

        Thread.sleep(50);

        assertThat(players).hasSize(2);
        assertThat(players).contains("player");
    }

    @Test(timeout = 5000)
    public void twoPlayersCanPlay() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        GameSession session1 = client.joinGame("game", "player1");
        GameSession session2 = client.joinGame("game", "player2");

        CompletableFuture<RoundResult> rr1f = new CompletableFuture<>();
        CompletableFuture<RoundResult> rr2f = new CompletableFuture<>();

        session1.onRoundResult(rr1f::complete);
        session2.onRoundResult(rr2f::complete);

        session1.makeMove("rock");
        session2.makeMove("scissors");

        RoundResult rr1 = rr1f.get();
        RoundResult rr2 = rr2f.get();

        assertThat(rr1.getRoundNumber()).isEqualTo(1);
        assertThat(rr1.getWinnerIds()).isEqualTo(Arrays.asList("player1"));

        assertThat(rr2.getRoundNumber()).isEqualTo(1);
        assertThat(rr2.getWinnerIds()).isEqualTo(Arrays.asList("player1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void playerShouldBeNotifiedWhenAnotherPlayerLeavesGame() throws Exception {
        connect();

        client.newGame("game");

        Thread.sleep(50);

        List<String> players = new ArrayList<>();
        PlayersChangeListener l = mock(PlayersChangeListener.class);
        doAnswer(invocation -> players.addAll((List<String>) invocation.getArguments()[0])).when(l).onPlayersChange(any());

        GameSession session1 = client.joinGame("game", "player");
        GameSession session2 = client.joinGame("game", "player2");

        session1.onPlayersChange(l);

        session2.leave();

        Thread.sleep(50);

        assertThat(players).contains("player");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void playerShouldBeNotifiedWhenAnotherPlayerDisconnectsFromTheServer() throws Exception {
        RpsClient client1 = new WebSocketRpsClient();
        RpsClient client2 = new WebSocketRpsClient();

        connect(client1);
        connect(client2);

        client1.newGame("game");

        Thread.sleep(50);

        List<String> players = new ArrayList<>();
        PlayersChangeListener l = mock(PlayersChangeListener.class);
        doAnswer(invocation -> players.addAll((List<String>) invocation.getArguments()[0])).when(l).onPlayersChange(any());

        GameSession session1 = client1.joinGame("game", "player");
        client2.joinGame("game", "player2");

        session1.onPlayersChange(l);

        Thread.sleep(50);

        client2.disconnect();

        Thread.sleep(50);

        assertThat(players).contains("player");
    }

    private void connect() {
        connect(client);
    }

    private void connect(RpsClient client) {
        client.connect("localhost", SERVER_PORT);
    }
}
