package edu.scau.imagemanagementsystem.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

public class ClipboardService {
    // Using ObjectProperty to wrap the list, so listeners get notified when the
    // list reference changes.
    private final ObjectProperty<List<File>> copiedFilesProperty = new SimpleObjectProperty<>(
            FXCollections.observableArrayList());

    public void copyFiles(List<File> files) {
        if (files != null) {
            copiedFilesProperty.set(FXCollections.observableArrayList(new ArrayList<>(files))); // Create a new list
        } else {
            if (copiedFilesProperty.get() != null) {
                copiedFilesProperty.get().clear(); // Clear existing list if any
            }
            copiedFilesProperty.set(FXCollections.observableArrayList()); // Set to new empty list
        }
    }

    public List<File> getCopiedFiles() {
        return copiedFilesProperty.get() == null ? Collections.emptyList()
                : Collections.unmodifiableList(copiedFilesProperty.get());
    }

    public boolean hasFiles() {
        return copiedFilesProperty.get() != null && !copiedFilesProperty.get().isEmpty();
    }

    public void clear() {
        if (copiedFilesProperty.get() != null) {
            copiedFilesProperty.get().clear();
        }
        // To ensure listeners are notified of a structural change (emptiness)
        copiedFilesProperty.set(FXCollections.observableArrayList());
    }

    public ObjectProperty<List<File>> copiedFilesProperty() {
        return copiedFilesProperty;
    }
}