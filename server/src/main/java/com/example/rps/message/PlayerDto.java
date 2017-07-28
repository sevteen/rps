package com.example.rps.message;

/**
 * @author Beka Tsotsoria
 */
public class PlayerDto {

    private String game;
    private String id;

    public PlayerDto() {
    }

    public PlayerDto(String game, String id) {
        this.game = game;
        this.id = id;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGame() {
        return game;
    }

    public String getId() {
        return id;
    }
}
