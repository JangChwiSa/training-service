package com.jangchwisa.trainingservice.training.focus.service;

import com.jangchwisa.trainingservice.training.focus.repository.FocusTrainingRepository.NewFocusCommand;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FocusCommandGenerator {

    private static final String[][] SIMPLE_COMMANDS = {
            {"Raise blue flag", "BLUE_UP"},
            {"Raise white flag", "WHITE_UP"}
    };

    private static final String[][] MEDIUM_COMMANDS = {
            {"Raise blue flag", "BLUE_UP"},
            {"Lower blue flag", "BLUE_DOWN"},
            {"Raise white flag", "WHITE_UP"},
            {"Lower white flag", "WHITE_DOWN"}
    };

    private static final String[][] COMPLEX_COMMANDS = {
            {"Raise blue flag", "BLUE_UP"},
            {"Lower blue flag", "BLUE_DOWN"},
            {"Raise white flag", "WHITE_UP"},
            {"Lower white flag", "WHITE_DOWN"},
            {"Raise both flags", "BOTH_UP"},
            {"Lower both flags", "BOTH_DOWN"}
    };

    public List<NewFocusCommand> generate(int durationSeconds, int commandIntervalMs, String commandComplexity) {
        int commandCount = Math.max(1, (durationSeconds * 1000) / commandIntervalMs);
        String[][] sourceCommands = commandsFor(commandComplexity);
        List<NewFocusCommand> commands = new ArrayList<>();

        for (int index = 0; index < commandCount; index++) {
            String[] command = sourceCommands[index % sourceCommands.length];
            commands.add(new NewFocusCommand(index + 1, command[0], command[1], index * commandIntervalMs));
        }

        return commands;
    }

    private String[][] commandsFor(String commandComplexity) {
        return switch (commandComplexity) {
            case "SIMPLE" -> SIMPLE_COMMANDS;
            case "MEDIUM" -> MEDIUM_COMMANDS;
            case "COMPLEX" -> COMPLEX_COMMANDS;
            default -> MEDIUM_COMMANDS;
        };
    }
}
