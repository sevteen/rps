package com.example.rps;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(player.makeMove(newContext())).isEqualTo(Weapon.SCISSORS);
        assertThat(player.makeMove(newContext())).isEqualTo(Weapon.ROCK);
        assertThat(player.makeMove(newContext())).isEqualTo(Weapon.PAPER);
    }

    @Test
    public void shouldReceiveMoveAbortedExceptionWhenAborted() throws Exception {
        QueuedPlayer player = new QueuedPlayer("id");

        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            player.abort();
        }).start();

        assertThatThrownBy(() -> player.makeMove(newContext()))
            .hasMessageContaining("id")
            .isInstanceOf(MoveAbortedException.class);

    }

    private GameContext newContext() {
        return new GameContext(Arrays.asList(Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS));
    }
}
