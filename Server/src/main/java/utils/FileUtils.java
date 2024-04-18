package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * Method to read a file and return a list of strings.
     * @param filePath The path to the file to read.
     * @return A list of strings, where each string is a line in the file.
     * @throws IOException If the file is not found or cannot be read.
     */

    public static List<String[]> readFile(String filePath) throws IOException {
        List<String[]> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";");
                lines.add(columns);
            }
        }

        return lines;
    }
}