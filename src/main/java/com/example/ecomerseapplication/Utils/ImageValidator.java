package com.example.ecomerseapplication.Utils;

import com.example.ecomerseapplication.ExceptionHandling.CustomExceptions.InvalidImageException;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageValidator {

    public static BufferedImage validateImageInput(MultipartFile image) throws IOException {
        if (image.isEmpty()) {
            throw new InvalidImageException("No image for inference!", "empty");
        }

        if (image.getContentType() == null ||
                !image.getContentType().startsWith("image/")) {
            throw new InvalidImageException("Content type for inference is not an image!", "contentType");
        }

        BufferedImage bufferedImage =
                ImageIO.read(image.getInputStream());

        if (bufferedImage == null) {
            throw new InvalidImageException("Decoding of the image for inference failed", "decoding");
        }

        if (bufferedImage.getWidth() < 50 ||
                bufferedImage.getHeight() < 50) {
            throw new InvalidImageException("Image too small", "size-small");
        }
        if (bufferedImage.getWidth() > 8000 ||
                bufferedImage.getHeight() > 8000)
            throw new InvalidImageException("Image too large", "size-large");

        return bufferedImage;
    }
}
