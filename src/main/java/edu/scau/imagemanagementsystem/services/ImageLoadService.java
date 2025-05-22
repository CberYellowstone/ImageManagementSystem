package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.io.FileInputStream;

import javafx.concurrent.Task;
import javafx.scene.image.Image;

public class ImageLoadService {

    /**
     * 异步加载指定图片文件的缩略图。
     *
     * @param imageFile 要加载缩略图的图片文件
     * @param reqWidth  请求的缩略图宽度
     * @param reqHeight 请求的缩略图高度
     * @return 一个 Task，其结果为加载的缩略图 Image 对象；如果文件无效或加载失败，则为 null。
     */
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

    /**
     * 异步加载指定图片文件的完整图像。
     *
     * @param imageFile 要加载的图片文件
     * @return 一个 Task，其结果为加载的完整 Image 对象；如果文件无效或加载失败，则为 null。
     */
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