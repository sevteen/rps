package com.example.server.core;

import java.util.Objects;

/**
 * @author Beka Tsotsoria
 */
public class Weapon {

    public static final Weapon ROCK = new Weapon("rock");
    public static final Weapon PAPER = new Weapon("paper");
    public static final Weapon SCISSORS = new Weapon("scissors");

    private String name;

    public Weapon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean is(Weapon weapon) {
        return equals(weapon);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weapon weapon = (Weapon) o;
        return Objects.equals(name, weapon.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
