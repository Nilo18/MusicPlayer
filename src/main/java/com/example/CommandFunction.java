package com.example;

// Custom wrapper for functions which will be called by the commands
// Used in the commands HashMap
@FunctionalInterface
public interface CommandFunction {
    void execute(Object... args);
}
