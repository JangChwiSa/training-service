package com.jangchwisa.trainingservice.training.focus.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository.NewFocusCommand;
import java.util.List;
import org.junit.jupiter.api.Test;

class FocusCommandGeneratorTest {

    FocusCommandGenerator generator = new FocusCommandGenerator();

    @Test
    void generatesCommandsByDurationAndInterval() {
        List<NewFocusCommand> commands = generator.generate(6, 3000, "SIMPLE");

        assertThat(commands).hasSize(2);
        assertThat(commands.get(0).order()).isEqualTo(1);
        assertThat(commands.get(0).displayAtMs()).isZero();
        assertThat(commands.get(1).order()).isEqualTo(2);
        assertThat(commands.get(1).displayAtMs()).isEqualTo(3000);
    }

    @Test
    void generatesAtLeastOneCommand() {
        List<NewFocusCommand> commands = generator.generate(1, 3000, "SIMPLE");

        assertThat(commands).hasSize(1);
    }
}
