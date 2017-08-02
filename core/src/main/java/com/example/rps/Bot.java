package com.example.rps;

/**
 * Bot player making random moves
 *
 * @author Beka Tsotsoria
 */
public class Bot implements Player {

    private String id;

    public Bot() {
        this("Bot");
    }

    public Bot(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Weapon makeMove(GameContext context) {
        return context.getAvailableMoves().get(((int) (Math.random() * 100)) % context.getAvailableMoves().size());
    }
}
