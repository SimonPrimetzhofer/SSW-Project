package common;

import java.io.*;

public class FileWriter {
    private static final String ENCODING = "utf-8";

    public static void write(String fileName, char ch) {
        try (final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), ENCODING))) {
            writer.append(ch);
        } catch (IOException ex) {
            System.err.printf("Could not write to file %s! Aborting operation...", fileName);
        }
    }
}
