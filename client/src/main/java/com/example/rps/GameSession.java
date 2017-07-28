package com.example.rps;

import java.util.List;

/**
 * Represents game session started by {@link RpsClient#newGame(String)} or {@link RpsClient#joinGame(String, String)}
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
     * Registers listener to player's turn. {@link TurnListener#onTurn()}
     * will be called when it's time for a player to make a move.
     */
    void onTurn(TurnListener listener);

    /**
     * Leaves game. {@link TurnListener#onTurn()} will not be invoked anymore
     */
    void leave();
}
