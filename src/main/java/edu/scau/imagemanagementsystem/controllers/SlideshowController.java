package edu.scau.imagemanagementsystem.controllers;

import java.util.List;

import edu.scau.imagemanagementsystem.model.ImageFileItem;
import edu.scau.imagemanagementsystem.services.ImageLoadService;
import edu.scau.imagemanagementsystem.utils.UiUtils;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SlideshowController {

    @FXML
    private ImageView slideshowImageView;
    @FXML
    private BorderPane rootPane;
    @FXML
    private HBox controlBar;
    @FXML
    private Button prevButton;
    @FXML
    private Button playPauseButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Label imageInfoLabel;

    private List<ImageFileItem> imagesToShow;
    private int currentIndex;
    private ImageLoadService imageLoadService;
    private Timeline slideshowTimeline;
    private boolean isPlaying = false;
    private static final double ZOOM_FACTOR = 1.1;

    public void initializeData(List<ImageFileItem> images, ImageFileItem startImage) {
        this.imagesToShow = images;
        this.currentIndex = images.indexOf(startImage);
        if (this.currentIndex < 0 && !images.isEmpty()) { // Fallback if startImage not found
            this.currentIndex = 0;
        }
        this.imageLoadService = new ImageLoadService();
        setupSlideshowTimeline();
        loadImageAtIndex(currentIndex);
        updateButtonStates();
    }

    @FXML
    public void initialize() {
        slideshowImageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
                slideshowImageView.fitWidthProperty().bind(rootPane.widthProperty().subtract(20));
                slideshowImageView.fitHeightProperty().bind(
                        rootPane.heightProperty().subtract(controlBar.heightProperty()).subtract(20));
                newScene.setOnScroll((ScrollEvent se) -> {
                    if (se.getDeltaY() > 0) {
                        handleZoomIn();
                    } else if (se.getDeltaY() < 0) {
                        handleZoomOut();
                    }
                    se.consume();
                });
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT) {
            showPreviousImage();
        } else if (event.getCode() == KeyCode.RIGHT) {
            showNextImage();
        } else if (event.getCode() == KeyCode.SPACE) {
            handlePlayPause();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) slideshowImageView.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
        shutdown();
    }

    private void setupSlideshowTimeline() {
        slideshowTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> showNextImageAutomatic()));
        slideshowTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    @FXML
    private void handlePrevImage() {
        showPreviousImage();
    }

    @FXML
    private void handleNextImage() {
        showNextImage();
    }

    private void showPreviousImage() {
        if (imagesToShow == null || imagesToShow.isEmpty())
            return;
        if (currentIndex > 0) {
            currentIndex--;
            loadImageAtIndex(currentIndex);
        }
        updateButtonStates();
    }

    private void showNextImage() {
        if (imagesToShow == null || imagesToShow.isEmpty())
            return;
        if (currentIndex < imagesToShow.size() - 1) {
            currentIndex++;
            loadImageAtIndex(currentIndex);
        } else {
            if (isPlaying) {
                handlePlayPause();
            }
        }
        updateButtonStates();
    }

    private void showNextImageAutomatic() {
        if (imagesToShow == null || imagesToShow.isEmpty())
            return;
        if (currentIndex < imagesToShow.size() - 1) {
            currentIndex++;
            loadImageAtIndex(currentIndex);
        } else {
            slideshowTimeline.stop();
            playPauseButton.setText("播放");
            isPlaying = false;
        }
        updateButtonStates();
    }

    @FXML
    private void handlePlayPause() {
        if (imagesToShow == null || imagesToShow.isEmpty())
            return;
        if (isPlaying) {
            slideshowTimeline.pause();
            playPauseButton.setText("播放");
        } else {
            slideshowTimeline.play();
            playPauseButton.setText("暂停");
            if (currentIndex == imagesToShow.size() - 1) {
                currentIndex = -1;
                showNextImageAutomatic();
            }
        }
        isPlaying = !isPlaying;
    }

    @FXML
    private void handleZoomIn() {
        slideshowImageView.setScaleX(slideshowImageView.getScaleX() * ZOOM_FACTOR);
        slideshowImageView.setScaleY(slideshowImageView.getScaleY() * ZOOM_FACTOR);
    }

    @FXML
    private void handleZoomOut() {
        slideshowImageView.setScaleX(slideshowImageView.getScaleX() / ZOOM_FACTOR);
        slideshowImageView.setScaleY(slideshowImageView.getScaleY() / ZOOM_FACTOR);
    }

    private void loadImageAtIndex(int index) {
        if (index >= 0 && index < imagesToShow.size()) {
            ImageFileItem item = imagesToShow.get(index);
            if (item.getFile() != null && item.getFile().exists()) {
                Task<Image> loadTask = imageLoadService.loadImageAsync(item.getFile());
                loadTask.setOnSucceeded(event -> {
                    slideshowImageView.setImage(loadTask.getValue());
                    slideshowImageView.setScaleX(1.0);
                    slideshowImageView.setScaleY(1.0);
                });
                loadTask.setOnFailed(event -> {
                    UiUtils.showErrorDialog("错误", "无法加载图片", item.getName(), (Exception) loadTask.getException());
                    slideshowImageView.setImage(null);
                });
                new Thread(loadTask).start();
                updateImageInfoLabel(item, index);
            } else {
                UiUtils.showErrorDialog("错误", "图片文件不存在", item.getName());
                slideshowImageView.setImage(null);
                updateImageInfoLabel(null, index);
            }
        } else {
            slideshowImageView.setImage(null);
            updateImageInfoLabel(null, index);
        }
    }

    private void updateImageInfoLabel(ImageFileItem item, int idx) {
        if (item != null && imagesToShow != null && !imagesToShow.isEmpty()) {
            imageInfoLabel.setText(String.format("%s - %d/%d", item.getName(), idx + 1, imagesToShow.size()));
        } else if (imagesToShow != null && !imagesToShow.isEmpty()) {
            imageInfoLabel.setText(String.format("图片无法加载 - %d/%d", idx + 1, imagesToShow.size()));
        } else {
            imageInfoLabel.setText("没有图片");
        }
    }

    private void updateButtonStates() {
        if (imagesToShow == null || imagesToShow.isEmpty()) {
            prevButton.setDisable(true);
            nextButton.setDisable(true);
            playPauseButton.setDisable(true);
            zoomInButton.setDisable(true);
            zoomOutButton.setDisable(true);
            return;
        }
        prevButton.setDisable(currentIndex <= 0);
        nextButton.setDisable(currentIndex >= imagesToShow.size() - 1 && !isPlaying);
        playPauseButton.setDisable(false);
        zoomInButton.setDisable(false);
        zoomOutButton.setDisable(false);
    }

    public void shutdown() {
        if (slideshowTimeline != null) {
            slideshowTimeline.stop();
        }
    }
}