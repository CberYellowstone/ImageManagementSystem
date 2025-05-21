package edu.scau.imagemanagementsystem.controllers;

import java.io.File;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import edu.scau.imagemanagementsystem.utils.UiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class MetadataDialogController {

    @FXML
    private TextArea allMetadataArea;

    private File imageFile;

    public void initializeData(File file) {
        this.imageFile = file;
        loadMetadata();
    }

    private void loadMetadata() {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
            StringBuilder sb = new StringBuilder();
            for (Directory dir : metadata.getDirectories()) {
                sb.append("[").append(dir.getName()).append("]\n");
                for (Tag tag : dir.getTags()) {
                    sb.append(tag.getTagName()).append(" = ").append(tag.getDescription()).append("\n");
                }
                sb.append("\n");
            }
            allMetadataArea.setText(sb.toString());
        } catch (Exception e) {
            UiUtils.showErrorDialog("错误", "无法读取元数据", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) allMetadataArea.getScene().getWindow()).close();
    }
}