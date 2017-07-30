package com.example.rps;

/**
 * @author Beka Tsotsoria
 */
public interface RoundResultListener {

    /**
     * Will be invoked by result of round is available
     */
    void onResult(RoundResult result);
}
