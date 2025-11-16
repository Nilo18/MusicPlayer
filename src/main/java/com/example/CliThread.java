package com.example;

import com.example.commands.CommandHandler;
import com.example.player.PlayerManager;
import com.example.searcher.Searcher;
import com.example.utilities.Utilities;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class CliThread implements Runnable {

    @Override
    public void run() {
        // isRunning is Atomic because a lambda has to use it
        AtomicBoolean isRunning = new AtomicBoolean(true);
        Scanner sc = new Scanner(System.in);

        /*
           Pattern.compile() is used to define valid syntaxes for the commands
           to ensure that the user uses the commands with the intended syntax.
           ^ matches the start of the string and ensures the command always begins with mp
           $ matches the end of the string and ensures that there are no other characters after the command
           \" and \" match the start and the end of the search keyword
           () inside define a capture group to store the keyword separately
           [^\"]+ matches one or more quotes that are not a ".
           Also join the Object vararg array into a valid string by creating a stream from an array of Objects
           .map(Object::toString) converts each Object to its string representation
           .toArray(String[]::new) collects the converted values into a new array
           String.join(" ") is used to join the strings with spaces between them
        */
        CommandHandler.addCommand("mp play", Pattern.compile("^mp play -\"([^\"]+)\"$"),
                (Object... option) ->
                Searcher.search(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)), 1L)
        );
        /* Shortcut command for playing */
        CommandHandler.addCommand("mp p", Pattern.compile("^mp p -\"([^\"]+)\"$"),
                (Object... option) ->
                        Searcher.search(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)), 1L)
        );
        CommandHandler.addCommand("mp search", Pattern.compile("^mp search -\"([^\"]+)\"$"),
                (Object... option)  -> Searcher.search(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)), 5L)
        );
        /* Shortcut command for searching */
        CommandHandler.addCommand("mp se", Pattern.compile("^mp se -\"([^\"]+)\"$"),
                (Object... option)  -> Searcher.search(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)), 5L)
        );
        CommandHandler.addCommand("mp forward", Pattern.compile("^mp forward -([^\"]+)$"),
                (Object... option) ->
                PlayerManager.forward(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)))
        );
        /* Shortcut command for forwarding */
        CommandHandler.addCommand("mp f", Pattern.compile("^mp f -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.forward(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        CommandHandler.addCommand("mp rewind", Pattern.compile("^mp rewind -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.rewind(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        /* Shortcut command for rewinding */
        CommandHandler.addCommand("mp r", Pattern.compile("^mp r -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.rewind(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        // Starting with ^ and ending with $ means that there are no arguments expected
        CommandHandler.addCommand("mp loop", Pattern.compile("^mp loop$"),
                (Object... a) -> Platform.runLater(PlayerManager::loop)
        );
        CommandHandler.addCommand("mp l", Pattern.compile("^mp l$"),
                (Object... a) -> Platform.runLater(PlayerManager::loop)
        );
        CommandHandler.addCommand("mp skip", Pattern.compile("^mp skip$"),
                (Object... a) -> PlayerManager.skip()
        );
        CommandHandler.addCommand("mp s", Pattern.compile("^mp s$"),
                (Object... a) -> PlayerManager.skip()
        );
        CommandHandler.addCommand("mp pause", Pattern.compile("^mp pause$"),
            (Object... a) -> PlayerManager.pause()
        );
        CommandHandler.addCommand("mp pa", Pattern.compile("^mp pa$"),
                (Object... a) -> PlayerManager.pause()
        );
        CommandHandler.addCommand("mp resume", Pattern.compile("^mp resume$"),
                (Object... a) -> PlayerManager.resume()
        );
        CommandHandler.addCommand("mp re", Pattern.compile("^mp re$"),
                (Object... a) -> PlayerManager.resume()
        );
        CommandHandler.addCommand("mp exit", Pattern.compile("^mp exit$"),
                (Object... a) -> Utilities.exit(isRunning)
        );
        CommandHandler.addCommand("mp e", Pattern.compile("^mp e$"),
                (Object... a) -> Utilities.exit(isRunning)
        );
        CommandHandler.addCommand("mp help", Pattern.compile("^mp help$"),
                (Object... a) -> Utilities.showAllCommands()
        );
        CommandHandler.addCommand("mp list", Pattern.compile("^mp list$"),
                (Object... a) -> PlayerManager.showPlaylist()
        );
        CommandHandler.addCommand("mp li", Pattern.compile("^mp li$"),
                (Object... a) -> PlayerManager.showPlaylist()
        );

        while (isRunning.get()) {
            if (!sc.hasNextLine()) {
                break;
            }

            String option = sc.nextLine().trim();

            CommandHandler.executeCommand(option);
        }
    }
}
