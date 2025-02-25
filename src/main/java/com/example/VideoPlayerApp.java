package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import java.io.File;

public class VideoPlayerApp extends Application {
    private MediaPlayer mediaPlayer;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        MediaView mediaView = new MediaView();

        File videoFile = FileUtils.chooseVideoFile(primaryStage);
        if (videoFile == null) return;

        Media media = new Media(videoFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        // 初始化控制条并传入 mediaPlayer
        VideoControllerBar controllerBar = new VideoControllerBar(mediaPlayer);
        controllerBar.setPlayAction(() -> mediaPlayer.play());
        controllerBar.setPauseAction(() -> mediaPlayer.pause());

        // 绑定视频尺寸到窗口
        mediaView.fitWidthProperty().bind(root.widthProperty());
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(60)); // 控制条高度约60px

        root.setCenter(mediaView);
        root.setBottom(controllerBar);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}