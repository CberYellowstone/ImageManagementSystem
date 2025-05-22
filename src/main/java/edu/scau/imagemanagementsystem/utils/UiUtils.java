package edu.scau.imagemanagementsystem.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class UiUtils {

    /**
     * 显示一个确认对话框。
     *
     * @param title   对话框的标题
     * @param header  对话框的头部文本
     * @param content 对话框的内容文本
     * @return 一个包含用户选择的 ButtonType 的 Optional，如果用户关闭对话框则为空
     */
    public static Optional<ButtonType> showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    /**
     * 显示一个错误对话框。
     *
     * @param title   对话框的标题
     * @param header  对话框的头部文本
     * @param content 对话框的内容文本
     */
    public static void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 显示一个包含异常详细信息的错误对话框。
     *
     * @param title   对话框的标题
     * @param header  对话框的头部文本
     * @param content 对话框的内容文本
     * @param ex      要显示的异常
     */
    public static void showErrorDialog(String title, String header, String content, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * 显示一个文本输入对话框。
     *
     * @param title        对话框的标题
     * @param header       对话框的头部文本
     * @param content      对话框的内容文本
     * @param defaultValue 输入字段的默认值
     * @return 一个包含用户输入的字符串的 Optional，如果用户取消则为空
     */
    public static Optional<String> showTextInputDialog(String title, String header, String content,
            String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }
}