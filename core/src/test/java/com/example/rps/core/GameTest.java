package com.example.rps.core;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Beka Tsotsoria
 */
public class GameTest {

    private Game game;

    @Before
    public void setUp() throws Exception {
        game = new Game("Test");
    }

    @Test
    public void gameShouldHaveName() throws Exception {
        assertThat(game.getName()).isEqualTo("Test");
    }

    @Test
    public void doRoundShouldThrowIllegalStateExceptionWhenThereAreZeroPlayersInTheGame() throws Exception {
        assertThatThrownBy(() -> game.doRound())
            .hasMessageContaining("At least 2 players are needed")
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void doRoundShouldThrowIllegalStateExceptionWhenThereIsOnePlayerInTheGame() throws Exception {
        game.join(new FakePlayer());

        assertThatThrownBy(() -> game.doRound())
            .hasMessageContaining("At least 2 players are needed")
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void joinShouldFailWithNullPointerExceptionIfPlayerIsNull() throws Exception {
        assertThatThrownBy(() -> game.join(null))
            .hasMessageContaining("player")
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void joinShouldFailWithIllegalArgumentExceptionIfPlayerIdIsAlreadyTaken() throws Exception {
        game.join(new FakePlayer(Weapon.PAPER, "id"));
        assertThatThrownBy(() -> game.join(new FakePlayer(Weapon.PAPER, "id")))
            .hasMessageContaining("Id \"id\" is already taken")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldGetListOfPlayers() throws Exception {
        game.join(new FakePlayer(Weapon.PAPER, "id"));
        assertThat(game.getPlayerIds()).isEqualTo(Arrays.asList("id"));

        game.join(new FakePlayer(Weapon.PAPER, "id2"));
        assertThat(game.getPlayerIds()).isEqualTo(Arrays.asList("id", "id2"));
    }

    @Test
    public void playerCanLeaveGame() throws Exception {
        game.join(new FakePlayer(Weapon.PAPER, "id"));
        game.leave("id");

        assertThat(game.getPlayerIds()).isEmpty();
    }

    @Test
    public void player1RockShouldWinOverPlayer2Scissors() throws Exception {
        FakePlayer rocky = FakePlayer.using(Weapon.ROCK);
        FakePlayer edward = FakePlayer.using(Weapon.SCISSORS);
        game.join(rocky);
        game.join(edward);

        RoundResult result = doRound();

        assertRoundResult(result, rocky, Weapon.ROCK);
    }

    @Test
    public void player1PaperShouldWinOverPlayer2Rock() throws Exception {
        FakePlayer paper = FakePlayer.using(Weapon.PAPER);
        FakePlayer rocky = FakePlayer.using(Weapon.ROCK);
        game.join(paper);
        game.join(rocky);

        RoundResult result = doRound();

        assertRoundResult(result, paper, Weapon.PAPER);
    }

    @Test
    public void player1ScissorsShouldWinOverPlayer2Paper() throws Exception {
        FakePlayer edward = FakePlayer.using(Weapon.SCISSORS);
        FakePlayer paper = FakePlayer.using(Weapon.PAPER);
        game.join(edward);
        game.join(paper);

        RoundResult result = doRound();

        assertRoundResult(result, edward, Weapon.SCISSORS);
    }

    @Test
    public void player2RockShouldWinOverPlayer1Scissors() throws Exception {
        FakePlayer edward = FakePlayer.using(Weapon.SCISSORS);
        FakePlayer rocky = FakePlayer.using(Weapon.ROCK);
        game.join(edward);
        game.join(rocky);

        RoundResult result = doRound();

        assertRoundResult(result, rocky, Weapon.ROCK);
    }

    @Test
    public void player2PaperShouldWinOverPlayer1Rock() throws Exception {
        FakePlayer rocky = FakePlayer.using(Weapon.ROCK);
        FakePlayer paper = FakePlayer.using(Weapon.PAPER);
        game.join(rocky);
        game.join(paper);

        RoundResult result = doRound();

        assertRoundResult(result, paper, Weapon.PAPER);
    }

    @Test
    public void player2ScissorsShouldWinOverPlayer1Paper() throws Exception {
        FakePlayer paper = FakePlayer.using(Weapon.PAPER);
        FakePlayer edward = FakePlayer.using(Weapon.SCISSORS);
        game.join(paper);
        game.join(edward);

        RoundResult result = doRound();

        assertRoundResult(result, edward, Weapon.SCISSORS);
    }

    private RoundResult doRound() {
        RoundResult result = game.doRound();
        assertThat(result).isNotNull();
        assertThat(result.getWinner()).isNotNull();
        return result;
    }

    private void assertRoundResult(RoundResult result, Player winner, Weapon weapon) {
        assertThat(result.getWinner().getId()).isEqualTo(winner.getId());
        assertThat(result.getWeaponUsed()).isEqualTo(weapon);
    }
}
