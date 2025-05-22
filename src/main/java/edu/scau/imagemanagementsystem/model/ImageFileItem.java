package edu.scau.imagemanagementsystem.model;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * 代表一个图片文件项，包含文件对象、名称、缩略图和选中状态的 JavaFX 属性。
 */
public class ImageFileItem {
    private final File file; // 原始文件对象
    private final StringProperty name; // 文件名属性
    private final ObjectProperty<Image> thumbnail; // 缩略图属性 (异步加载)
    private final BooleanProperty selected; // 选中状态属性

    /**
     * 构造一个 ImageFileItem 对象。
     *
     * @param file 关联的图片文件
     */
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