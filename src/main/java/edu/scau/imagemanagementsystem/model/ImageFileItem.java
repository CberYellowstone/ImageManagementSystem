package edu.scau.imagemanagementsystem.model;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class ImageFileItem {
    private final File file;
    private final StringProperty name;
    private final ObjectProperty<Image> thumbnail; // For thumbnail image
    private final BooleanProperty selected;

    public ImageFileItem(File file) {
        this.file = file;
        this.name = new SimpleStringProperty(file.getName());
        this.thumbnail = new SimpleObjectProperty<>(null); // Thumbnail loaded asynchronously
        this.selected = new SimpleBooleanProperty(false);
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public Image getThumbnail() {
        return thumbnail.get();
    }

    public void setThumbnail(Image thumbnail) {
        this.thumbnail.set(thumbnail);
    }

    public ObjectProperty<Image> thumbnailProperty() {
        return thumbnail;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public String toString() {
        return getName(); // Useful for TreeView items if File objects are directly used initially
    }
}