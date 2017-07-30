package com.example.rps;

/**
 * Thrown when player attempts to join to non-existing game
 *
 * @author Beka Tsotsoria
 */
public class GameDoesNotExistException extends RuntimeException {

    public GameDoesNotExistException(String message) {
        super(message);
    }
}
