package com.example.commands;

import java.util.regex.Pattern;

public class Command {
    public Pattern syntax;
    public CommandFunction function;

    public Command(Pattern syntax, CommandFunction function) {
        this.syntax = syntax;
        this.function = function;
    }
}
