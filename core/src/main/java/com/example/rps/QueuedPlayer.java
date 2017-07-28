package com.example.rps;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Implementation which {@link #makeMove(GameContext) makes} moves based
 * on queue
 *
 * @author Beka Tsotsoria
 */
public class QueuedPlayer implements Player {

    private String id;
    private BlockingQueue<Weapon> weapons = new LinkedBlockingDeque<>();

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
            return weapons.take();
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

}
