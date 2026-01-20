import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

public class MP3Player {

    private AdvancedPlayer player;
    private String currentFile;
    private int pausedOnFrame = 0;
    private Thread playerThread;

    // Play a song from a specific frame
    public void play(String filename) {
        try {
            currentFile = filename;
            FileInputStream fis = new FileInputStream(filename);
            player = new AdvancedPlayer(fis);

            // Track frames played for pause/resume
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackFinished(PlaybackEvent evt) {
                    pausedOnFrame += evt.getFrame();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Run playback in a separate thread so menu stays responsive
        playerThread = new Thread(() -> {
            try {
                player.play(pausedOnFrame, Integer.MAX_VALUE);
            } catch (JavaLayerException e) {
                e.printStackTrace();
            }
        });

        playerThread.start();
    }

    public void pause() {
        if (player != null) {
            player.close();
            System.out.println("Paused!");
        }
    }

    public void stop() {
        if (player != null) {
            player.close();
            pausedOnFrame = 0;
            System.out.println("Stopped!");
        }
    }

    public void resume() {
        if (currentFile != null) {
            play(currentFile);
            System.out.println("Resuming...");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MP3Player mp3 = new MP3Player();

        // Folder containing songs
        File folder = new File("songs");
        File[] songs = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        if (songs == null || songs.length == 0) {
            System.out.println("No MP3 files found in the 'songs' folder!");
            return;
        }

        while (true) {
            System.out.println("\n🎵 Available Songs:");
            for (int i = 0; i < songs.length; i++) {
                System.out.println((i + 1) + ". " + songs[i].getName());
            }
            System.out.println("0. Exit");

            System.out.println("\nCommands: play [number], pause, resume, stop, exit");
            System.out.print("Enter command: ");
            String input = sc.nextLine();
            String[] parts = input.split(" ");

            switch (parts[0].toLowerCase()) {
                case "play":
                    if (parts.length < 2) {
                        System.out.println("Specify song number!");
                        break;
                    }
                    try {
                        int choice = Integer.parseInt(parts[1]);
                        if (choice < 1 || choice > songs.length) {
                            System.out.println("Invalid song number!");
                            break;
                        }
                        mp3.stop(); // stop any currently playing song
                        mp3.pausedOnFrame = 0; // reset frame
                        mp3.play(songs[choice - 1].getPath());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number!");
                    }
                    break;

                case "pause":
                    mp3.pause();
                    break;

                case "resume":
                    mp3.resume();
                    break;

                case "stop":
                    mp3.stop();
                    break;

                case "exit":
                    mp3.stop();
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Unknown command!");
            }
        }
    }
}
