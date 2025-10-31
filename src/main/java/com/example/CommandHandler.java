package com.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandHandler {
    private static final HashMap<String, Command> commands = new HashMap<>();

    public static void addCommand(String name, Pattern syntax, CommandFunction function) {
        commands.put(name, new Command(syntax, function));
    }

    public static void executeCommand(String command) {
        for (Command cmd : commands.values()) {
            // Check the input against the regex syntax
            // This ensures that syntax is always valid
            Matcher matcher = cmd.syntax.matcher(command);

            if (matcher.matches()) {
                // Extract the argument if the regex check succeeded
                Object[] args = new Object[matcher.groupCount()];
                for (int i = 0; i < args.length; i++) {
                    args[i] = matcher.group(i + 1);
                }
                cmd.function.execute(args);
                return;
            }
        }
        System.out.println("Unrecognized command or invalid syntax.");
    }
}
