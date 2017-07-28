package com.example.rps;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Beka Tsotsoria
 */
public class QueuedPlayerTest {

    @Test
    public void canCreateQueuedPlayer() throws Exception {
        QueuedPlayer player = new QueuedPlayer("id");
        assertThat(player.getId()).isEqualTo("id");
    }

    @Test
    public void shouldMakeConfiguredMove() throws Exception {
        QueuedPlayer player = new QueuedPlayer("id");
        player.addMove(Weapon.SCISSORS);
        player.addMove(Weapon.ROCK);
        player.addMove(Weapon.PAPER);

        assertThat(player.makeMove(new GameContext())).isEqualTo(Weapon.SCISSORS);
        assertThat(player.makeMove(new GameContext())).isEqualTo(Weapon.ROCK);
        assertThat(player.makeMove(new GameContext())).isEqualTo(Weapon.PAPER);
    }
}
