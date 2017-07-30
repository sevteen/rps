package com.example.rps;

import java.util.List;

/**
 * Represents game session started by {@link RpsClient#joinGame(String, String)}
 *
 * @author Beka Tsotsoria
 */
public interface GameSession {

    /**
     * Returns list of moves available in this game. Only
     * values returned here are allowed to be used as valid move
     */
    List<String> getAvailableMoves();

    /**
     * Registers listener to list of players in this game
     *
     */
    GameSession onPlayersChange(PlayersChangeListener listener);

    /**
     * Registers listener to round's result. {@link RoundResultListener#onResult(RoundResult)}
     * will be called when round is completed and winner is known
     */
    GameSession onRoundResult(RoundResultListener listener);

    /**
     * Submits move
     *
     * @throws IllegalArgumentException if move is not {@link #getAvailableMoves() available}
     */
    void makeMove(String move);

    /**
     * Leaves game. {@link RoundResultListener#onResult(RoundResult)} will not be invoked anymore
     */
    void leave();
}
