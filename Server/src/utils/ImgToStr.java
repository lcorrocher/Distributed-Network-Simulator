package utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for converting image to string (RGB pixel values)
 */
public class ImgToStr {
    BufferedImage img;
    private String filePath;

    /**
     * Constructor that takes the filepath of the image.
     *
     * @param filePath The file path of the image.
     */
    public ImgToStr(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Loading the image from the previously specified filepath.
     *
     * @return The loaded image.
     * @throws IOException If an I/O error occurs while loading the image.
     */
    public BufferedImage loadImage() throws IOException {
        File imageFile = new File(filePath);
        BufferedImage image = ImageIO.read(imageFile);

        img = image;
        return image;
    }

    /**
     * Converts the loaded image to a string representation of RGB pixel values.
     *
     * @return The string representation of RGB pixel values. Formatted as (255, 255, 255,...)
     * @throws IOException If an I/O error occurs during the conversion.
     */
    public String convertToStr(BufferedImage image) throws IOException, IOException{
        StringBuffer ImgStr = new StringBuffer();

        // Get image dimensions (e.g 600 x 400)
        int width = image.getWidth();
        int height = image.getHeight();

        // Iterate through pixels and extract RGB values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                ImgStr.append(red).append(", ").append(green).append(", ").append(blue).append(", ");
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("before.txt"))) {
            writer.write(ImgStr.toString());
            System.out.println("String successfully written to file: " + filePath);
        } catch (IOException e) {
            System.err.println("Error writing string to file: " + e.getMessage());
        }

        return ImgStr.toString();
    }

}

