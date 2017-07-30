package com.example.rps;

/**
 * Thrown when game rules are not consistent
 *
 * @author Beka Tsotsoria
 */
public class InconsistentGameRulesException extends RuntimeException {

    public InconsistentGameRulesException(String message) {
        super(message);
    }
}
