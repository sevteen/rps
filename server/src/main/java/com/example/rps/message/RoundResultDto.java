package com.example.rps.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Beka Tsotsoria
 */
public class RoundResultDto {

    private String winner;
    private String weaponUsed;

    @JsonCreator
    public RoundResultDto(@JsonProperty("winner") String winner, @JsonProperty("weaponUsed") String weaponUsed) {
        this.winner = winner;
        this.weaponUsed = weaponUsed;
    }

    public String getWinner() {
        return winner;
    }

    public String getWeaponUsed() {
        return weaponUsed;
    }
}
