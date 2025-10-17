package com.example;

import io.github.cdimascio.dotenv.Dotenv;

// Class for accessing .env values safely
public class AppConfig {
    private static final Dotenv DOTENV = Dotenv.load();

    public static String getDotenvValue(String val) {
        return DOTENV.get(val, ""); // Return an empty string if the value doesn't exist in .env
    }
}
