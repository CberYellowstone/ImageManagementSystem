package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.io.FileInputStream;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class ImageLoadService {

    public Task<Image> loadThumbnailAsync(File imageFile, double reqWidth, double reqHeight) {
        return new Task<>() {
            @Override
            protected Image call() throws Exception {
                if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
                    return null;
                }
                // Load image with requested width and height, preserving aspect ratio
                // The Image constructor with width/height and preserveRatio=true handles this.
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    return new Image(fis, reqWidth, reqHeight, true, true);
                } // smooth = true for better quality
            }
        };
    }

    public Task<Image> loadImageAsync(File imageFile) {
        return new Task<>() {
            @Override
            protected Image call() throws Exception {
                if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
                    return null;
                }
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    return new Image(fis); // Load full image
                }
            }
        };
    }
}