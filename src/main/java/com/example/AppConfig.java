package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

// Class for accessing .env values safely
public class AppConfig {
    public String getInjectedKey() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) return null;
            prop.load(input);
            return prop.getProperty("api.key");
        } catch (IOException ex) {
            System.out.println("Couldn't inject key: " + ex);
            return "";
        }
    }

}
