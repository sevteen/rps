package com.example.rps;

/**
 * Represents handle of async game typically created by {@link Game#doRoundsAsync(RoundResultListener)}
 *
 * @author Beka Tsotsoria
 */
public interface AsyncPlay {

    /**
     * Stops game
     */
    void stop();
}
