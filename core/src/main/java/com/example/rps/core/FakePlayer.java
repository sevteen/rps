package com.example.rps.core;

import java.util.UUID;

/**
 * @author Beka Tsotsoria
 */
public class FakePlayer implements Player {

    private Weapon weapon;
    private String id;

    public FakePlayer() {
        this(Weapon.PAPER, "id");
    }

    public FakePlayer(Weapon weapon, String id) {
        this.weapon = weapon;
        this.id = id;
    }

    public static FakePlayer using(Weapon weapon) {
        return new FakePlayer(weapon, UUID.randomUUID().toString());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Weapon makeMove(GameContext context) {
        return weapon;
    }
}
