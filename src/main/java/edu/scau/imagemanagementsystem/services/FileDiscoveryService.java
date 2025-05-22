package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import edu.scau.imagemanagementsystem.utils.FileUtils;
import javafx.concurrent.Task;

public class FileDiscoveryService {

    /**
     * 异步获取指定父目录下的子目录列表。
     *
     * @param parentDir 父目录
     * @return 一个 Task，其结果为子目录列表。如果父目录无效或没有子目录，则列表为空。
     */
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

    /**
     * 异步获取指定目录下的图片文件列表。
     *
     * @param directory 目标目录
     * @return 一个 Task，其结果为图片文件列表。如果目录无效或没有图片文件，则列表为空。
     */
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

    /**
     * 异步计算指定图片文件列表的总大小。
     *
     * @param imageFiles 图片文件列表
     * @return 一个 Task，其结果为文件总大小（字节）。如果列表为 null，则返回 0。
     */
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