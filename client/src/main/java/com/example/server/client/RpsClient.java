package com.example.server.client;

import java.util.List;

/**
 * RPS client which interacts with RPS server
 *
 * @author Beka Tsotsoria
 */
public interface RpsClient {

    /**
     * Connects to RPS server
     */
    void connect(String host, int port);

    /**
     * Returns list of game names currently available on specific RPS server
     */
    List<String> getCurrentGames();

    /**
     * Start new game and returns session
     *
     * @param name name of the game, must be unique
     * @return game session
     * @throws IllegalArgumentException if name is not unique
     */
    GameSession newGame(String name);

    /**
     * Joins player into game
     *
     * @param gameName unique name of game
     * @param playerId unique ID of player
     * @throws IllegalArgumentException if playerId is not unique in this game
     */
    GameSession joinGame(String gameName, String playerId);

    /**
     * Disconnects from RPS server
     */
    void disconnect();
}
