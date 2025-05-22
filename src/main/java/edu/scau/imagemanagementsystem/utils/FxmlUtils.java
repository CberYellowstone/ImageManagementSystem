package edu.scau.imagemanagementsystem.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import edu.scau.imagemanagementsystem.controllers.MetadataDialogController;
import edu.scau.imagemanagementsystem.controllers.RenameDialogController;
import edu.scau.imagemanagementsystem.controllers.SlideshowController;
import edu.scau.imagemanagementsystem.model.BatchRenameParams;
import edu.scau.imagemanagementsystem.model.ImageFileItem;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class FxmlUtils {

    /**
     * 加载 FXML 文件并可选地初始化其控制器。
     *
     * @param fxmlPath              FXML 文件的路径
     * @param controllerInitializer 一个 Consumer，用于在加载后初始化控制器，可以为 null
     * @param <T>                   控制器的类型
     * @return 加载的 FXML 文件的控制器
     * @throws IOException 如果加载 FXML 文件失败
     */
    public static <T> T loadFxml(String fxmlPath, Consumer<Object> controllerInitializer) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(FxmlUtils.class.getResource(fxmlPath)));
        Object controller = loader.getController();
        if (controllerInitializer != null && controller != null) {
            controllerInitializer.accept(controller);
        }
        return (T) controller; // Return the controller instance
    }

    /**
     * 打开幻灯片播放窗口。
     *
     * @param images     要显示的图片列表
     * @param startImage 幻灯片开始时显示的图片
     * @param owner      父窗口，可以为 null
     */
    public static void openSlideshowWindow(List<ImageFileItem> images, ImageFileItem startImage, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects
                    .requireNonNull(
                            FxmlUtils.class.getResource("/edu/scau/imagemanagementsystem/fxml/SlideshowView.fxml")));
            Parent root = loader.load();
            SlideshowController controller = loader.getController();
            controller.initializeData(images, startImage);

            Stage slideshowStage = new Stage();
            slideshowStage.setTitle("幻灯片播放");
            slideshowStage.initModality(Modality.WINDOW_MODAL);
            if (owner != null) {
                slideshowStage.initOwner(owner);
            }
            slideshowStage.setScene(new Scene(root));
            slideshowStage.show();

            slideshowStage.setOnCloseRequest(event -> controller.shutdown());

        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showErrorDialog("错误", "无法打开幻灯片窗口", e.getMessage());
        }
    }

    /**
     * 打开批量重命名对话框。
     *
     * @param owner 父窗口，可以为 null
     * @return一个包含 BatchRenameParams 的 Optional，如果用户确认则包含参数，否则为空
     */
    public static Optional<BatchRenameParams> openBatchRenameDialog(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    FxmlUtils.class.getResource("/edu/scau/imagemanagementsystem/fxml/BatchRenameDialog.fxml")));
            Parent root = loader.load();
            RenameDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("批量重命名");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialogStage.initOwner(owner);
            }
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();

            return controller.getResult();
        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showErrorDialog("错误", "无法打开批量重命名窗口", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 打开元数据查看窗口
     *
     * @param imageFile 要查看元数据的图片文件
     * @param owner     父窗口，可以为 null
     */
    public static void openMetadataDialog(File imageFile, Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                    FxmlUtils.class.getResource("/edu/scau/imagemanagementsystem/fxml/MetadataDialog.fxml")));
            Parent root = loader.load();
            MetadataDialogController controller = loader.getController();
            controller.initializeData(imageFile);
            Stage dialog = new Stage();
            dialog.setTitle("查看元数据 - " + imageFile.getName());
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (owner != null) {
                dialog.initOwner(owner);
            }
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            UiUtils.showErrorDialog("错误", "无法打开元数据窗口", e.getMessage());
        }
    }
}