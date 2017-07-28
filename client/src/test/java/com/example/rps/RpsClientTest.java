package com.example.rps;

import org.junit.Test;

/**
 * @author Beka Tsotsoria
 */
public class RpsClientTest {

    @Test
    public void name() throws Exception {
        RpsClient client = null;

        client.connect("localhost", 1010);
        GameSession session = client.newGame("gela");
        session.getAvailableMoves();
        session.onTurn(() -> "");
    }
}
