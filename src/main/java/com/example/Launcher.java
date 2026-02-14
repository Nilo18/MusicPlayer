package com.example;

import java.io.OutputStream;
import java.io.PrintStream;

public class Launcher {
    public static void main(String[] args) {
        // Save the original error stream
        PrintStream originalErr = System.err;

        // Redirect System.err to a "null" stream temporarily
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                // Do nothing: eat the warnings
            }
        }));

        try {
            // Call the app - JavaFX will try to print its warning to the "null" stream
            if (args.length == 0) {
                App.main(new String[]{"init"});
            } else {
                App.main(args);
            }
        } finally {
            // Restore the original error stream so we can still see real bugs later
            System.setErr(originalErr);
        }
    }
}
