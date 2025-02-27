package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert; // 新增导入
import javafx.scene.control.Label;
import javafx.scene.input.Dragboard; // 新增导入
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode; // 新增导入
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import java.io.File;

public class VideoPlayerApp extends Application {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private VideoControllerBar controllerBar;
    private StackPane mediaContainer;
    private Label standbyLabel;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        mediaContainer = new StackPane();
        mediaContainer.setMinWidth(0); // 允许缩小到零宽度
        standbyLabel = new Label("DogPlayer");
        standbyLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: rgba(255,255,255,0.3);");
        StackPane.setAlignment(standbyLabel, Pos.CENTER);

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaContainer.getChildren().addAll(mediaView, standbyLabel);
        mediaContainer.setStyle("-fx-background-color: black;");

        controllerBar = new VideoControllerBar(null, primaryStage, mediaView);
        controllerBar.setOpenAction(() -> loadNewVideo());
        BorderPane.setMargin(controllerBar, new Insets(0));

        // 动态绑定 MediaView 的宽度和高度
        mediaContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitWidth(newVal.doubleValue());
        });

        mediaContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitHeight(newVal.doubleValue() - 60);
        });

        root.setCenter(mediaContainer);
        root.setBottom(controllerBar);

        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        // 全局键盘事件监听
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
                if (mediaPlayer != null) {
                    controllerBar.togglePlayPause();
                }
            }
        });

        // 鼠标移动事件
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e ->
                controllerBar.handleMouseMove()
        );

        // 全屏状态监听
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> {
                    mediaView.setFitWidth(mediaContainer.getWidth());
                    mediaView.setFitHeight(mediaContainer.getHeight() - 60);
                    root.requestLayout();
                });
            }
        });

        // 拖放事件处理
        scene.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        scene.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasFiles()) {
                File file = dragboard.getFiles().get(0);
                if (isVideoFile(file)) {
                    loadVideoFile(file);
                    event.setDropCompleted(true);
                } else {
                    showUnsupportedFileAlert(); // 显示不支持的文件提示
                    event.setDropCompleted(false);
                }
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("DogPlayer");
        primaryStage.show();
    }

    /**
     * 判断文件是否为视频文件
     */
    private boolean isVideoFile(File file) {
        String[] supportedExtensions = {".mp4", ".flv", ".mkv", ".avi"};
        String fileName = file.getName().toLowerCase();
        for (String ext : supportedExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 加载视频文件
     */
    private void loadVideoFile(File videoFile) {
        if (videoFile != null) {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                controllerBar.dispose();
            }

            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.pause();
            });

            mediaView.setMediaPlayer(mediaPlayer);
            standbyLabel.setVisible(false);
            controllerBar.updateMediaPlayer(mediaPlayer);
        }
    }

    /**
     * 显示不支持的文件提示窗口
     */
    private void showUnsupportedFileAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText("仅支持导入的视频文件格式（如 .mp4, .flv, .mkv, .avi）。");
        alert.showAndWait();
    }

    private void loadNewVideo() {
        File videoFile = FileUtils.chooseVideoFile(primaryStage);
        if (videoFile != null) {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                controllerBar.dispose();
            }

            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.pause();
            });

            mediaView.setMediaPlayer(mediaPlayer);
            standbyLabel.setVisible(false);
            controllerBar.updateMediaPlayer(mediaPlayer);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}