package com.example.rps;

/**
 * Represents player SPI
 *
 * @author Beka Tsotsoria
 */
public interface Player {

    /**
     * Return unique ID of player
     */
    String getId();

    /**
     * Return desired weapon to be used in next round
     * @param context context of game
     */
    Weapon makeMove(GameContext context);
}
