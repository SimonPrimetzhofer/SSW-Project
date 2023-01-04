package common;

import java.io.*;

public class FileWriter {
    private static String ENCODING = "utf-8";

    public static void write(String fileName, String text) {
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), ENCODING))) {
            writer.write(text);
        } catch (IOException ex) {
            System.err.printf("Could not write to file %s! Aborting operation...", fileName);
        }
    }
}
