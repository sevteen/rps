package com.example.rps;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the game itself
 *
 * @author Beka Tsotsoria
 */
public class Game {

    private final Map<String, Player> players = new ConcurrentSkipListMap<>();
    private final Map<String, AtomicInteger> playerWinCounter = new ConcurrentHashMap<>();
    private final AtomicInteger roundCounter = new AtomicInteger();

    private String name;

    public Game(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Performs single round, waits for all players to make a {@link Player#makeMove(GameContext) move} and returns result
     *
     * @return result of round
     * @throws IllegalStateException if game is not {@link #isReady() ready}
     */
    public RoundResult doRound() {
        ensureReady();
        return doRoundInternal();
    }

    /**
     * Performs rounds asynchronously indefinitely
     *
     * @param listener round listener which will receive results of rounds
     * @throws IllegalStateException if game is not {@link #isReady() ready}
     */
    public void doRoundsAsync(RoundResultListener listener) {
        ensureReady();
        new Thread(() -> {
            while (true) {
                RoundResult result = doRoundInternal();
                listener.onResult(result);
            }
        }).start();
    }

    private void ensureReady() {
        if (!isReady()) {
            throw new IllegalStateException("At least 2 players are needed");
        }
    }

    private RoundResult doRoundInternal() {
        Iterator<Player> iterator = players.values().iterator();
        Player player1 = iterator.next();
        Player player2 = iterator.next();
        Weapon weapon1 = player1.makeMove(null);
        Weapon weapon2 = player2.makeMove(null);
        boolean draw = true;
        Player winner = null;
        Player looser = null;
        Weapon winnerWeaponUsed = null;
        Weapon looserWeaponUsed = null;

        if (beats(weapon1, weapon2)) {
            winner = player1;
            winnerWeaponUsed = weapon1;
            looser = player2;
            looserWeaponUsed = weapon2;
            draw = false;
        }

        if (beats(weapon2, weapon1)) {
            winner = player2;
            winnerWeaponUsed = weapon2;
            looser = player1;
            looserWeaponUsed = weapon1;
            draw = false;
        }

        int roundCounter = this.roundCounter.incrementAndGet();
        if (draw) {
            return new RoundResult(Arrays.asList(
                new PlayerResult(player1.getId(), weapon1, false, getPlayerWinCounter(player1.getId()).get()),
                new PlayerResult(player2.getId(), weapon2, false, getPlayerWinCounter(player2.getId()).get())
            ), roundCounter);
        }
        return new RoundResult(Arrays.asList(
            new PlayerResult(winner.getId(), winnerWeaponUsed, true, getPlayerWinCounter(winner.getId()).incrementAndGet()),
            new PlayerResult(looser.getId(), looserWeaponUsed, false, getPlayerWinCounter(looser.getId()).get())), roundCounter);
    }

    private AtomicInteger getPlayerWinCounter(String playerId) {
        return playerWinCounter.computeIfAbsent(playerId, w -> new AtomicInteger());
    }

    /**
     * @return true if <code>weapon1</code> beats <code>weapon2</code>, false otherwise
     */
    private boolean beats(Weapon weapon1, Weapon weapon2) {
        if (Weapon.ROCK.is(weapon1)) {
            if (Weapon.PAPER.is(weapon2)) {
                return false;
            } else if (Weapon.SCISSORS.is(weapon2)) {
                return true;
            }
        } else if (Weapon.SCISSORS.is(weapon1)) {
            if (Weapon.PAPER.is(weapon2)) {
                return true;
            } else if (Weapon.ROCK.is(weapon2)) {
                return false;
            }
        } else if (Weapon.PAPER.is(weapon1)) {
            if (Weapon.ROCK.is(weapon2)) {
                return true;
            } else if (Weapon.SCISSORS.is(weapon2)) {
                return false;
            }
        }
        return false;
    }

    /**
     * Joins player into game, there must be at least 2 players
     * in the game
     *
     * @throws IllegalArgumentException if {@link Player#getId() id} of the player is not
     *                                  unique in this game
     * @throws NullPointerException     if player is null
     */
    public void join(Player player) {
        if (player == null) {
            throw new NullPointerException("player");
        }
        if (players.putIfAbsent(player.getId(), player) != null) {
            throw new IllegalArgumentException("Id \"" + player.getId() + "\" is already taken");
        }
    }

    /**
     * Leaves player from the game
     */
    public void leave(String playerId) {
        players.remove(playerId);
    }

    /**
     * Returns list of {@link Player#getId() IDs} of joined players
     */
    public List<String> getPlayerIds() {
        return new ArrayList<>(players.keySet());
    }

    /**
     * Returns set of available weapons in this game
     */
    public Set<Weapon> getAvailableWeapons() {
        return new HashSet<>(Arrays.asList(Weapon.ROCK, Weapon.PAPER, Weapon.SCISSORS));
    }

    /**
     * Tests whether or not game is ready, that is by checking
     * number of players, which should be at least 2
     */
    public boolean isReady() {
        return players.size() > 1;
    }
}
