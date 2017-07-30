package com.example.rps;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * @author Beka Tsotsoria
 */
public class GameTest {

    private Game game;

    @Before
    public void setUp() throws Exception {
        game = new Game("Test", GameRules.CLASSIC);
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
    public void gameShouldBecomeReadyWhen2PlayersAreJoined() throws Exception {
        assertThat(game.isReady())
            .isFalse();

        game.join(new FakePlayer(Weapon.PAPER, "id1"));
        assertThat(game.isReady())
            .isFalse();

        game.join(new FakePlayer(Weapon.PAPER, "id2"));
        assertThat(game.isReady())
            .isTrue();

        game.leave("id1");
        assertThat(game.isReady())
            .isFalse();
    }

    @Test
    public void playerCanLeaveGame() throws Exception {
        game.join(new FakePlayer(Weapon.PAPER, "id"));
        game.leave("id");

        assertThat(game.getPlayerIds()).isEmpty();
    }

    @Test
    public void player1RockAndPlayer2RockShouldFinishAsDraw() throws Exception {
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.ROCK);
        FakePlayer edward = FakePlayer.using("edward", Weapon.ROCK);
        game.join(rocky);
        game.join(edward);

        assertThat(game.doRound().isDraw()).isTrue();
    }

    @Test
    public void player1RockShouldWinOverPlayer2Scissors() throws Exception {
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.ROCK);
        FakePlayer edward = FakePlayer.using("edward", Weapon.SCISSORS);
        game.join(rocky);
        game.join(edward);

        RoundResult result = doRound();

