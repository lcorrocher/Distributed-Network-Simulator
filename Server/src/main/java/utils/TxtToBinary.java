package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class for extracting from txt file / converting string to 8-bit binary.
 */
public class TxtToBinary {

    /**
     * Extracts the content of a text file and returns it as a string.
     *
     * @param filepath The path to the text file.
     * @return The content of the text file as a string.
     * @throws IOException If an I/O error occurs.
     */
    public static String extractFromFile(String filepath) throws IOException{
        return Files.readString(Path.of(filepath));
    }


    /**
     * Converts a string into its binary representation (8-bit format).
     *
     * @param s The input string to be converted.
     * @return The binary representation of the input string.
     * @throws IOException If an I/O error occurs.
     */
    public static String convertToBinary(String s) throws IOException {
        byte[] bytes = s.getBytes();

        StringBuilder binaryContent = new StringBuilder();
        for (byte b : bytes) {
            // ensuring that null spaces are "padded" to make each letter 8 bits long
            binaryContent.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryContent.toString();
    }
}


