package edu.scau.imagemanagementsystem.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileUtils {

    /**
     * 支持的图片文件扩展名列表。
     */
    public static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp");

    /**
     * 检查给定文件是否为支持的图片文件。
     *
     * @param file 要检查的文件
     * @return 如果文件是支持的图片文件则返回 true，否则返回 false
     */
    public static boolean isImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String extension = getFileExtension(file.getName());
        return SUPPORTED_EXTENSIONS.contains(extension.toLowerCase());
    }

    /**
     * 获取给定文件名的扩展名。
     *
     * @param fileName 文件名
     * @return 文件扩展名（不含点），如果文件名没有扩展名或为 null，则返回空字符串
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 获取给定文件名中不含扩展名的部分。
     *
     * @param fileName 文件名
     * @return 不含扩展名的文件名，如果文件名为 null，则返回空字符串
     */
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

    /**
     * 生成一个用零填充到指定位数的数字字符串。
     *
     * @param number 要格式化的数字
     * @param digits 结果字符串的总位数
     * @return 用零填充的数字字符串
     */
    public static String generatePaddedNumber(int number, int digits) {
        return String.format("%0" + digits + "d", number);
    }

    /**
     * 在目标目录中查找一个不冲突的文件名。
     * 如果 {@code baseName.extension} 已存在，则尝试 {@code baseName_1.extension},
     * {@code baseName_2.extension} 等，直到找到一个不存在的文件名。
     *
     * @param targetDirectory 目标目录
     * @param baseName        基本文件名（不含扩展名）
     * @param extension       文件扩展名（不含点），如果文件没有扩展名则为空字符串
     * @return 一个表示不冲突文件路径的 File 对象
     */
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