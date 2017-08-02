package com.example.rps;

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
     * Start new game
     *
     * @param name name of the game, must be unique
     * @throws IllegalArgumentException if name is not unique
     */
    void newGame(String name);

    /**
     * Joins bot into game
     *
     * @param gameName unique name of game
     * @throws GameDoesNotExistException when game with specified name does not exist
     */
    void joinBot(String gameName);

    /**
     * Joins player into game and returns session
     *
     * @param gameName unique name of game
     * @param playerId unique ID of player
     * @throws IllegalArgumentException  if playerId is not unique in this game
     * @throws GameDoesNotExistException when game with specified name does not exist
     */
    GameSession joinGame(String gameName, String playerId);

    /**
     * Disconnects from RPS server
     */
    void disconnect();
}
