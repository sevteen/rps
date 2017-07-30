package com.example.rps;

/**
 * Represents single round result of player
 *
 * @author Beka Tsotsoria
 */
public class PlayerResult {

    private String playerId;
    private String move;
    private boolean winner;
    private int totalWins;

    public PlayerResult(String playerId, String move, boolean winner, int totalWins) {
        this.playerId = playerId;
        this.move = move;
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
     * Returns move made by the player in this round
     */
    public String getMove() {
        return move;
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
            ", weapon=" + move +
            ", winner=" + winner +
            ", totalWins=" + totalWins +
            '}';
    }
}
