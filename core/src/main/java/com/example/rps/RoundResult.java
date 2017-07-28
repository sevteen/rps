package com.example.rps;

/**
 * Represents result of {@link Game#doRound() round}
 *
 * @author Beka Tsotsoria
 */
public class RoundResult {

    private Player winner;
    private Weapon weaponUsed;

    public RoundResult(Player winner, Weapon weaponUsed) {
        this.winner = winner;
        this.weaponUsed = weaponUsed;
    }

    public Player getWinner() {
        return winner;
    }

    public Weapon getWeaponUsed() {
        return weaponUsed;
    }
}
