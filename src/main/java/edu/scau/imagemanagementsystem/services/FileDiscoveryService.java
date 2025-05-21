package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import edu.scau.imagemanagementsystem.utils.FileUtils;
import javafx.concurrent.Task;

public class FileDiscoveryService {

    public Task<List<File>> getSubDirectoriesAsync(File parentDir) {
        return new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                if (parentDir == null || !parentDir.isDirectory()) {
                    return List.of();
                }
                File[] dirs = parentDir.listFiles(File::isDirectory);
                return dirs == null ? List.of() : Arrays.asList(dirs);
            }
        };
    }

    public Task<List<File>> getImageFilesAsync(File directory) {
        return new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                if (directory == null || !directory.isDirectory()) {
                    return List.of();
                }
                File[] files = directory.listFiles(f -> f.isFile() && FileUtils.isImageFile(f));
                return files == null ? List.of() : Arrays.asList(files);
            }
        };
    }

    public Task<Long> calculateDirectorySizeAsync(List<File> imageFiles) {
        return new Task<>() {
            @Override
            protected Long call() throws Exception {
                if (imageFiles == null) {
                    return 0L;
                }
                return imageFiles.stream().mapToLong(File::length).sum();
            }
        };
    }
}