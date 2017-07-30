package com.example.rps;

import java.util.List;

/**
 * @author Beka Tsotsoria
 */
public interface PlayersChangeListener {

    /**
     * Invoked when list of players changed
     */
    void onPlayersChange(List<String> players);
}
