package com.example.rps;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Beka Tsotsoria
 */
public class Cli {

    private String[] args;
    private PrintStream out;
    private Scanner in;

    private boolean running;

    public Cli(String[] args) {
        this.args = args;
        out = System.out;
        in = new Scanner(System.in);
    }

    public void run() throws Exception {
        if (args.length < 2) {
            printUsageAndExit();
        }
        running = true;

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        log("=====================================" +
            "\n" +
            "========  Welcome to RPS CLI ========" +
            "\n" +
            "=====================================");
        log("Type \"exit\" to exit from the game anytime program is waiting for your input");
        log("Press any key to start the game...");
        System.in.read();

        RpsClient client = new WebSocketRpsClient();

        log("Connecting to server");
        client.connect(host, port);

        log("Retrieving list of available games");
        List<String> games = client.getCurrentGames();

        String chosenGame;
        if (games.isEmpty()) {
            String choice = input("There are no available games at the moment, would you like to create it? [y/n]");
            if ("y".equalsIgnoreCase(choice)) {
                chosenGame = createGameFromInput(client);
            } else {
                log("Good bye then!");
                System.exit(0);
                return;
            }
        } else {
            int gameIndex;
            do {
                games.add("CREATE NEW GAME");
                printGames(games);
                gameIndex = inputInteger("Choose the game you want to join, by inputting corresponding number: ", games.size() + 1);
            } while (gameIndex > games.size() + 1);

            if (gameIndex == games.size()) {
                chosenGame = createGameFromInput(client);
            } else {
                chosenGame = games.get(gameIndex - 1);
            }
        }

        String playerId = input("Choose your nick name: ");

        log("Joining game: " + chosenGame + " as: " + playerId);
        GameSession session = client.joinGame(chosenGame, playerId);

        session.onPlayersChange(players -> log("Players in the game " + chosenGame + " now are: " + asPrintableList(players)));
        session.onRoundResult(rr -> {
            List<String> moves = rr.getPlayerResults().stream()
                .map(PlayerResult::getMove)
                .collect(Collectors.toList());

            if (rr.isDraw()) {
                log("Round " + rr.getRoundNumber() + " completed draw");
            }
            log("Round " + rr.getRoundNumber() + " completed, results are as follows: " +
                "Winner(s): " + asPrintableList(rr.getWinnerIds()) + "\n" +
                "Player moves: " + asPrintableList(moves));

            if ("y".equalsIgnoreCase(input("Again?"))) {
                makeMove(session);
            } else {
                log("Bye bye");
                System.exit(0);
            }
        });

        log("Following moves are available in this game: " + session.getAvailableMoves());
        makeMove(session);

        while (running) {
            Thread.sleep(100);
        }
    }

    private String createGameFromInput(RpsClient client) {
        String chosenGame;
        chosenGame = input("Please enter the name of game: ");
        client.newGame(chosenGame);
        log("Created game: " + chosenGame);
        return chosenGame;
    }

    private void printGames(List<String> games) {
        IntStream.range(0, games.size())
            .forEach(idx -> log((idx + 1) + ". " + games.get(idx)));
    }

    private void makeMove(GameSession session) {
        String move = input("Please make a move: ");
        session.makeMove(move);
        log("Waiting for opponent(s)' move");
    }

    private String asPrintableList(List<String> list) {
        return String.join(", ", list);
    }

    private void log(String msg) {
        out.println(msg);
    }

    private void printUsageAndExit() {
        out.println("Usage: <server_host> <server_ip>");
        System.exit(1);
    }

    private String input(String prompt) {
        log(prompt);
        String value = readLine();
        if ("exit".equalsIgnoreCase(value.trim())) {
            System.exit(0);
            return null;
        }
        return value;
    }

    private int inputInteger(String prompt, int maxValue) {
        Integer integerInput = null;
        do {
            try {
                integerInput = Integer.parseInt(input(prompt));
            } catch (NumberFormatException ignored) {
            }
        } while (integerInput == null || integerInput >= maxValue);
        return integerInput;
    }

    private String readLine() {
        return in.nextLine();
    }
}
