package utils;

import java.io.IOException;

/**
 * Class for converting 8-bit binary back to string.
 */

public class BinaryToTxt {

    /**
     * Converts a binary string to its corresponding text representation.
     *
     * @param binaryString The input binary string to be converted.
     * @return The text representation of the binary string.
     * @throws IOException If an I/O error occurs during the conversion.
     */
    public static String convertToText(String binaryString) throws IOException {
        String outputText = "";
        // Splitting the binary string into chunks of 8 characters
        for (int i = 0; i < binaryString.length(); i += 8) {
            String binaryChunk = binaryString.substring(i, Math.min(i + 8, binaryString.length()));

            // Convert each binary chunk to its decimal equivalent and then to a character
            int decimalValue = Integer.parseInt(binaryChunk, 2);
            char character = (char) decimalValue;

            // Append the character to the text content
            outputText = outputText + character;
        }
        return outputText.toString();
    }
}
