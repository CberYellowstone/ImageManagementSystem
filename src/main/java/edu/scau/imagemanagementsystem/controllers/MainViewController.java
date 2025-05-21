package edu.scau.imagemanagementsystem.controllers;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scau.imagemanagementsystem.model.BatchRenameParams;
import edu.scau.imagemanagementsystem.model.ImageFileItem;
import edu.scau.imagemanagementsystem.services.FileDiscoveryService;
import edu.scau.imagemanagementsystem.services.FileOperationService;
import edu.scau.imagemanagementsystem.services.ImageLoadService;
import edu.scau.imagemanagementsystem.utils.FileUtils;
import edu.scau.imagemanagementsystem.utils.FxmlUtils;
import edu.scau.imagemanagementsystem.utils.UiUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.PopupWindow;
import javafx.stage.Window;

public class MainViewController {
    private static final Logger logger = LoggerFactory.getLogger(MainViewController.class);
    private final FileOperationService fileOperationService = new FileOperationService();
    private final FileDiscoveryService fileDiscoveryService = new FileDiscoveryService();
    private final ImageLoadService imageLoadService = new ImageLoadService();
    private final edu.scau.imagemanagementsystem.services.ClipboardService clipboardService = new edu.scau.imagemanagementsystem.services.ClipboardService();

    private ObservableList<ImageFileItem> selectedImageItems = FXCollections.observableArrayList();
    private ObservableList<ImageFileItem> imageFileItemObservableList = FXCollections.observableArrayList();

    @FXML
    private TreeView<File> directoryTreeView;
    @FXML
    private FlowPane imagePreviewPane;
    @FXML
    private ScrollPane imageScrollPane;
    @FXML
    private Label currentDirectoryLabel;
    @FXML
    private Label imageCountInDirLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Button slideshowButton;
    @FXML
    private AnchorPane imagePreviewAnchorPane;

    // 用于空白区域的粘贴菜单
    private ContextMenu blankContextMenu;

    private ImageFileItem lastSelectedItemForShift;
    private long currentDirectoryTotalSizeInBytes = 0L;

    private static final double THUMBNAIL_WIDTH = 120;
    private static final double THUMBNAIL_HEIGHT = 120;

