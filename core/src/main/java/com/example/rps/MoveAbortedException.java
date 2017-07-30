package com.example.rps;

/**
 * Thrown when {@link Player#makeMove(GameContext) players' move} gets {@link AbortablePlayer#abort() aborted}
 *
 * @author Beka Tsotsoria
 */
public class MoveAbortedException extends RuntimeException {

    public MoveAbortedException(String message) {
        super(message);
    }
}
