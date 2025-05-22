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

    /**
     * 将文件列表复制到内部剪贴板。
     * 如果传入的列表为 null，则清空剪贴板。
     *
     * @param files 要复制的文件列表，可以为 null
     */
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

    /**
     * 获取当前复制到剪贴板的文件列表的不可修改视图。
     *
     * @return 不可修改的文件列表；如果剪贴板为空或未初始化，则返回空列表。
     */
    public List<File> getCopiedFiles() {
        return copiedFilesProperty.get() == null ? Collections.emptyList()
                : Collections.unmodifiableList(copiedFilesProperty.get());
    }

    /**
     * 检查剪贴板中是否有文件。
     *
     * @return 如果剪贴板中有文件则返回 true，否则返回 false。
     */
    public boolean hasFiles() {
        return copiedFilesProperty.get() != null && !copiedFilesProperty.get().isEmpty();
    }

    /**
     * 清空剪贴板。
     */
    public void clear() {
        if (copiedFilesProperty.get() != null) {
            copiedFilesProperty.get().clear();
        }
        // To ensure listeners are notified of a structural change (emptiness)
        copiedFilesProperty.set(FXCollections.observableArrayList());
    }

    /**
     * 返回包含复制文件列表的 ObjectProperty。
     * 这允许外部组件监听剪贴板内容的变化。
     *
     * @return 复制文件列表的 ObjectProperty
     */
    public ObjectProperty<List<File>> copiedFilesProperty() {
        return copiedFilesProperty;
    }
}