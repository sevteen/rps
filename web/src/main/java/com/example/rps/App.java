package com.example.rps;

import com.example.rps.core.Game;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Beka Tsotsoria
 */
@SpringBootApplication
@Controller
public class App {

    private ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/game", method = RequestMethod.POST)
    public String createGame() {
        String gameId = UUID.randomUUID().toString();
        games.put(gameId, new Game());
        return "redirect:/game/" + gameId;
    }

    @RequestMapping("/game/{id}")
    public String game(@PathVariable String id, ModelMap model) {
        Game game = games.get(id);
        if (game == null) {
            throw new NotFoundException();
        }
        model.addAttribute("gameId", id);
        return "game";
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
