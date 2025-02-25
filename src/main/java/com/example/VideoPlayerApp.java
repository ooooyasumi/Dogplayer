package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer; // 修复拼写错误
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
        standbyLabel = new Label("DogPlayer");
        standbyLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: rgba(255,255,255,0.3);");
        StackPane.setAlignment(standbyLabel, Pos.CENTER);

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaContainer.getChildren().addAll(mediaView, standbyLabel);
        mediaContainer.setStyle("-fx-background-color: black;");

        controllerBar = new VideoControllerBar(null, primaryStage, mediaView); // 传递 MediaView
        controllerBar.setOpenAction(() -> loadNewVideo());
        BorderPane.setMargin(controllerBar, new Insets(0));

        mediaView.fitWidthProperty().bind(mediaContainer.widthProperty());
        mediaView.fitHeightProperty().bind(mediaContainer.heightProperty().subtract(60));

        root.setCenter(mediaContainer);
        root.setBottom(controllerBar);

        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        // 全局键盘事件监听
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume(); // 阻止事件传递到其他组件
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
                    mediaView.setFitWidth(primaryStage.getWidth());
                    mediaView.setFitHeight(primaryStage.getHeight() - 60);
                    root.requestLayout(); // 强制刷新布局
                });
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("DogPlayer");
        primaryStage.show();
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