    // 添加搜索与排序控件引用
    @FXML
    private TextField searchTextField;
    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private void initialize() {
        initializeDirectoryTree();
        // 设置预览面板缩略图和文件夹排列约束
        imagePreviewPane.setHgap(10);
        imagePreviewPane.setVgap(10);
        imagePreviewPane.setPadding(new Insets(10));
        imagePreviewPane.setAlignment(Pos.TOP_LEFT);
        // 根据 ScrollPane 宽度自动换行
        imagePreviewPane.prefWrapLengthProperty().bind(imageScrollPane.widthProperty());

        imageScrollPane.setOnMouseClicked(event -> {
            if (event.getTarget() == imageScrollPane || event.getTarget() == imagePreviewPane) {
                clearSelection();
                updateStatusLabel();
                // 清除目录项高亮
                for (Node child : imagePreviewPane.getChildren()) {
                    child.getStyleClass().remove("thumbnail-selected");
                }
            }
        });

        slideshowButton.setOnAction(event -> handleSlideshowButtonAction());
        // 支持在目录树中多选，以显示路径上所有节点的高亮
        directoryTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        updateStatusLabel();
        // 初始化空白区域的上下文菜单，复用原始菜单样式，仅保留粘贴和刷新
        blankContextMenu = createThumbnailContextMenu(null);
        // 禁用除"粘贴"和"刷新"之外的菜单项，使样式一致
        for (MenuItem mi : blankContextMenu.getItems()) {
            if (!"粘贴".equals(mi.getText()) && !"刷新".equals(mi.getText())) {
                mi.setDisable(true);
            }
        }
        // 空白处右键：先隐藏所有弹窗，再显示此菜单
        imagePreviewPane.setOnContextMenuRequested(event -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof PopupWindow) {
                    ((PopupWindow) w).hide();
                }
            }
            blankContextMenu.show(imagePreviewPane, event.getScreenX(), event.getScreenY());
        });

        // 高级搜索
        searchTextField.textProperty().addListener((obs, old, ne) -> filterAndSort());
        // 排序选项
        sortComboBox.getItems().addAll("名称升序", "名称降序", "大小升序", "大小降序", "日期升序", "日期降序");
        sortComboBox.getSelectionModel().selectFirst();
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, ne) -> filterAndSort());
        // 键盘快捷键
        Platform.runLater(() -> {
            Scene scene = directoryTreeView.getScene();
            if (scene != null) {
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.DELETE), this::handleDeleteSelected);
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                        this::handleCopySelected);
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN),
                        this::handlePasteSelected);
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F2), this::handleRenameSelected);
                scene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                        () -> searchTextField.requestFocus());
            }
        });
    }

    private String formatFileSize(long size) {
        if (size <= 0)
            return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private void initializeDirectoryTree() {
        TreeItem<File> rootNode = new TreeItem<>(new File("计算机"));
        rootNode.setExpanded(true);

        File[] roots = File.listRoots();
        for (File rootDrive : roots) {
            TreeItem<File> driveItem = createDirectoryTreeItem(rootDrive);
            rootNode.getChildren().add(driveItem);
        }

        directoryTreeView.setRoot(rootNode);
        directoryTreeView.setShowRoot(true);

        // 使用 TreeItem.toString() 显示文件/目录名称，避免 File.toString() 全路径
        directoryTreeView.setCellFactory(tv -> new TreeCell<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TreeItem<File> treeItem = getTreeItem();
                    if (treeItem.getParent() == null) {
                        // 根节点特殊显示
                        setText("计算机");
                    } else {
                        // 其他节点使用 TreeItem.toString()
                        setText(treeItem.toString());
                    }
                }
            }
        });

        directoryTreeView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.getValue() != null) {
                        File selectedDir = newValue.getValue();
                        if (selectedDir.isDirectory()) {
                            onDirectorySelected(selectedDir);
                        }
                    } else {
                        imagePreviewPane.getChildren().clear();
                        imageFileItemObservableList.clear();
                        selectedImageItems.clear();
                        currentDirectoryLabel.setText("当前目录:");
                        imageCountInDirLabel.setText("0 张图片");
                        currentDirectoryTotalSizeInBytes = 0L;
                        updateStatusLabel();
                    }
                });
    }

    private TreeItem<File> createDirectoryTreeItem(File directory) {
        TreeItem<File> item = new TreeItem<>(directory) {
            @Override
            public String toString() {
                return getValue() == null ? ""
                        : (getValue().getName().isEmpty() ? getValue().getAbsolutePath() : getValue().getName());
            }
        };

        if (directory.isDirectory()) {
            File[] subDirs = directory.listFiles(file -> file.isDirectory());
            if (subDirs != null && subDirs.length > 0) {
                item.getChildren().add(new TreeItem<>());
            }

            item.expandedProperty().addListener((obs, wasExpanded, isNowExpanded) -> {
                if (isNowExpanded && item.getChildren().size() == 1 &&
                        item.getChildren().get(0).getValue() == null) {
                    item.getChildren().clear();
                    loadSubDirectories(item);
                }
            });
        }
        return item;
    }

    private void loadSubDirectories(TreeItem<File> parentItem) {
        File parentDir = parentItem.getValue();
        if (parentDir == null || !parentDir.isDirectory()) {
            return;
        }

        Task<List<File>> subDirTask = fileDiscoveryService.getSubDirectoriesAsync(parentDir);
        subDirTask.setOnSucceeded(event -> {
            List<File> subDirs = subDirTask.getValue();
            if (subDirs != null) {
                for (File dir : subDirs) {
                    parentItem.getChildren().add(createDirectoryTreeItem(dir));
                }
            }
        });
        subDirTask.setOnFailed(event -> {
            logger.error("Failed to load subdirectories for: " + parentDir.getAbsolutePath(),
                    subDirTask.getException());
        });
        new Thread(subDirTask).start();
    }

    private void onDirectorySelected(File directory) {
        logger.info("Directory selected: {}", directory.getAbsolutePath());
        currentDirectoryLabel.setText("当前目录: " + directory.getAbsolutePath());
        // 先清空旧的选中与数据，并展示子目录
        selectedImageItems.clear();
        imageFileItemObservableList.clear();
        imagePreviewPane.getChildren().clear();
        File[] subDirs = directory.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                createAndAddDirectoryNode(subDir);
            }
        }
        Task<List<File>> imageFilesTask = fileDiscoveryService.getImageFilesAsync(directory);

        imageFilesTask.setOnSucceeded(e -> {
            List<File> imageFiles = imageFilesTask.getValue();
            Platform.runLater(() -> {
                if (imageFiles != null) {
                    imageFileItemObservableList.clear();
                    for (File imageFile : imageFiles) {
                        ImageFileItem item = new ImageFileItem(imageFile);
                        imageFileItemObservableList.add(item);
                    }
                    // 过滤并排序后渲染缩略图
                    filterAndSort();
                    imageCountInDirLabel.setText(imageFiles.size() + " 张图片");
                    calculateAndDisplayTotalSize(imageFiles);
                } else {
                    imageCountInDirLabel.setText("0 张图片");
                }
                updateStatusLabel();
            });
        });

        imageFilesTask.setOnFailed(e -> {
            Throwable ex = imageFilesTask.getException();
            logger.error("Failed to load image files for directory: {}", directory.getAbsolutePath(), ex);
            Platform.runLater(() -> {
                UiUtils.showErrorDialog("加载图片失败", "无法加载目录中的图片文件。", ex != null ? ex.getMessage() : "");
                imageCountInDirLabel.setText("加载错误");
                updateStatusLabel();
            });
        });
        new Thread(imageFilesTask).start();
    }

    private void createAndAddThumbnailNode(ImageFileItem item) {
        ImageView thumbnailView = new ImageView();
        thumbnailView.setFitWidth(THUMBNAIL_WIDTH);
        thumbnailView.setFitHeight(THUMBNAIL_HEIGHT);
        thumbnailView.setPreserveRatio(true);

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        // 当名称过长时在悬停时显示完整名称
        nameLabel.setTooltip(new javafx.scene.control.Tooltip(item.getName()));
        // 固定宽度并居中显示文字
        nameLabel.setPrefWidth(THUMBNAIL_WIDTH);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        HBox imageContainer = new HBox(thumbnailView);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setMinHeight(THUMBNAIL_HEIGHT);

        VBox container = new VBox(5, imageContainer, nameLabel);
        container.setAlignment(Pos.CENTER);
        container.setUserData(item);
        container.setPadding(new Insets(5));
        // 设置固定尺寸，保证网格对齐
        container.setPrefWidth(THUMBNAIL_WIDTH + 20);
        container.setPrefHeight(THUMBNAIL_HEIGHT + 40);

        if (item.getThumbnail() != null) {
            thumbnailView.setImage(item.getThumbnail());
        } else {
            Task<Image> loadThumbnailTask = imageLoadService.loadThumbnailAsync(item.getFile(), THUMBNAIL_WIDTH,
                    THUMBNAIL_HEIGHT);
            loadThumbnailTask.setOnSucceeded(event -> {
                Image thumbnail = loadThumbnailTask.getValue();
                item.setThumbnail(thumbnail);
                if (thumbnail != null) {
                    Platform.runLater(() -> thumbnailView.setImage(thumbnail));
                }
            });
            loadThumbnailTask.setOnFailed(event -> {
                Throwable ex = loadThumbnailTask.getException();
                logger.error("Failed to load thumbnail for {}", item.getName(), ex);
            });
            new Thread(loadThumbnailTask).start();
        }

        item.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                container.getStyleClass().add("thumbnail-selected");
            } else {
                container.getStyleClass().remove("thumbnail-selected");
            }
        });

        container.setOnMouseClicked(event -> handleThumbnailClick(event, item, container));

        ContextMenu contextMenu = createThumbnailContextMenu(item);
        container.setOnContextMenuRequested(event -> {
            // 隐藏所有现有弹窗，确保只显示一个菜单
            for (Window w : Window.getWindows()) {
                if (w instanceof PopupWindow) {
                    ((PopupWindow) w).hide();
                }
            }
            if (selectedImageItems.contains(item)) {
                contextMenu.show(container, event.getScreenX(), event.getScreenY());
            } else {
                clearSelection();
                toggleItemSelection(item);
                contextMenu.show(container, event.getScreenX(), event.getScreenY());
            }
            event.consume();
        });

        imagePreviewPane.getChildren().add(container);
    }

    private void handleThumbnailClick(MouseEvent event, ImageFileItem item, VBox container) {
        if (event.getButton() == MouseButton.PRIMARY) {
            if (event.getClickCount() == 1) {
                if (event.isShiftDown() && event.isControlDown()) {
                    if (lastSelectedItemForShift != null
                            && imageFileItemObservableList.contains(lastSelectedItemForShift)
                            && imageFileItemObservableList.contains(item)) {
                        int anchorIndex = imageFileItemObservableList.indexOf(lastSelectedItemForShift);
                        int clickedIndex = imageFileItemObservableList.indexOf(item);
                        int startIndex = Math.min(anchorIndex, clickedIndex);
                        int endIndex = Math.max(anchorIndex, clickedIndex);
                        for (int i = startIndex; i <= endIndex; i++) {
                            ImageFileItem itemInRange = imageFileItemObservableList.get(i);
                            if (!selectedImageItems.contains(itemInRange)) {
                                toggleItemSelection(itemInRange);
                            }
                        }
                    } else {
                        toggleItemSelection(item);
                        lastSelectedItemForShift = item;
                    }
                } else if (event.isControlDown()) {
                    toggleItemSelection(item);
                    lastSelectedItemForShift = item;
                } else if (event.isShiftDown()) {
                    clearSelection();
                    if (lastSelectedItemForShift != null
                            && imageFileItemObservableList.contains(lastSelectedItemForShift)
                            && imageFileItemObservableList.contains(item)) {
                        int anchorIndex = imageFileItemObservableList.indexOf(lastSelectedItemForShift);
                        int clickedIndex = imageFileItemObservableList.indexOf(item);
                        int startIndex = Math.min(anchorIndex, clickedIndex);
                        int endIndex = Math.max(anchorIndex, clickedIndex);
                        for (int i = startIndex; i <= endIndex; i++) {
                            toggleItemSelection(imageFileItemObservableList.get(i));
                        }
                    } else {
                        toggleItemSelection(item);
                        lastSelectedItemForShift = item;
                    }
                } else {
                    clearSelection();
                    toggleItemSelection(item);
                    lastSelectedItemForShift = item;
                }
            } else if (event.getClickCount() == 2) {
                handleThumbnailDoubleClick(item);
            }
        }
        updateStatusLabel();
        event.consume();
    }

    private void toggleItemSelection(ImageFileItem item) {
        item.setSelected(!item.isSelected());
        if (item.isSelected()) {
            if (!selectedImageItems.contains(item)) {
                selectedImageItems.add(item);
            }
        } else {
            selectedImageItems.remove(item);
        }
    }

    private void clearSelection() {
        for (ImageFileItem listItem : new ArrayList<>(selectedImageItems)) {
            listItem.setSelected(false);
        }
        selectedImageItems.clear();
    }

    private void calculateAndDisplayTotalSize(List<File> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            currentDirectoryTotalSizeInBytes = 0L;
            return;
        }
        Task<Long> calculateSizeTask = fileDiscoveryService.calculateDirectorySizeAsync(imageFiles);
        calculateSizeTask.setOnSucceeded(e -> {
            Long totalSize = calculateSizeTask.getValue();
            currentDirectoryTotalSizeInBytes = (totalSize != null) ? totalSize : 0L;
            Platform.runLater(() -> {
                logger.info("Total size of images in directory: {}", formatFileSize(currentDirectoryTotalSizeInBytes));
                updateStatusLabel();
            });
        });
        calculateSizeTask.setOnFailed(e -> {
            Throwable ex = calculateSizeTask.getException();
            logger.error("Failed to calculate directory size.", ex);
            currentDirectoryTotalSizeInBytes = 0L;
            Platform.runLater(this::updateStatusLabel);
        });
        new Thread(calculateSizeTask).start();
    }

    private void updateStatusLabel() {
        long totalSizeOfSelected = 0;
        for (ImageFileItem item : selectedImageItems) {
            totalSizeOfSelected += item.getFile().length();
        }

        String statusText;
        if (!selectedImageItems.isEmpty()) {
            statusText = String.format("选中 %d 张图片 (共 %s)", selectedImageItems.size(),
                    formatFileSize(totalSizeOfSelected));
        } else {
            statusText = String.format("%d 张图片 (共 %s)", imageFileItemObservableList.size(),
                    formatFileSize(currentDirectoryTotalSizeInBytes));
        }
        statusLabel.setText(statusText);
    }

    private void handleThumbnailDoubleClick(ImageFileItem item) {
        logger.info("Thumbnail double-clicked: {}", item.getName());
        if (imageFileItemObservableList.isEmpty())
            return;
        Window owner = imagePreviewPane.getScene().getWindow();
        FxmlUtils.openSlideshowWindow(new ArrayList<>(imageFileItemObservableList), item, owner);
    }

    @FXML
    private void handleSlideshowButtonAction() {
        logger.info("Slideshow button clicked.");
        if (imageFileItemObservableList.isEmpty()) {
            UiUtils.showErrorDialog("提示", "没有图片可播放", "当前目录没有图片或未选择目录。");
            return;
        }
        Window owner = imagePreviewPane.getScene().getWindow();
        FxmlUtils.openSlideshowWindow(new ArrayList<>(imageFileItemObservableList), imageFileItemObservableList.get(0),
                owner);
    }

    private ContextMenu createThumbnailContextMenu(ImageFileItem item) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("删除");
        deleteItem.setOnAction(e -> handleDeleteSelected());

        MenuItem copyItem = new MenuItem("复制");
        copyItem.setOnAction(e -> handleCopySelected());

        MenuItem renameItem = new MenuItem("重命名");
        renameItem.setOnAction(e -> handleRenameSelected());

        MenuItem pasteItem = new MenuItem("粘贴");
        pasteItem.setOnAction(e -> handlePasteSelected());

        // 新增刷新选项
        MenuItem refreshItem = new MenuItem("刷新");
        refreshItem.setOnAction(e -> {
            TreeItem<File> selectedDirItem = directoryTreeView.getSelectionModel().getSelectedItem();
            if (selectedDirItem != null && selectedDirItem.getValue() != null) {
                onDirectorySelected(selectedDirItem.getValue());
            }
        });

        contextMenu.getItems().addAll(deleteItem, copyItem, renameItem);
        if (item != null) {
            MenuItem viewMetaItem = new MenuItem("查看元数据");
            viewMetaItem.setOnAction(e -> handleViewMetadata(item));
            contextMenu.getItems().add(viewMetaItem);
        }
        contextMenu.getItems().addAll(pasteItem, refreshItem);
        // 每次显示时更新粘贴按钮的可用状态
        contextMenu.setOnShowing(event -> {
            boolean dirSelected = directoryTreeView.getSelectionModel().getSelectedItem() != null;
            pasteItem.setDisable(!clipboardService.hasFiles() || !dirSelected);
            refreshItem.setDisable(!dirSelected);
        });
        return contextMenu;
    }

    @FXML
    private void handleDeleteSelected() {
        if (selectedImageItems.isEmpty()) {
            UiUtils.showErrorDialog("删除失败", "没有选中的图片", "请先选择要删除的图片。");
            return;
        }

        Optional<ButtonType> result = UiUtils.showConfirmationDialog("确认删除",
                "确定要删除选中的 " + selectedImageItems.size() + " 张图片吗?",
                "此操作无法撤销。");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            List<File> filesToDelete = selectedImageItems.stream().map(ImageFileItem::getFile)
                    .collect(Collectors.toList());
            Task<Boolean> deleteTask = fileOperationService.deleteFilesAsync(filesToDelete);
            deleteTask.setOnSucceeded(e -> {
                boolean success = deleteTask.getValue();
                Platform.runLater(() -> {
                    if (success) {
                        logger.info("{} files deleted successfully.", filesToDelete.size());
                        TreeItem<File> selectedDirItem = directoryTreeView.getSelectionModel().getSelectedItem();
                        if (selectedDirItem != null) {
                            onDirectorySelected(selectedDirItem.getValue());
                        }
                    } else {
                        UiUtils.showErrorDialog("删除失败", "部分或全部图片删除失败。", "请检查文件权限或文件是否被占用。");
                        TreeItem<File> selectedDirItem = directoryTreeView.getSelectionModel().getSelectedItem();
                        if (selectedDirItem != null) {
                            onDirectorySelected(selectedDirItem.getValue());
                        }
                    }
                });
            });
            deleteTask.setOnFailed(e -> {
                Throwable ex = deleteTask.getException();
                Platform.runLater(
                        () -> UiUtils.showErrorDialog("删除错误", "删除操作执行失败。", ex != null ? ex.getMessage() : ""));
            });
            new Thread(deleteTask).start();
        }
    }

    @FXML
    private void handleCopySelected() {
        if (selectedImageItems.isEmpty()) {
            UiUtils.showErrorDialog("复制失败", "没有选中的图片", "请先选择要复制的图片。");
            return;
        }
        List<File> filesToCopy = selectedImageItems.stream().map(ImageFileItem::getFile).collect(Collectors.toList());
        clipboardService.copyFiles(filesToCopy);
        statusLabel.setText("已复制 " + filesToCopy.size() + " 张图片。");
        logger.info("Copied {} files to internal clipboard.", filesToCopy.size());
    }

    @FXML
    private void handlePasteSelected() {
        TreeItem<File> selectedDirItem = directoryTreeView.getSelectionModel().getSelectedItem();
        if (selectedDirItem == null || selectedDirItem.getValue() == null) {
            UiUtils.showErrorDialog("粘贴失败", "未选择目标目录", "请先在目录树中选择一个目标目录。");
            return;
        }
        File targetDirectory = selectedDirItem.getValue();
        if (!clipboardService.hasFiles()) {
            UiUtils.showErrorDialog("粘贴失败", "剪贴板为空", "剪贴板中没有可粘贴的文件。");
            return;
        }

        List<File> filesToPaste = clipboardService.getCopiedFiles();
        Task<List<File>> pasteTask = fileOperationService.pasteFilesAsync(filesToPaste, targetDirectory);

        pasteTask.setOnSucceeded(e -> {
            List<File> pastedFiles = pasteTask.getValue();
            Platform.runLater(() -> {
                logger.info("Pasted {} files to {}", pastedFiles.size(), targetDirectory.getAbsolutePath());
                onDirectorySelected(targetDirectory);
                statusLabel.setText("已粘贴 " + pastedFiles.size() + " 张图片到 " + targetDirectory.getName());
            });
        });
        pasteTask.setOnFailed(e -> {
            Throwable ex = pasteTask.getException();
            Platform.runLater(
                    () -> UiUtils.showErrorDialog("粘贴错误", "粘贴操作执行失败。", ex != null ? ex.getMessage() : ""));
        });
        new Thread(pasteTask).start();
    }

    @FXML
    private void handleRenameSelected() {
        if (selectedImageItems.isEmpty()) {
            UiUtils.showErrorDialog("重命名失败", "没有选中的图片", "请先选择要重命名的图片。");
            return;
        }

        if (selectedImageItems.size() == 1) {
            ImageFileItem itemToRename = selectedImageItems.get(0);
            Optional<String> newNameBaseOpt = UiUtils.showTextInputDialog("重命名图片",
                    "输入新的文件名 (不含扩展名):",
                    "当前文件名: " + FileUtils.getFileNameWithoutExtension(itemToRename.getName()),
                    FileUtils.getFileNameWithoutExtension(itemToRename.getName()));

            if (newNameBaseOpt.isPresent() && !newNameBaseOpt.get().trim().isEmpty()) {
                String newNameBase = newNameBaseOpt.get().trim();
                Task<Boolean> renameTask = fileOperationService.renameFileAsync(itemToRename.getFile(), newNameBase);
                renameTask.setOnSucceeded(e -> {
                    boolean success = renameTask.getValue();
                    Platform.runLater(() -> {
                        if (success) {
                            logger.info("File {} renamed successfully to {}.{}", itemToRename.getName(), newNameBase,
                                    FileUtils.getFileExtension(itemToRename.getName()));
                            onDirectorySelected(itemToRename.getFile().getParentFile());
                        } else {
                            UiUtils.showErrorDialog("重命名失败", "无法重命名文件。", "可能是文件名已存在或无效。");
                        }
                    });
                });
                renameTask.setOnFailed(e -> {
                    Throwable ex = renameTask.getException();
                    Platform.runLater(() -> UiUtils.showErrorDialog("重命名错误", "重命名操作执行失败。",
                            ex != null ? ex.getMessage() : ""));
                });
                new Thread(renameTask).start();
            }
        } else {
            Optional<BatchRenameParams> paramsOpt = FxmlUtils
                    .openBatchRenameDialog(imagePreviewPane.getScene().getWindow());
            if (paramsOpt.isPresent()) {
                BatchRenameParams params = paramsOpt.get();
                List<File> filesToRename = selectedImageItems.stream().map(ImageFileItem::getFile)
                        .collect(Collectors.toList());
                Task<Boolean> batchRenameTask = fileOperationService.batchRenameFilesAsync(filesToRename, params);
                batchRenameTask.setOnSucceeded(e -> {
                    boolean success = batchRenameTask.getValue();
                    Platform.runLater(() -> {
                        logger.info("Batch rename task completed. Success: {}", success);
                        if (!filesToRename.isEmpty()) {
                            onDirectorySelected(filesToRename.get(0).getParentFile());
                        }
                        if (!success) {
                            UiUtils.showErrorDialog("批量重命名", "部分或全部文件重命名失败/跳过。", "请检查控制台日志获取详情。");
                        }
                    });
                });
                batchRenameTask.setOnFailed(e -> {
                    Throwable ex = batchRenameTask.getException();
                    Platform.runLater(() -> UiUtils.showErrorDialog("批量重命名错误", "批量重命名操作执行失败。",
                            ex != null ? ex.getMessage() : ""));
                });
                new Thread(batchRenameTask).start();
            }
        }
    }

    /**
     * 为子目录创建目录项，使用文件夹表情图标并支持双击进入。
     */
    private void createAndAddDirectoryNode(File dir) {
        // 使用文件夹Emoji作为图标
        Label iconLabel = new Label("\uD83D\uDCC1");
        iconLabel.setStyle("-fx-font-size:48pt;");
        HBox iconContainer = new HBox(iconLabel);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setMinHeight(THUMBNAIL_HEIGHT);

        Label nameLabel = new Label(dir.getName());
        nameLabel.setWrapText(true);
        // 当名称过长时在悬停时显示完整名称
        nameLabel.setTooltip(new javafx.scene.control.Tooltip(dir.getName()));
        // 固定宽度并居中显示文字
        nameLabel.setPrefWidth(THUMBNAIL_WIDTH);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setTextAlignment(TextAlignment.CENTER);

        VBox container = new VBox(5, iconContainer, nameLabel);
        container.setAlignment(Pos.CENTER);
        container.setUserData(dir);
        container.setPadding(new Insets(5));
        // 设置固定尺寸，保证网格对齐
        container.setPrefWidth(THUMBNAIL_WIDTH + 20);
        container.setPrefHeight(THUMBNAIL_HEIGHT + 40);
        container.getStyleClass().add("thumbnail-container");

        // 单击选中高亮；双击在树中选中以驱动刷新
        container.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getClickCount() == 1) {
                    // 高亮当前容器
                    clearSelection();
                    for (Node child : imagePreviewPane.getChildren()) {
                        child.getStyleClass().remove("thumbnail-selected");
                    }
                    container.getStyleClass().add("thumbnail-selected");
                } else if (event.getClickCount() == 2) {
                    // 只在树中选中，触发 onDirectorySelected
                    selectDirectoryInTree(dir);
                }
            }
            event.consume();
        });
        // 仅显示粘贴与刷新
        ContextMenu contextMenu = createThumbnailContextMenu(null);
        for (MenuItem mi : contextMenu.getItems()) {
            String text = mi.getText();
            if (!"粘贴".equals(text) && !"刷新".equals(text)) {
                mi.setDisable(true);
            }
        }
        container.setOnContextMenuRequested(event -> {
            for (Window w : Window.getWindows()) {
                if (w instanceof PopupWindow) {
                    ((PopupWindow) w).hide();
                }
            }
            contextMenu.show(container, event.getScreenX(), event.getScreenY());
            event.consume();
        });
        imagePreviewPane.getChildren().add(container);
    }

    /**
     * 在目录树中查找并选中指定目录，展开路径。
     */
    private void selectDirectoryInTree(File directory) {
        // 从虚拟根开始
        TreeItem<File> current = directoryTreeView.getRoot();
        // 构建从目标目录到根驱动的路径列表并反转，便于逐级匹配
        List<File> path = new ArrayList<>();
        for (File f = directory; f != null; f = f.getParentFile()) {
            path.add(f);
        }
        Collections.reverse(path);
        // 逐级查找并展开，若缺少节点则添加
        for (File p : path) {
            TreeItem<File> next = null;
            for (TreeItem<File> child : current.getChildren()) {
                if (p.equals(child.getValue())) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                // 若不存在则创建新节点
                next = createDirectoryTreeItem(p);
                current.getChildren().add(next);
            }
            current = next;
            current.setExpanded(true);
        }
        // 选中目标节点，触发右侧刷新
        directoryTreeView.getSelectionModel().select(current);
    }

    // 查看元数据
    private void handleViewMetadata(ImageFileItem item) {
        Window owner = imagePreviewPane.getScene().getWindow();
        FxmlUtils.openMetadataDialog(item.getFile(), owner);
    }

    // 过滤并排序渲染
    private void filterAndSort() {
        String keyword = searchTextField.getText();
        String lower = (keyword == null) ? "" : keyword.toLowerCase();
        String sortOpt = sortComboBox.getSelectionModel().getSelectedItem();
        // 保留目录节点
        java.util.List<Node> dirNodes = imagePreviewPane.getChildren().stream()
                .filter(n -> n.getUserData() instanceof File)
                .collect(Collectors.toList());
        imagePreviewPane.getChildren().clear();
        dirNodes.forEach(imagePreviewPane.getChildren()::add);
        // 过滤
        java.util.List<ImageFileItem> filtered = imageFileItemObservableList.stream()
                .filter(item -> item.getName().toLowerCase().contains(lower))
                .collect(Collectors.<ImageFileItem>toList());
        // 排序
        if (sortOpt != null) {
            switch (sortOpt) {
                case "名称升序":
                    filtered.sort(Comparator.comparing(ImageFileItem::getName, String.CASE_INSENSITIVE_ORDER));
                    break;
                case "名称降序":
                    filtered.sort(
                            Comparator.comparing(ImageFileItem::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                    break;
                case "大小升序":
                    filtered.sort(Comparator.comparingLong((ImageFileItem item) -> item.getFile().length()));
                    break;
                case "大小降序":
                    filtered.sort(Comparator.comparingLong((ImageFileItem item) -> item.getFile().length()).reversed());
                    break;
                case "日期升序":
                    filtered.sort(Comparator.comparingLong((ImageFileItem item) -> item.getFile().lastModified()));
                    break;
                case "日期降序":
                    filtered.sort(
                            Comparator.comparingLong((ImageFileItem item) -> item.getFile().lastModified()).reversed());
                    break;
                default:
                    break;
            }
        }
        // 添加缩略图
        for (ImageFileItem item : filtered) {
            createAndAddThumbnailNode(item);
        }
    }
}
