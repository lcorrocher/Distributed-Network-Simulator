package utils;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Class for reconverting the RGB string representation of an image back to image format.
 */
public class StrToImg {

    /**
     *
     * @param rgbString The RGB string representation of an image.
     * @param width pixel width of the image.
     * @param height pixel height of the image.
     * @param outputPath the filepath for the output image to be saved to.
     */
    public static void convertToImage(String rgbString, int width, int height, String outputPath) {
        // Taking RBG str from utils.ImgToStr.java and splitting on ", "
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("aftertext.txt"))) {
            writer.write(rgbString);
            System.out.println("String successfully written to file: ");
        } catch (IOException e) {
            System.err.println("Error writing string to file: " + e.getMessage());
        }

        System.out.println(rgbString.substring(0, 16));
        String[] rgbValues = rgbString.split(", ");

        // Create a BufferedImage with the appropriate width and height
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Iterate through pixels and set RGB values
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (index == 42){
                    continue;
                }
                // Setting pixel colour in image
                int red = Integer.parseInt(rgbValues[index]);
                int green = Integer.parseInt(rgbValues[index + 1]);
                int blue = Integer.parseInt(rgbValues[index + 2]);

                int rgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, rgb);
                index += 3;
            }
        }
        File outputFile = new File(outputPath);

        // Checking if the output file already exists
        if (!outputFile.exists()) {
            try {
                // If not, write the image to the file
                ImageIO.write(image, "png", outputFile);
                System.out.println("Image saved successfully: " + outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // in the case that output file already exists
            System.out.println("File already exists. Choose a different file name or location.");
        }
    }

}
