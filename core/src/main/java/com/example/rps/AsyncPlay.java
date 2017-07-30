package com.example.rps;

/**
 * Represents handle of async game typically created by {@link Game#doRoundsAsync(RoundResultListener)}
 *
 * @author Beka Tsotsoria
 */
public interface AsyncPlay {

    /**
     * Tests whether or not play is active
     */
    boolean isPlaying();

    /**
     * Stops game
     */
    void stop();
}
