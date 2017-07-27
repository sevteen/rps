package com.example.server.client;

import java.util.List;

/**
 * @author Beka Tsotsoria
 */
public class DefaultRpsClient implements RpsClient {

    @Override
    public void connect(String host, int port) {

    }

    @Override
    public List<String> getCurrentGames() {
        return null;
    }

    @Override
    public GameSession newGame(String name) {
        return null;
    }

    @Override
    public GameSession joinGame(String gameName, String playerId) {
        return null;
    }

    @Override
    public void disconnect() {

    }
}