        assertRoundResult(result, rocky, Weapon.ROCK);
    }

    @Test
    public void player1PaperShouldWinOverPlayer2Rock() throws Exception {
        FakePlayer paper = FakePlayer.using("paper", Weapon.PAPER);
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.ROCK);
        game.join(paper);
        game.join(rocky);

        RoundResult result = doRound();

        assertRoundResult(result, paper, Weapon.PAPER);
    }

    @Test
    public void player1ScissorsShouldWinOverPlayer2Paper() throws Exception {
        FakePlayer edward = FakePlayer.using("edward", Weapon.SCISSORS);
        FakePlayer paper = FakePlayer.using("paper", Weapon.PAPER);
        game.join(edward);
        game.join(paper);

        RoundResult result = doRound();

        assertRoundResult(result, edward, Weapon.SCISSORS);
    }

    @Test
    public void player1ScissorsAndPlayer2ScissorsShouldFinishAsDraw() throws Exception {
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.SCISSORS);
        FakePlayer edward = FakePlayer.using("edward", Weapon.SCISSORS);
        game.join(rocky);
        game.join(edward);

        assertThat(game.doRound().isDraw()).isTrue();
    }

    @Test
    public void player2RockShouldWinOverPlayer1Scissors() throws Exception {
        FakePlayer edward = FakePlayer.using("edward", Weapon.SCISSORS);
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.ROCK);
        game.join(edward);
        game.join(rocky);

        RoundResult result = doRound();

        assertRoundResult(result, rocky, Weapon.ROCK);
    }

    @Test
    public void player2PaperShouldWinOverPlayer1Rock() throws Exception {
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.ROCK);
        FakePlayer paper = FakePlayer.using("paper", Weapon.PAPER);
        game.join(rocky);
        game.join(paper);

        RoundResult result = doRound();

        assertRoundResult(result, paper, Weapon.PAPER);
    }

    @Test
    public void player2ScissorsShouldWinOverPlayer1Paper() throws Exception {
        FakePlayer paper = FakePlayer.using("paper", Weapon.PAPER);
        FakePlayer edward = FakePlayer.using("edward", Weapon.SCISSORS);
        game.join(paper);
        game.join(edward);

        RoundResult result = doRound();

        assertRoundResult(result, edward, Weapon.SCISSORS);
    }

    @Test
    public void player1PaperAndPlayer2PaperShouldFinishAsDraw() throws Exception {
        FakePlayer rocky = FakePlayer.using("rocky", Weapon.PAPER);
        FakePlayer edward = FakePlayer.using("edward", Weapon.PAPER);
        game.join(rocky);
        game.join(edward);

        assertThat(game.doRound().isDraw()).isTrue();
    }

    @Test
    public void rockPaperAndScissorsShouldBeAsAvailableWeapons() throws Exception {
        assertThat(game.getAvailableWeapons()).isEqualTo(new HashSet<>(Arrays.asList(Weapon.ROCK, Weapon.PAPER, Weapon.SCISSORS)));
    }

    @Test(timeout = 1000)
    public void doRoundsAsyncAndThenStop() throws Exception {
        FakePlayer john = FakePlayer.inTurn("john", Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS);
        FakePlayer edward = FakePlayer.inTurn("edward", Weapon.SCISSORS, Weapon.SCISSORS, Weapon.ROCK);
        game.join(john);
        game.join(edward);

        List<RoundResult> rounds = new ArrayList<>();
        RoundResultListener l = mock(RoundResultListener.class);
        doAnswer(invocation -> rounds.add((RoundResult) invocation.getArguments()[0])).when(l).onResult(any());

        game.doRoundsAsync(l);

        while (rounds.size() < 3) ;
        assertRoundResult(rounds.get(0), edward, Weapon.SCISSORS);
        assertRoundResult(rounds.get(1), john, Weapon.ROCK);
        assertRoundResult(rounds.get(2), edward, Weapon.ROCK);
    }

    @Test
    public void canCalculatePlayerResultsCorrectly() throws Exception {
        FakePlayer john = FakePlayer.inTurn("john", Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS);
        FakePlayer edward = FakePlayer.inTurn("edward", Weapon.SCISSORS, Weapon.ROCK, Weapon.ROCK);
        game.join(john);
        game.join(edward);

        RoundResult rr1 = game.doRound();

        assertThat(rr1.getRoundNumber()).isEqualTo(1);
        assertThat(rr1.isDraw()).isFalse();
        assertThat(rr1.resultFor("john").getMove()).isEqualTo(Weapon.PAPER.getName());
        assertThat(rr1.resultFor("john").isWinner()).isFalse();
        assertThat(rr1.resultFor("john").getTotalWins()).isEqualTo(0);

        assertThat(rr1.resultFor("edward").getMove()).isEqualTo(Weapon.SCISSORS.getName());
        assertThat(rr1.resultFor("edward").isWinner()).isTrue();
        assertThat(rr1.resultFor("edward").getTotalWins()).isEqualTo(1);

        RoundResult rr2 = game.doRound();

        assertThat(rr2.getRoundNumber()).isEqualTo(2);
        assertThat(rr2.isDraw()).isTrue();
        assertThat(rr2.resultFor("john").getMove()).isEqualTo(Weapon.ROCK.getName());
        assertThat(rr2.resultFor("john").isWinner()).isFalse();
        assertThat(rr2.resultFor("john").getTotalWins()).isEqualTo(0);

        assertThat(rr2.resultFor("edward").getMove()).isEqualTo(Weapon.ROCK.getName());
        assertThat(rr2.resultFor("edward").isWinner()).isFalse();
        assertThat(rr2.resultFor("edward").getTotalWins()).isEqualTo(1);

        RoundResult rr3 = game.doRound();

        assertThat(rr3.getRoundNumber()).isEqualTo(3);
        assertThat(rr3.isDraw()).isFalse();
        assertThat(rr3.resultFor("john").getMove()).isEqualTo(Weapon.SCISSORS.getName());
        assertThat(rr3.resultFor("john").isWinner()).isFalse();
        assertThat(rr3.resultFor("john").getTotalWins()).isEqualTo(0);

        assertThat(rr3.resultFor("edward").getMove()).isEqualTo(Weapon.ROCK.getName());
        assertThat(rr3.resultFor("edward").isWinner()).isTrue();
        assertThat(rr3.resultFor("edward").getTotalWins()).isEqualTo(2);
    }

    @Test
    public void canPlayWithBot() throws Exception {
        FakePlayer john = FakePlayer.inTurn("john", Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS);
        Bot bot = new Bot();
        game.join(john);
        game.join(bot);

        RoundResult result = game.doRound();

        assertThat(result.getPlayerResults()).hasSize(2);
    }

    @Test
    public void canPlayWithLizardSpockExtension() throws Exception {
        game = new Game("extended", GameRules.LIZARD_SPOCK_EXTENSION);

        FakePlayer john = FakePlayer.inTurn("john",
            Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS,
            Weapon.LIZARD, Weapon.SPOCK, Weapon.LIZARD,
            Weapon.SCISSORS, Weapon.LIZARD, Weapon.ROCK);

        FakePlayer edward = FakePlayer.inTurn("edward",
            Weapon.PAPER, Weapon.ROCK, Weapon.SCISSORS,
            Weapon.LIZARD, Weapon.SPOCK, Weapon.PAPER,
            Weapon.SPOCK, Weapon.SPOCK, Weapon.LIZARD);

        game.join(john);
        game.join(edward);

        assertThat(game.doRound().isDraw()).isTrue();
        assertThat(game.doRound().isDraw()).isTrue();
        assertThat(game.doRound().isDraw()).isTrue();
        assertThat(game.doRound().isDraw()).isTrue();
        assertThat(game.doRound().isDraw()).isTrue();

        assertRoundResult(doRound(), john, Weapon.LIZARD);
        assertRoundResult(doRound(), edward, Weapon.SPOCK);
        assertRoundResult(doRound(), john, Weapon.LIZARD);
        assertRoundResult(doRound(), john, Weapon.ROCK);
    }

    @Test
    public void onlyFirst2PlayersShouldBeInTheGameForNow() throws Exception {
        FakePlayer john = FakePlayer.using("john", Weapon.PAPER);
        FakePlayer edward = FakePlayer.using("edward", Weapon.ROCK);
        FakePlayer jack = FakePlayer.using("jack", Weapon.PAPER);

        game.join(john);
        game.join(edward);
        game.join(jack);

        assertThat(game.getPlayerIds()).hasSize(2);
        assertThat(game.getPlayerIds()).contains("john", "edward");
    }

    private RoundResult doRound() {
        RoundResult result = game.doRound();
        assertThat(result).isNotNull();
        assertThat(result.getWinnerIds()).isNotEmpty();
        return result;
    }

    private void assertRoundResult(RoundResult result, Player winner, Weapon weapon) {
        List<String> winners = result.getWinnerIds();
        assertThat(winners).isNotEmpty();
        assertThat(winners.get(0)).isEqualTo(winner.getId());
        assertThat(result.getWeaponUsed(winners.get(0))).isEqualTo(weapon.getName());
    }
}
