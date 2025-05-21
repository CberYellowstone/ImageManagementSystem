package edu.scau.imagemanagementsystem.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    public static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

    public static boolean isImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String extension = getFileExtension(file.getName());
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    public static String generatePaddedNumber(int number, int digits) {
        return String.format("%0" + digits + "d", number);
    }

    public static File findNonConflictingFileName(File targetDirectory, String baseName, String extension) {
        File newFile;
        if (extension.isEmpty()) {
            newFile = new File(targetDirectory, baseName);
        } else {
            newFile = new File(targetDirectory, baseName + "." + extension);
        }

        if (!newFile.exists()) {
            return newFile;
        }

        int counter = 1;
        while (true) {
            String numberedBaseName = baseName + "_" + counter;
            if (extension.isEmpty()) {
                newFile = new File(targetDirectory, numberedBaseName);
            } else {
                newFile = new File(targetDirectory, numberedBaseName + "." + extension);
            }
            if (!newFile.exists()) {
                return newFile;
            }
            counter++;
        }
    }
}