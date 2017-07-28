package com.example.rps;

/**
 * @author Beka Tsotsoria
 */
public interface RoundResultListener {

    /**
     * Will be invoked by {@link Game#doRoundsAsync(RoundResultListener)} when
     * after every round
     */
    void onResult(RoundResult result);
}
