package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import edu.scau.imagemanagementsystem.utils.FileUtils;

import edu.scau.imagemanagementsystem.model.BatchRenameParams;
import javafx.concurrent.Task;

public class FileOperationService {

    public Task<Boolean> deleteFilesAsync(List<File> files) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (files == null)
                    return false;
                boolean allDeleted = true;
                for (File file : files) {
                    if (file.exists() && !file.delete()) {
                        allDeleted = false;
                        // Log or handle individual file deletion failure if necessary
                        System.err.println("Failed to delete: " + file.getAbsolutePath());
                    }
                }
                return allDeleted;
            }
        };
    }

    public Task<List<File>> pasteFilesAsync(List<File> filesToCopy, File targetDirectory) {
        return new Task<>() {
            @Override
            protected List<File> call() throws Exception {
                if (filesToCopy == null || targetDirectory == null || !targetDirectory.isDirectory()) {
                    return List.of();
                }
                List<File> pastedFiles = new ArrayList<>();
                for (File originalFile : filesToCopy) {
                    if (originalFile.exists()) {
                        String baseName = FileUtils.getFileNameWithoutExtension(originalFile.getName());
                        String extension = FileUtils.getFileExtension(originalFile.getName());
                        File destinationFile = FileUtils.findNonConflictingFileName(targetDirectory, baseName,
                                extension);
                        try {
                            Files.copy(originalFile.toPath(), destinationFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            pastedFiles.add(destinationFile);
                        } catch (IOException e) {
                            System.err.println("Failed to paste file: " + originalFile.getName() + " to "
                                    + destinationFile.getName());
                            e.printStackTrace();
                            // Continue pasting other files
                        }
                    }
                }
                return pastedFiles;
            }
        };
    }

    public Task<Boolean> renameFileAsync(File oldFile, String newFileNameBase) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (oldFile == null || !oldFile.exists() || newFileNameBase == null || newFileNameBase.isEmpty()) {
                    return false;
                }
                String extension = FileUtils.getFileExtension(oldFile.getName());
                String newName = newFileNameBase + (extension.isEmpty() ? "" : "." + extension);
                File newFile = new File(oldFile.getParentFile(), newName);

                if (newFile.exists()) {
                    System.err
                            .println("Rename failed: File with new name already exists: " + newFile.getAbsolutePath());
                    return false; // Or implement conflict resolution
                }
                return oldFile.renameTo(newFile);
            }
        };
    }

    public Task<Boolean> batchRenameFilesAsync(List<File> files, BatchRenameParams params) {
        return new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (files == null || params == null)
                    return false;
                boolean allRenamed = true;
                int currentNumber = params.getStartNumber();
                for (File file : files) {
                    if (file.exists()) {
                        String extension = FileUtils.getFileExtension(file.getName());
                        String newFileNameBase = params.getPrefix()
                                + FileUtils.generatePaddedNumber(currentNumber++, params.getNumDigits());
                        String newName = newFileNameBase + (extension.isEmpty() ? "" : "." + extension);
                        File newFile = new File(file.getParentFile(), newName);

                        if (newFile.exists() && !newFile.equals(file)) { // Avoid renaming to itself if name is the same
                            System.err.println("Batch rename skipped for " + file.getName() + ": Target file " + newName
                                    + " already exists.");
                            allRenamed = false;
                            continue; // Skip this file or implement more sophisticated conflict resolution
                        }

                        if (!file.renameTo(newFile)) {
                            allRenamed = false;
                            System.err.println(
                                    "Failed to rename: " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath());
                        }
                    }
                }
                return allRenamed;
            }
        };
    }
}