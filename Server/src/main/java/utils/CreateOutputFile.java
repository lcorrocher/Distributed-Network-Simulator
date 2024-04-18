package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Class for creating a text file with the given content.
 */
public class CreateOutputFile {

    /**
     *
     * @param filePath filepath to specify where output file will be saved
     * @param content Content to be written to output file.
     * @throws IOException
     */
    public static void createTextFile(String filePath, String content) throws IOException {
        // Creating the file and writing content to it
        Files.write(Path.of(filePath), content.getBytes(), StandardOpenOption.CREATE);

    }

    public static void createDirectory(String directoryPath) throws IOException {
        Path path = Paths.get(directoryPath);
        Files.createDirectories(path);
    }
}
