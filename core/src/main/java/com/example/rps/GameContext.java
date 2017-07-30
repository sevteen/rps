package com.example.rps;

import java.util.List;

/**
 * @author Beka Tsotsoria
 */
public class GameContext {

    private List<Weapon> availableMoves;

    public GameContext(List<Weapon> availableMoves) {
        this.availableMoves = availableMoves;
    }

    public List<Weapon> getAvailableMoves() {
        return availableMoves;
    }
}
