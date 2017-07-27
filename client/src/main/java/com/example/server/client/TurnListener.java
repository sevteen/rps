package com.example.server.client;

/**
 * This is where the player submits moves to game
 *
 * @author Beka Tsotsoria
 */
public interface TurnListener {

    /**
     * Invoked when it's time for a player to make a move.
     * <p>
     * <strong>NOTE:</strong> this method will be called indefinitely until, one of the
     * {@link GameSession#getAvailableMoves() available move} is returned
     *
     * @return move to be submitted
     */
    String onTurn();
}
