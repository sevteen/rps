package com.example.rps;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents result of {@link Game#doRound() round}
 *
 * @author Beka Tsotsoria
 */
public class RoundResult {

    private List<PlayerResult> playerResults = new ArrayList<>();
    private int roundNumber;

    public RoundResult(List<PlayerResult> playerResults, int roundNumber) {
        this.playerResults = playerResults;
        this.roundNumber = roundNumber;
    }

    /**
     * Creates new draw result
     */
    public static RoundResult draw(int round) {
        return new RoundResult(new ArrayList<>(), round);
    }

    public List<PlayerResult> getPlayerResults() {
        return playerResults;
    }

    public PlayerResult resultFor(String playerId) {
        return streamFor(playerId).findFirst().orElse(null);
    }

    private Stream<PlayerResult> streamFor(String playerId) {
        return playerResults.stream()
            .filter(pr -> pr.getPlayerId().equals(playerId));
    }

    public List<String> getWinnerIds() {
        return playerResults.stream()
            .filter(PlayerResult::isWinner)
            .map(PlayerResult::getPlayerId)
            .collect(Collectors.toList());
    }

    public Weapon getWeaponUsed(String playerId) {
        return streamFor(playerId)
            .map(PlayerResult::getWeapon)
            .findFirst()
            .orElse(null);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public boolean isDraw() {
        return playerResults.stream().noneMatch(PlayerResult::isWinner);
    }

    @Override
    public String toString() {
        return isDraw() ? "Draw" : playerResults.toString();
    }
}
