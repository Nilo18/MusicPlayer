package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.util.Map;

// Class for accessing .env values safely
public class AppConfig {
    // Old directory path C:/Users/hp/MusicPlayer
    private static final Dotenv DOTENV = Dotenv.configure().directory("C:/Program Files/JXPlayer").load();

    public static String getDotenvValue(String val) {
//        System.out.println("Loaded .env variables:");

//        for (DotenvEntry entry : DOTENV.entries()) {
//            System.out.println(entry.getKey() + " = " + entry.getValue());
//        }
        return DOTENV.get(val, ""); // Return an empty string if the value doesn't exist in .env
    }
}
