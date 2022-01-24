package Lesson5Concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

public class ReadFile implements Callable<String> {

    private String fileName;

    public ReadFile(String fileName) {
        setFilename(fileName);
    }

    public String call() {
        String contents = null;
        try {
            // Read the contents of the file into this thread
            contents = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        // Return the contents of the file as a String
        return contents;
    }

    private void setFilename(String fileName) {
        this.fileName = fileName;
    }

}
