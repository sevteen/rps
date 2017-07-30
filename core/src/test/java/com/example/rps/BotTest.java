package com.example.rps;

import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Beka Tsotsoria
 */
public class BotTest {

    @Test
    public void canHaveId() throws Exception {
        assertThat(new Bot("name").getId()).isEqualTo("name");
    }

    @Test
    public void canMakeRandomMoves() throws Exception {
         Bot bot = new Bot();

         GameContext paperContext = new GameContext(Arrays.asList(Weapon.PAPER));
         assertThat(bot.makeMove(paperContext)).isEqualTo(Weapon.PAPER);

        GameContext rockContext = new GameContext(Arrays.asList(Weapon.ROCK));
        assertThat(bot.makeMove(rockContext)).isEqualTo(Weapon.ROCK);

        GameContext scissorsContext = new GameContext(Arrays.asList(Weapon.SCISSORS));
        assertThat(bot.makeMove(scissorsContext)).isEqualTo(Weapon.SCISSORS);

        assertThat(bot.makeMove(new GameContext(Arrays.asList(Weapon.SCISSORS, Weapon.ROCK, Weapon.PAPER))))
            .isNotNull();
    }
}
