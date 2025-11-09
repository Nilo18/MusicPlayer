package com.example;

import java.util.regex.Pattern;

public class Command {
    Pattern syntax;
    CommandFunction function;

    public Command(Pattern syntax, CommandFunction function) {
        this.syntax = syntax;
        this.function = function;
    }
}
