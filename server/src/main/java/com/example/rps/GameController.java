package com.example.rps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Beka Tsotsoria
 */
@Controller
public class GameController {

    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, QueuedPlayer> players = new ConcurrentHashMap<>();
    private final Map<String, PlayerOfGame> sessions = new ConcurrentHashMap<>();

    @Autowired
    private SimpMessagingTemplate simp;

    @MessageMapping("/create")
    @SendTo("/topic/games")
    public Set<String> createGame(String game) {
        if (games.putIfAbsent(game, new Game(game)) == null) {
            log.info("Created game {}", game);
        } else {
            log.info("Game with name {} already exists", game);
        }
        return games.keySet();
    }

    @MessageMapping("/{name}/join")
    public void joinGame(@DestinationVariable String name, String playerId, SimpMessageHeaderAccessor headerAccessor) {
        sessions.put(headerAccessor.getSessionId(), new PlayerOfGame(name, playerId));
        Game game = getGame(name);
        joinGame(game, new QueuedPlayer(playerId));
        log.info("Player {} joined game {}", playerId, name);
    }

    @MessageMapping("/{name}/leave")
    public void leaveGame(@DestinationVariable String name, String playerId) {
        Game game = getGame(name);
        leaveGame(game, playerId);
        log.info("Player {} left game {}", playerId, name);
    }

    @MessageMapping("/{name}/move/{playerId}")
    public void move(@DestinationVariable String name, @DestinationVariable String playerId, String move) {
        players.get(keyFor(name, playerId)).addMove(Weapon.from(move));
        log.info("Player {} made move {} in game {}", playerId, move, name);
    }

    @SubscribeMapping("/{name}/moves")
    public Set<String> getAvailableMoves(@DestinationVariable String name) {
        return getGame(name).getAvailableWeapons().stream()
            .map(Weapon::getName)
            .collect(Collectors.toSet());
    }

    @SubscribeMapping("/available")
    public Set<String> getAvailableGames() {
        return games.keySet();
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent e) {
        PlayerOfGame player = sessions.get(e.getSessionId());
        if (player != null) {
            Game game = games.get(player.getGame());
            if (game != null) {
                leaveGame(game, player.getPlayer());
                sessions.remove(e.getSessionId());
            }
        }
    }

    private void joinGame(Game game, QueuedPlayer player) {
        game.join(player);
        players.put(keyFor(game.getName(), player.getId()), player);
        simp.convertAndSend("/topic/game/" + game.getName() + "/players", getPlayersOfGame(game));
        if (game.isReady()) {
            log.info("Game {} is ready", game.getName());
            sendTurn(game.getName(), game.getPlayerIds().get(0));
            game.doRoundsAsync(rr -> {
                log.info("Round completed {}", rr);
                simp.convertAndSend("/topic/game/" + game.getName() + "/result", rr);
            });
        }
    }

    private void sendTurn(String gameName, String playerId) {
        simp.convertAndSend("/topic/game/" + gameName + "/players/" + playerId + "/turn", "");
    }

    private void leaveGame(Game game, String playerId) {
        game.leave(playerId);
        players.remove(keyFor(game.getName(), playerId));
        simp.convertAndSend("/topic/game/" + game.getName() + "/players", getPlayersOfGame(game));
    }

    private String keyFor(String gameName, String playerId) {
        return gameName + "_" + playerId;
    }

    private List<String> getPlayersOfGame(Game game) {
        return game.getPlayerIds().stream()
            .collect(Collectors.toList());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private Game getGame(String name) {
        Game game = games.get(name);
        if (game == null) {
            throw new IllegalStateException("Game with name " + name + " does not exist");
        }
        return game;
    }

    static class PlayerOfGame {

        private String game;
        private String player;

        public PlayerOfGame(String game, String player) {
            this.game = game;
            this.player = player;
        }

        public String getGame() {
            return game;
        }

        public String getPlayer() {
            return player;
        }
    }
}
