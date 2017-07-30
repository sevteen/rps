package com.example.rps;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Beka Tsotsoria
 */
public class FakePlayer implements Player {

    private Weapon weapon;
    private String id;
    private int delay;

    private LinkedBlockingQueue<Weapon> weapons = new LinkedBlockingQueue<>();

    public FakePlayer() {
        this(Weapon.PAPER, "id");
    }

    public FakePlayer(Weapon weapon, String id) {
        this.weapon = weapon;
        this.id = id;
    }

    public FakePlayer(String id, Weapon... weapons) {
        this.id = id;
        Arrays.stream(weapons)
            .forEach(w -> this.weapons.offer(w));
    }

    public static FakePlayer using(String id, Weapon weapon) {
        return new FakePlayer(weapon, id);
    }

    public static FakePlayer inTurn(String id, Weapon... weapons) {
        return new FakePlayer(id, weapons);
    }

    /**
     * Delay between subsequent moves in milliseconds
     */
    public FakePlayer withDelay(int delay) {
        this.delay = delay;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Weapon makeMove(GameContext context) {
        try {
            if (delay != 0) {
                Thread.sleep(delay);
            }
            return weapon != null ? weapon : weapons.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
