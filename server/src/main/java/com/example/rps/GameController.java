package com.example.rps;

import com.example.rps.message.PlayerDto;
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
    private final Map<String, PlayerDto> sessions = new ConcurrentHashMap<>();

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
        sessions.put(headerAccessor.getSessionId(), new PlayerDto(name, playerId));
        Game game = getGame(name);
        joinGame(game, new FakePlayer(Weapon.PAPER, playerId));
        log.info("Player {} joined game {}", playerId, name);
    }

    @MessageMapping("/{name}/leave")
    public void leaveGame(@DestinationVariable String name, String playerId) {
        Game game = getGame(name);
        leaveGame(game, playerId);
        log.info("Player {} left game {}", playerId, name);
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent e) {
        PlayerDto player = sessions.get(e.getSessionId());
        if (player != null) {
            Game game = games.get(player.getGame());
            if (game != null) {
                leaveGame(game, player.getId());
                sessions.remove(e.getSessionId());
            }
        }
    }

    private void joinGame(Game game, Player player) {
        game.join(player);
        simp.convertAndSend("/topic/game/" + game.getName() + "/players", getPlayersOfGame(game));
        if (game.isReady()) {
            simp.convertAndSend("/topic/game/" + game.getName() + "/players/" + game.getPlayerIds().get(0) + "/turn", "");
        }
    }

    private void leaveGame(Game game, String playerId) {
        game.leave(playerId);
        simp.convertAndSend("/topic/game/" + game.getName() + "/players", getPlayersOfGame(game));
    }

    private List<PlayerDto> getPlayersOfGame(Game game) {
        return game.getPlayerIds().stream()
            .map(pid -> new PlayerDto(game.getName(), pid))
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
}
