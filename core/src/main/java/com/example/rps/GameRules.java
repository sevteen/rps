package com.example.rps;

import java.util.*;

/**
 * Represents rules of games, users can play games based on rules they define
 * using this class. Rules need to be consistent though, which means following:
 * <ul>
 * <li>there should not be direct circular relationship between weapons, for example: if ROCK defeats SCISSORS,
 * there should not be the rule which indicates that SCISSORS defeats ROCK</li>
 * <li>every weapon should be defeated by at least one another weapon</li>
 * </ul>
 *
 * @author Beka Tsotsoria
 * TODO: add more consistency checks
 */
public class GameRules {

    /**
     * Represents rules for classic Rock-Paper-Scissors game
     */
    public static final GameRules CLASSIC = builder()
        .defeats(Weapon.PAPER, Weapon.ROCK)
        .defeats(Weapon.SCISSORS, Weapon.PAPER)
        .defeats(Weapon.ROCK, Weapon.SCISSORS)
        .build();

    /**
     * Represents rules for Lizard-Spock extension
     */
    public static final GameRules LIZARD_SPOCK_EXTENSION = builder()
        .defeats(Weapon.PAPER, Weapon.ROCK, Weapon.SPOCK)
        .defeats(Weapon.SCISSORS, Weapon.PAPER, Weapon.LIZARD)
        .defeats(Weapon.ROCK, Weapon.SCISSORS, Weapon.LIZARD)
        .defeats(Weapon.SPOCK, Weapon.SCISSORS, Weapon.ROCK)
        .defeats(Weapon.LIZARD, Weapon.PAPER, Weapon.SPOCK)
        .build();

    private final Map<Weapon, List<Weapon>> relationships;

    private GameRules(Builder b) {
        this.relationships = b.relationships;
        relationships.forEach((weapon, defeatees) -> {
            Weapon circularDefeater = defeatees.stream().filter(defeatee -> defeats(defeatee, weapon))
                .findFirst()
                .orElse(null);
            if (circularDefeater != null) {
                throw new InconsistentGameRulesException(weapon.getName() + " and " + circularDefeater.getName() + " defeat each other");
            }
            if (!isDefeatableByAnyone(weapon)) {
                throw new InconsistentGameRulesException(weapon.getName() + " is not defeatable");
            }
        });
    }

    private boolean isDefeatableByAnyone(Weapon weapon) {
        return relationships.entrySet().stream()
            .filter(e -> !e.getKey().equals(weapon))
            .map(Map.Entry::getValue)
            .anyMatch(otherDefeatees -> otherDefeatees.contains(weapon));
    }

    public boolean defeats(Weapon weapon, Weapon anotherWeapon) {
        return relationships.get(weapon).contains(anotherWeapon);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Weapon, List<Weapon>> relationships = new HashMap<>();

        /**
         * Adds rule which means that <code>weapon</code> will defeat <code>anotherWeapons</code>
         *
         * @return this instance to allow chaining
         */
        public Builder defeats(Weapon weapon, Weapon... anotherWeapons) {
            relationships.computeIfAbsent(weapon, w -> new ArrayList<>()).addAll(Arrays.asList(anotherWeapons));
            return this;
        }

        /**
         * Builds rules
         *
         * @throws InconsistentGameRulesException when rules are not consistent
         */
        public GameRules build() {
            return new GameRules(this);
        }
    }

}
