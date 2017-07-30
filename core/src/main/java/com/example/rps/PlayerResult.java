package com.example.rps;

/**
 * Represents single round result of player
 *
 * @author Beka Tsotsoria
 */
public class PlayerResult {

    private String playerId;
    private Weapon weapon;
    private boolean winner;
    private int totalWins;

    public PlayerResult(String playerId, Weapon weapon, boolean winner, int totalWins) {
        this.playerId = playerId;
        this.weapon = weapon;
        this.winner = winner;
        this.totalWins = totalWins;
    }

    /**
     * Returns ID of player
     */
    public String getPlayerId() {
        return playerId;
    }

    /**
     * Returns weapon used by the player in this round
     */
    public Weapon getWeapon() {
        return weapon;
    }

    /**
     * Checks if {@link #getPlayerId() player} won this round
     */
    public boolean isWinner() {
        return winner;
    }

    /**
     * Returns total number of wins by {@link #getPlayerId() player}
     */
    public int getTotalWins() {
        return totalWins;
    }

    @Override
    public String toString() {
        return "PlayerResult{" +
            "playerId='" + playerId + '\'' +
            ", weapon=" + weapon +
            ", winner=" + winner +
            ", totalWins=" + totalWins +
            '}';
    }
}
