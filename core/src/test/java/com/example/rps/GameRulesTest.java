package com.example.rps;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Beka Tsotsoria
 */
public class GameRulesTest {

    @Test
    public void canBuildValidGameRules() throws Exception {
        GameRules rules = GameRules.builder()
            .defeats(Weapon.SCISSORS, Weapon.PAPER)
            .defeats(Weapon.PAPER, Weapon.ROCK)
            .defeats(Weapon.ROCK, Weapon.SCISSORS)
            .build();

        assertThat(rules.defeats(Weapon.PAPER, Weapon.ROCK)).isTrue();
        assertThat(rules.defeats(Weapon.ROCK, Weapon.SCISSORS)).isTrue();
        assertThat(rules.defeats(Weapon.SCISSORS, Weapon.PAPER)).isTrue();

        assertThat(rules.defeats(Weapon.ROCK, Weapon.PAPER)).isFalse();
        assertThat(rules.defeats(Weapon.SCISSORS, Weapon.ROCK)).isFalse();
        assertThat(rules.defeats(Weapon.PAPER, Weapon.SCISSORS)).isFalse();
    }

    @Test
    public void buildShouldFailWithInconsistentRulesExceptionWhenTwoWeaponsDefeatEachOther() throws Exception {
        assertThatThrownBy(() -> GameRules.builder()
            .defeats(Weapon.SCISSORS, Weapon.PAPER)
            .defeats(Weapon.PAPER, Weapon.SCISSORS)
            .defeats(Weapon.ROCK, Weapon.SCISSORS)
            .defeats(Weapon.PAPER, Weapon.ROCK)
            .build())
            .hasMessageContaining("scissors and paper defeat each other")
            .isInstanceOf(InconsistentGameRulesException.class);
    }

    @Test
    public void buildShouldFailWithInconsistentRulesExceptionWhenWeaponDoesNotHaveDefeater() throws Exception {
        assertThatThrownBy(() -> GameRules.builder()
            .defeats(Weapon.PAPER, Weapon.ROCK)
            .defeats(Weapon.SCISSORS, Weapon.PAPER)
            .build())
            .hasMessageContaining("scissors is not defeatable")
            .isInstanceOf(InconsistentGameRulesException.class);
    }
}
