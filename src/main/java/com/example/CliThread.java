package com.example;

import com.example.commands.CommandHandler;
import com.example.player.PlayerManager;
import com.example.searcher.Searcher;
import com.example.utilities.Utilities;
import javafx.application.Platform;

import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
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
        CommandHandler.addCommand("play", Pattern.compile("^play -\"([^\"]+)\"$"),
                (Object... option) ->
                Searcher.search(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)), 1L)
        );
        /* Shortcut command for playing */
        CommandHandler.addCommand("p", Pattern.compile("^p -\"([^\"]+)\"$"),
                (Object... option) ->
                        Searcher.search(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)), 1L)
        );
        CommandHandler.addCommand("search", Pattern.compile("^search -\"([^\"]+)\"$"),
                (Object... option)  -> Searcher.search(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)), 5L)
        );
        /* Shortcut command for searching */
        CommandHandler.addCommand("se", Pattern.compile("^se -\"([^\"]+)\"$"),
                (Object... option)  -> Searcher.search(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)), 5L)
        );
        CommandHandler.addCommand("forward", Pattern.compile("^forward -([^\"]+)$"),
                (Object... option) ->
                PlayerManager.forward(String.join(" ", Arrays.stream(option)
                        .map(Object::toString)
                        .toArray(String[]::new)))
        );
        /* Shortcut command for forwarding */
        CommandHandler.addCommand("f", Pattern.compile("^f -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.forward(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        CommandHandler.addCommand("rewind", Pattern.compile("^rewind -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.rewind(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        /* Shortcut command for rewinding */
        CommandHandler.addCommand("r", Pattern.compile("^r -([^\"]+)$"),
                (Object... option) ->
                        PlayerManager.rewind(String.join(" ", Arrays.stream(option)
                                .map(Object::toString)
                                .toArray(String[]::new)))
        );
        // Starting with ^ and ending with $ means that there are no arguments expected
        CommandHandler.addCommand("loop", Pattern.compile("^loop$"),
                (Object... a) -> Platform.runLater(PlayerManager::loop)
        );
        CommandHandler.addCommand("l", Pattern.compile("^l$"),
                (Object... a) -> Platform.runLater(PlayerManager::loop)
        );
        CommandHandler.addCommand("skip", Pattern.compile("^skip$"),
                (Object... a) -> PlayerManager.skip()
        );
        CommandHandler.addCommand("s", Pattern.compile("^s$"),
                (Object... a) -> PlayerManager.skip()
        );
        CommandHandler.addCommand("previous", Pattern.compile("^previous$"),
                (Object... a) -> PlayerManager.previous()
        );
        CommandHandler.addCommand("prev", Pattern.compile("^prev$"),
                (Object... a) -> PlayerManager.previous()
        );
        CommandHandler.addCommand("pause", Pattern.compile("^pause$"),
            (Object... a) -> PlayerManager.pause()
        );
        CommandHandler.addCommand("pa", Pattern.compile("^pa$"),
                (Object... a) -> PlayerManager.pause()
        );
        CommandHandler.addCommand("resume", Pattern.compile("^resume$"),
                (Object... a) -> PlayerManager.resume()
        );
        CommandHandler.addCommand("re", Pattern.compile("^re$"),
                (Object... a) -> PlayerManager.resume()
        );
        CommandHandler.addCommand("exit", Pattern.compile("^exit$"),
                (Object... a) -> Utilities.exit(isRunning)
        );
        CommandHandler.addCommand("e", Pattern.compile("^e$"),
                (Object... a) -> Utilities.exit(isRunning)
        );
        CommandHandler.addCommand("help", Pattern.compile("^help$"),
                (Object... a) -> Utilities.showAllCommands()
        );
        CommandHandler.addCommand("list", Pattern.compile("^list$"),
                (Object... a) -> PlayerManager.showPlaylist()
        );
        CommandHandler.addCommand("li", Pattern.compile("^li$"),
                (Object... a) -> PlayerManager.showPlaylist()
        );
        CommandHandler.addCommand("duration", Pattern.compile("^duration$"),
                (Object... a) -> Utilities.showDuration()
        );
        CommandHandler.addCommand("dur", Pattern.compile("^dur$"),
                (Object... a) -> Utilities.showDuration()
        );

        CommandHandler.addCommand("remaining", Pattern.compile("^remaining$"),
                (Object... a) -> Utilities.showRemainingTime()
        );
        CommandHandler.addCommand("rem", Pattern.compile("^rem$"),
                (Object... a) -> Utilities.showRemainingTime()
        );

        while (isRunning.get()) {
            System.out.print("#> ");
            String option = sc.nextLine().trim();

            CommandHandler.executeCommand(option);
        }
    }
}
