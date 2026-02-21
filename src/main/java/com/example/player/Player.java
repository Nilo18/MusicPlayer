package com.example.player;

import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.jline.consoleui.prompt.ConsolePrompt;
import org.jline.consoleui.prompt.PromptResultItemIF;
import org.jline.consoleui.prompt.builder.ListPromptBuilder;
import org.jline.consoleui.prompt.builder.PromptBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Player {
    private MediaPlayer player;
    private Queue queue = new Queue();
    private boolean isLooped = false;
    // Made this static so PlayerUtilities.printPlaylist() doesn't have to take it as the 4th parameter
    // PlayerUtilities.printPlaylist() needs access to it in order to reset the selection mode
    // in case the playlist is empty
    private static AtomicBoolean isSelecting = new AtomicBoolean(false);

    public void play(Path musicPath) {
        if (player != null) {
            player.dispose();
        }

        // Add new music to queue,
        // this ensures that the program doesn't have to be restarted
        // in order for the new music to be included in the queue
        System.out.println();
        Media media = new Media(musicPath.toUri().toString());
        player = new MediaPlayer(media);

        isLooped = false;
        // Add listener to the end of the music so it plays the next one in the queue
        player.setOnEndOfMedia(this::playNext);

        player.setOnReady(() -> {
            player.seek(Duration.ZERO); // ensure playback starts from beginning
            player.play();
        });
        player.setOnError(() -> {
            MediaException ex = player.getError();
            switch (ex.getType()) {
                case MEDIA_UNSUPPORTED -> System.out.println("The music contains unsupported file format.");
                case MEDIA_INACCESSIBLE -> System.out.println("Couldn't open the downloaded music file.");
                case MEDIA_UNAVAILABLE -> System.out.println("Media is currently unavailable.");
                default -> System.out.println("Unknown error has occurred: " + ex.getType());
            }
        });
    }

    public void playNext() {
        Path nextMusic = queue.getNextMusic();
        if (nextMusic != null) {
            play(nextMusic);
        } else {
            System.out.println("Playlist finished.");
        }
    }

    public void loop() {
        if (player != null) {
            isLooped = true;
            player.setOnEndOfMedia(() -> {
                player.seek(Duration.ZERO);
                player.play();
            });
        }
        player.setCycleCount(MediaPlayer.INDEFINITE);
        this.isLooped = true;
    }

    public void pause() {
        // Ensure that the player is properly initialized
        if (player == null) {
            System.out.println("No music to pause.");
            return;
        }
        // Only pause if there is a music playing, this condition is handled in PlayerManager as well
        // But PlayerManager is basically a wrapper of a single Player object and handles the condition
        // only for that specific Player object, here we handle the case for all Player objects to ensure
        // That none of them throw unexpected exceptions
        MediaPlayer.Status status = player.getStatus();
        if (status == MediaPlayer.Status.PLAYING) {
            player.pause();
            System.out.println("Music paused.");
        }
    }

    public void resume() {
        // If the player hasn't been initialized, exit prematurely to prevent nullptr errors
        if (player == null) {
            System.out.println("No music to resume.");
            return;
        }

        MediaPlayer.Status status = player.getStatus();
        // If the music isn't already playing, play it again
        if (status != MediaPlayer.Status.PLAYING) {
            player.play();
            System.out.println("Music resumed.");
        }
    }

    public void forward(int howMuch) {
        if (player == null) {
            System.out.println("No music to forward.");
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration newTime = currentTime.add(Duration.seconds(howMuch));
        Duration totalTime = player.getTotalDuration();
        if (!isLooped) {
            player.seek(newTime);
        } else {
            if (newTime.greaterThanOrEqualTo(totalTime)) {
                newTime = Duration.ZERO;
            }
            player.seek(newTime);
        }
    }

    public void rewind(int howMuch) {
        if (player == null) {
            System.out.println("No music to rewind.");
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration newTime = currentTime.subtract(Duration.seconds(howMuch));

        if (newTime.lessThan(Duration.ZERO)) {
            newTime = Duration.ZERO;
        }

        player.seek(newTime);
    }

    public Queue getQueue() {
        return queue;
    }

    public void setLoopState(boolean val) {
        this.isLooped = val;
    }

    public boolean isLooping() {
        return isLooped;
    }

    /* Enters a mode where user can choose between the playlist using arrows */
    public void activatePlaylistSelectionMode() {
        List<Path> playlist = queue.getPlaylist();
        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.builder().system(true).build();
            terminal.flush();
            terminal.writer().flush();

            Thread.sleep(50);
            // Inside mp list, before prompt.prompt()
            while (terminal.reader().available() > 0) {
                int ch  = terminal.reader().read();
                System.out.println(ch);
            }

            terminal.puts(InfoCmp.Capability.enter_ca_mode);
            terminal.puts(InfoCmp.Capability.cursor_invisible);
            terminal.flush();
            isSelecting.set(true);
            label:
            while (isSelecting.get()) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                ConsolePrompt prompt = new ConsolePrompt(terminal);
                PromptBuilder builder = prompt.getPromptBuilder();
                ListPromptBuilder listBuilder = builder.createListPrompt().
                        name("selection").
                        message("Press ARROW UP or ARROW DOWN to navigate over the list.").
                        message("Press ARROW RIGHT or ARROW LEFT to change pages.").
                        message("Viewing page " + PlaylistPagination.getPageNumber());

                PlayerUtilities.printPlaylist(playlist, listBuilder, terminal);

                listBuilder.addPrompt();
                Map<String, PromptResultItemIF> result = prompt.prompt(builder.build());
                if (result == null) {
                    isSelecting.set(false);
                    break;
                }
                PromptResultItemIF selectedItem = result.get("selection");

                if (selectedItem != null) {
                    String selectId = selectedItem.getResult();

                    switch (selectId) {
                        case "NEXT":
                            PlaylistPagination.moveForward(playlist, terminal, listBuilder);
                            break;
                        case "PREV":
                            PlaylistPagination.moveBehind(playlist, terminal, listBuilder);
                            break;
                        case "QUIT":
                            isSelecting.set(false);
                            terminal.puts(InfoCmp.Capability.cursor_normal);
                            terminal.puts(InfoCmp.Capability.exit_ca_mode);
                            terminal.flush();
                            break label;
                        default:
                            int musicIndex = Integer.parseInt(selectId);
                            PlayerUtilities.playPlaylistMusic(String.valueOf(playlist.get(musicIndex)), terminal);
                            terminal.puts(InfoCmp.Capability.cursor_normal);
                            terminal.puts(InfoCmp.Capability.exit_ca_mode);
                            terminal.flush();
                            isSelecting.set(false);
                            break label;
                    }
                }
            }

            terminal.close();
        } catch (IOException err) {
            System.out.println("Couldn't initialize the terminal.");
        } catch (Exception err) {
            System.out.println("Unexpected error has occurred while trying to show the playlist: " + err);
        } finally {
            if (terminal != null) {
                terminal.puts(InfoCmp.Capability.cursor_normal);
                terminal.puts(InfoCmp.Capability.exit_ca_mode);
                terminal.flush();
            }
        }
    }

    public static boolean getPlaylistSelectionState() {
        return isSelecting.get();
    }

    public static void setPlaylistSelectionMode(boolean val) {
        isSelecting.set(val);
    }
}
