package com.example.rps.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Beka Tsotsoria
 */
public class PlayerDto {

    private String game;
    private String id;

    @JsonCreator
    public PlayerDto(@JsonProperty("name") String game, @JsonProperty("id") String id) {
        this.game = game;
        this.id = id;
    }

    public String getGame() {
        return game;
    }

    public String getId() {
        return id;
    }
}
