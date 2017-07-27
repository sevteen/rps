package com.example.rps;

import com.example.rps.message.PlayerDto;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Beka Tsotsoria
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AppTest {

    @Autowired
    private MockMvc mvc;

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
    public void indexShouldHaveEmptyListOfGames() throws Exception {
        mvc.perform(get("/"))
            .andDo(print())
            .andExpect(model().attribute("games", Matchers.hasSize(0)));
    }

    @Test
    public void requestingNonExistingGameShouldResultInNotFoundError() throws Exception {
        mvc.perform(get("/game/1"))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void canCreateNewGame() throws Exception {
        String location = mvc.perform(post("/game").param("name", "theGame"))
            .andExpect(status().isFound())
            .andExpect(redirectedUrlPattern("/game/*"))
            .andDo(print())
            .andReturn()
            .getResponse().getHeader("Location");

        mvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(model().attribute("game", "theGame"));
    }

    @Test
    public void creating2GamesWithSameNameShouldFailWithBadRequest() throws Exception {
        mvc.perform(post("/game").param("name", "theGame"))
            .andExpect(status().isFound());

        mvc.perform(post("/game").content("theGame"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void canJoinTheGame() throws Exception {
        createGame("theGame");

        StompSession session = startSession();

        PlayersHandler playersHandler = new PlayersHandler();

        session.subscribe("/topic/game/theGame/players", playersHandler);
        session.send("/game/theGame/join", "thePlayer");

        List<PlayerDto> players = playersHandler.getPlayers();
        assertThat(players).hasSize(1);
        PlayerDto player = players.get(0);
        assertThat(player).isNotNull();
        assertThat(player.getGame()).isEqualTo("theGame");
        assertThat(player.getId()).isEqualTo("thePlayer");
    }

    @Test
    public void firstPlayerShouldBeNotifiedWhenAnotherPlayerJoinsTheGame() throws Exception {
        createGame("theGame");

        StompSession session1 = startSession();
        StompSession session2 = startSession();

        PlayersHandler playersHandler = new PlayersHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        List<PlayerDto> players = playersHandler.getPlayers();
        assertThat(players).hasSize(2);

        PlayerDto player1 = players.get(0);
        assertThat(player1).isNotNull();
        assertThat(player1.getGame()).isEqualTo("theGame");
        assertThat(player1.getId()).isEqualTo("thePlayer1");

        PlayerDto player2 = players.get(1);
        assertThat(player2).isNotNull();
        assertThat(player2.getGame()).isEqualTo("theGame");
        assertThat(player2.getId()).isEqualTo("thePlayer2");
    }

    @Test
    public void playerShouldBeNotifiedWhenAnotherPlayerLeavesTheGame() throws Exception {
        createGame("theGame");

        StompSession session1 = startSession();
        StompSession session2 = startSession();

        PlayersHandler playersHandler = new PlayersHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        // Wait a little bit to make sure "leave" message is sent after "join".
        // This is due to the fact that messages are processed concurrently by spring
        Thread.sleep(100);

        session2.send("/game/theGame/leave", "thePlayer2");

        // Ensure all the messages are received by handler
        Thread.sleep(100);

        List<List<PlayerDto>> allPlayers = playersHandler.getAllPlayers();

        // List of players updated 3 times:
        // 1. when thePlayer1 joins
        // 2. when thePlayer2 joins
        // 3. when thePlayer2 leaves
        assertThat(allPlayers).hasSize(3);

        List<PlayerDto> players = allPlayers.get(2);

        PlayerDto player = players.get(0);
        assertThat(player).isNotNull();
        assertThat(player.getGame()).isEqualTo("theGame");
        assertThat(player.getId()).isEqualTo("thePlayer1");
    }

    @Test
    public void playerShouldLeaveGameAutomaticallyWhenWebSocketIsDisconnected() throws Exception {
        createGame("theGame");

        StompSession session1 = startSession();
        StompSession session2 = startSession();

        PlayersHandler playersHandler = new PlayersHandler();

        session1.subscribe("/topic/game/theGame/players", playersHandler);

        session1.send("/game/theGame/join", "thePlayer1");
        session2.send("/game/theGame/join", "thePlayer2");

        Thread.sleep(100);

        session2.disconnect();

        // Ensure all the messages are received by handler
        Thread.sleep(100);

        List<List<PlayerDto>> allPlayers = playersHandler.getAllPlayers();

        // List of players updated 3 times:
        // 1. when thePlayer1 joins
        // 2. when thePlayer2 joins
        // 3. when thePlayer2's session closes
        assertThat(allPlayers).hasSize(3);

        List<PlayerDto> players = allPlayers.get(2);

        PlayerDto player = players.get(0);
        assertThat(player).isNotNull();
        assertThat(player.getGame()).isEqualTo("theGame");
        assertThat(player.getId()).isEqualTo("thePlayer1");
    }

    private void createGame(String name) throws Exception {
        mvc.perform(post("/game").param("name", name));
    }

    private StompSession startSession() throws InterruptedException, ExecutionException, TimeoutException {
        return stompClient.connect(url,
            new StompSessionHandlerAdapter() {
            }).get(1, SECONDS);
    }

    private class PlayersHandler implements StompFrameHandler {

        private CompletableFuture<List<PlayerDto>> future = new CompletableFuture<>();
        private List<List<PlayerDto>> allPlayers = new ArrayList<>();

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return new ParameterizedTypeReference<List<PlayerDto>>() {
            }.getType();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders headers, Object payload) {
            List<PlayerDto> players = ((List<Map<String, String>>) payload)
                .stream()
                .map(hm -> new PlayerDto(hm.get("game"), hm.get("id")))
                .collect(Collectors.toList());
            future.complete(players);
            allPlayers.add(players);
        }

        public List<PlayerDto> getPlayers() throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(30, SECONDS);
        }

        public List<List<PlayerDto>> getAllPlayers() {
            return allPlayers;
        }
    }
}
