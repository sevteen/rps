package com.example.rps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the game itself
 *
 * @author Beka Tsotsoria
 */
public class Game {

    private final Logger log = LoggerFactory.getLogger(Game.class);

    private final Map<String, Player> players = new ConcurrentSkipListMap<>();
    private final Map<String, AtomicInteger> playerWinCounter = new ConcurrentHashMap<>();
    private final AtomicInteger roundCounter = new AtomicInteger();
    private AsyncPlay asyncPlay;

    private String name;
    private GameRules rules;

    /**
     * Crates game with {@link GameRules#CLASSIC classic} game rules
     *
     * @param name name of the game
     */
    public Game(String name) {
        this(name, GameRules.CLASSIC);
    }

    /**
     * Creates game with specified rules
     *
     * @param name  name of the game
     * @param rules rules of the game
     */
    public Game(String name, GameRules rules) {
        this.name = name;
        this.rules = rules;
    }

    public String getName() {
        return name;
    }

    /**
     * Performs single round, waits for all players to make a {@link Player#makeMove(GameContext) move} and returns result
     *
     * @return result of round
     * @throws IllegalStateException if game is not {@link #isReady() ready}
     * @throws MoveAbortedException  when one of the player's move gets aborted in the middle of round
     */
    public RoundResult doRound() {
        ensureReady();
        return doRoundInternal();
    }

    /**
     * Performs rounds asynchronously indefinitely until game is {@link AsyncPlay#stop() stopped}
     *
     * @param listener round listener which will receive results of rounds
     * @return handle to the game which can be {@link AsyncPlay#stop() stopped} later
     * @throws IllegalStateException if game is not {@link #isReady() ready}
     */
    public synchronized AsyncPlay doRoundsAsync(RoundResultListener listener) {
        ensureReady();
        if (asyncPlay != null) {
            log.info("Game is already running in the background");
            return asyncPlay;
        }
        AtomicBoolean running = new AtomicBoolean(true);
        new Thread(() -> {
            while (running.get()) {
                try {
                    RoundResult result = doRoundInternal();
                    listener.onResult(result);
                } catch (MoveAbortedException | NoSuchElementException e) {
                    log.warn("Seems one of the player left game unexpectedly. Stopping round loop until someone joins again", e);
                    asyncPlay.stop();
                }
            }
        }).start();
        return asyncPlay = new AsyncPlay() {

            @Override
            public boolean isPlaying() {
                return running.get();
            }

            @Override
            public void stop() {
                running.set(false);
                asyncPlay = null;
            }
        };
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
        GameContext context = new GameContext(new ArrayList<>(getAvailableWeapons()));
        Weapon weapon1 = player1.makeMove(context);
        Weapon weapon2 = player2.makeMove(context);
        log.info("Player {} made move {}", player1.getId(), weapon1);
        log.info("Player {} made move {}", player2.getId(), weapon2);
        boolean draw = true;
        Player winner = null, looser = null;
        Weapon winnerWeaponUsed = null, looserWeaponUsed = null;

        if (defeats(weapon1, weapon2)) {
            winner = player1;
            winnerWeaponUsed = weapon1;
            looser = player2;
            looserWeaponUsed = weapon2;
            draw = false;
        }

        if (defeats(weapon2, weapon1)) {
            winner = player2;
            winnerWeaponUsed = weapon2;
            looser = player1;
            looserWeaponUsed = weapon1;
            draw = false;
        }

        int roundCounter = this.roundCounter.incrementAndGet();
        if (draw) {
            return new RoundResult(Arrays.asList(
                new PlayerResult(player1.getId(), weapon1.getName(), false, getPlayerWinCounter(player1.getId()).get()),
                new PlayerResult(player2.getId(), weapon2.getName(), false, getPlayerWinCounter(player2.getId()).get())
            ), roundCounter);
        }
        RoundResult rr = new RoundResult(Arrays.asList(
            new PlayerResult(winner.getId(), winnerWeaponUsed.getName(), true, getPlayerWinCounter(winner.getId()).incrementAndGet()),
            new PlayerResult(looser.getId(), looserWeaponUsed.getName(), false, getPlayerWinCounter(looser.getId()).get())), roundCounter);
        log.info("Round completed {}", rr);
        return rr;
    }

    private AtomicInteger getPlayerWinCounter(String playerId) {
        return playerWinCounter.computeIfAbsent(playerId, w -> new AtomicInteger());
    }

    /**
     * @return true if <code>weapon1</code> defeats <code>weapon2</code>, false otherwise
     */
    private boolean defeats(Weapon weapon1, Weapon weapon2) {
        return rules.defeats(weapon1, weapon2);
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
        synchronized (players) {
            if (players.size() == 2) {
                log.warn("Ignoring player {}, because there are already 2 players in the game, which is current supported maximum", player.getId());
                return;
            }
            if (players.putIfAbsent(player.getId(), player) != null) {
                throw new IllegalArgumentException("Id \"" + player.getId() + "\" is already taken");
            }
        }
        log.info("Player {} joined game {}", player.getId(), name);
    }

    /**
     * Leaves player from the game
     */
    public void leave(String playerId) {
        synchronized (players) {
            Player player = players.remove(playerId);
            if (player instanceof AbortablePlayer) {
                log.info("Aborting player {}", player.getId());
                ((AbortablePlayer) player).abort();
            }
        }
        log.info("Player {} left game {}", playerId, name);
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
        return new HashSet<>(rules.getAllWeapons());
    }

    /**
     * Tests whether or not game is ready, that is by checking
     * number of players, which should be at least 2
     */
    public boolean isReady() {
        return players.size() > 1;
    }
}
