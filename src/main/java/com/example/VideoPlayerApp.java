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
        // 创建布局
        BorderPane root = new BorderPane();
        MediaView mediaView = new MediaView();
        VideoControllerBar controllerBar = new VideoControllerBar();

        // 选择视频文件
        File videoFile = FileUtils.chooseVideoFile(primaryStage);
        if (videoFile == null) return;

        // 初始化媒体播放器
        Media media = new Media(videoFile.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);

        // 绑定控制条事件
        controllerBar.setPlayAction(() -> mediaPlayer.play());
        controllerBar.setPauseAction(() -> mediaPlayer.pause());

        // 设置布局
        root.setCenter(mediaView);
        root.setBottom(controllerBar);

        // 绑定视频尺寸到窗口大小（核心修改）
        mediaView.fitWidthProperty().bind(root.widthProperty());  // 视频宽度 = 窗口宽度
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(50));  // 视频高度 = 窗口高度 - 控制条高度

        // 创建场景并显示
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("JavaFX Video Player");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}