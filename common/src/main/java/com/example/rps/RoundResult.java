package com.example.rps;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents result of single game round
 *
 * @author Beka Tsotsoria
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoundResult {

    private List<PlayerResult> playerResults = new ArrayList<>();
    private int roundNumber;

    @JsonCreator
    public RoundResult(@JsonProperty("playerResults") List<PlayerResult> playerResults, @JsonProperty("roundNumber") int roundNumber) {
        this.playerResults = playerResults;
        this.roundNumber = roundNumber;
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

    public String getWeaponUsed(String playerId) {
        return streamFor(playerId)
            .map(PlayerResult::getMove)
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
        return "Round " + roundNumber + " " + (isDraw() ? "Draw" : playerResults.toString());
    }
}
