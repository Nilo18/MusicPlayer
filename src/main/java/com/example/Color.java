package com.example;

/* This enum will be used when music selection mode is active.
*  For example in mp list command, BLUE will be used to highlight the current music the user is choosing */
public enum Color {
    // Reset is needed to make sure that the color given to a text isn't leaked to other text unintentionally
    RESET("\u001B[0m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    BLUE("\u001B[34m"),
    YELLOW("\u001B[33m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m");

    private final String code;
    Color(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return code;
    }
}
