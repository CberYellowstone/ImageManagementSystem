package edu.scau.imagemanagementsystem.controllers;

import java.util.Optional;

import edu.scau.imagemanagementsystem.model.BatchRenameParams;
import edu.scau.imagemanagementsystem.utils.UiUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RenameDialogController {

    @FXML
    private TextField prefixField;
    @FXML
    private TextField startNumberField;
    @FXML
    private TextField digitsField;
    @FXML
    private Button okButton;

    private BatchRenameParams params;

    public Optional<BatchRenameParams> getResult() {
        return Optional.ofNullable(params);
    }

    @FXML
    private void initialize() {
        startNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                startNumberField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        digitsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                digitsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleOk() {
        String prefix = prefixField.getText();
        String startNumStr = startNumberField.getText();
        String digitsStr = digitsField.getText();

        if (prefix == null || prefix.trim().isEmpty()) {
            UiUtils.showErrorDialog("输入错误", "前缀不能为空", "请输入文件名前缀。");
            return;
        }

        if (startNumStr.isEmpty() || digitsStr.isEmpty()) {
            UiUtils.showErrorDialog("输入错误", "起始编号和位数不能为空", "请输入有效的数字。");
            return;
        }

        try {
            int startNumber = Integer.parseInt(startNumStr);
            int numDigits = Integer.parseInt(digitsStr);

            if (startNumber < 0) {
                UiUtils.showErrorDialog("输入错误", "起始编号不能为负数", "请输入一个非负的起始编号。");
                return;
            }
            if (numDigits <= 0 || numDigits > 10) {
                UiUtils.showErrorDialog("输入错误", "编号位数无效", "请输入一个介于1和10之间的编号位数。");
                return;
            }

            params = new BatchRenameParams(prefix.trim(), startNumber, numDigits);
            closeDialog();
        } catch (NumberFormatException e) {
            UiUtils.showErrorDialog("输入错误", "无效的数字格式", "起始编号和编号位数必须是有效的整数。");
        }
    }

    @FXML
    private void handleCancel() {
        params = null;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) prefixField.getScene().getWindow();
        stage.close();
    }
}