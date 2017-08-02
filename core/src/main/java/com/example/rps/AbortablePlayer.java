package com.example.rps;

/**
 * @author Beka Tsotsoria
 */
public interface AbortablePlayer extends Player {

    /**
     * {@inheritDoc}
     *
     * @throws MoveAbortedException when this player is {@link #abort() aborted}
     */
    @Override
    Weapon makeMove(GameContext context) throws MoveAbortedException;

    /**
     * Releases threads blocked by {@link #makeMove(GameContext)} by receiving
     * {@link MoveAbortedException}
     */
    void abort();
}
