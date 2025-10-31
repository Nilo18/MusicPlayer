package com.example;

import java.util.regex.Pattern;

public class Command {
//    String name;
    Pattern syntax;
    CommandFunction function;

    public Command(Pattern syntax, CommandFunction function) {
        this.syntax = syntax;
        this.function = function;
    }
}
