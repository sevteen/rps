package com.example.rps;

import java.util.Objects;

/**
 * @author Beka Tsotsoria
 */
public class Weapon {

    public static final Weapon ROCK = from("rock");
    public static final Weapon PAPER = from("paper");
    public static final Weapon SCISSORS = from("scissors");
    public static final Weapon LIZARD = from("lizard");
    public static final Weapon SPOCK = from("spock");

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

    public static Weapon from(String weaponName) {
        return new Weapon(weaponName.toLowerCase());
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
