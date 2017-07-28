package com.example.rps;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Beka Tsotsoria
 */
public class FakePlayer implements Player {

    private Weapon weapon;
    private String id;

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

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Weapon makeMove(GameContext context) {
        try {
            return weapon != null ? weapon : weapons.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
