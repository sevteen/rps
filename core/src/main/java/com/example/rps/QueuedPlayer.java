package com.example.rps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Implementation which {@link #makeMove(GameContext) makes} moves based
 * on queue
 *
 * @author Beka Tsotsoria
 */
public class QueuedPlayer implements AbortablePlayer {

    private String id;
    private BlockingQueue<Weapon> weapons = new LinkedBlockingDeque<>();
    private volatile boolean aborted;

    public QueuedPlayer(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Weapon makeMove(GameContext context) {
        try {
            Weapon move;
            do {
                move = weapons.poll(100, TimeUnit.MILLISECONDS);
            } while (move == null && !aborted);
            if (aborted) {
                throw new MoveAbortedException("Move of player " + id + " got aborted");
            }
            return move;
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for next move for player: " + id, e);
        }
    }

    /**
     * Adds move to queue
     */
    public void addMove(Weapon weapon) {
        weapons.offer(weapon);
    }

    @Override
    public void abort() {
        aborted = true;
    }
}
