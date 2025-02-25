package com.example;

import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class VideoControllerBar extends HBox {
    private Button playButton;
    private Button pauseButton;
    private Slider progressSlider;
    private Label timeLabel;
    private MediaPlayer mediaPlayer;
    private boolean isDragging = false;

    public VideoControllerBar(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        initUI();
        bindMediaPlayer();
    }

    private void initUI() {
        playButton = new Button("▶");
        pauseButton = new Button("⏸");
        progressSlider = new Slider(0, 1, 0);
        timeLabel = new Label("00:00/00:00");

        // 控制条样式
        this.setAlignment(Pos.CENTER);
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 10;");

        // 进度条设置
        progressSlider.setMinWidth(200);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        // 时间标签样式
        timeLabel.setStyle("-fx-text-fill: white;");

        // 添加组件
        this.getChildren().addAll(playButton, pauseButton, progressSlider, timeLabel);

        // 进度条拖动事件（彻底修复警告）
        progressSlider.setOnMousePressed(e -> {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            }
            isDragging = true;
        });

        progressSlider.setOnMouseDragged(e -> {
            if (isDragging) {
                updateTimeDisplay();
            }
        });

        progressSlider.setOnMouseReleased(e -> {
            if (isDragging) {
                Duration totalDuration = mediaPlayer.getTotalDuration();
                if (isValidDuration(totalDuration)) {
                    // 使用毫秒精度计算，避免浮点误差
                    long totalMillis = (long) totalDuration.toMillis();
                    double progress = progressSlider.getValue();
                    long newMillis = (long) (totalMillis * progress);
                    Duration newTime = Duration.millis(newMillis);
                    mediaPlayer.seek(newTime);
                }
                isDragging = false;
                if (mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                    mediaPlayer.play();
                }
            }
        });
    }

    private void bindMediaPlayer() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isDragging && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    updateProgress();
                }
            }
        };
        timer.start();
    }

    private void updateProgress() {
        Duration currentTime = mediaPlayer.getCurrentTime();
        Duration totalDuration = mediaPlayer.getTotalDuration();
        if (isValidDuration(totalDuration)) {
            // 毫秒级精度计算
            long currentMillis = (long) currentTime.toMillis();
            long totalMillis = (long) totalDuration.toMillis();
            double progress = (double) currentMillis / totalMillis;
            if (!Double.isNaN(progress)) {
                progressSlider.setValue(progress);
                timeLabel.setText(formatTime(currentMillis) + "/" + formatTime(totalMillis));
            }
        }
    }

    private void updateTimeDisplay() {
        Duration totalDuration = mediaPlayer.getTotalDuration();
        if (isValidDuration(totalDuration)) {
            long totalMillis = (long) totalDuration.toMillis();
            double progress = progressSlider.getValue();
            long currentMillis = (long) (totalMillis * progress);
            timeLabel.setText(formatTime(currentMillis) + "/" + formatTime(totalMillis));
        }
    }

    private boolean isValidDuration(Duration duration) {
        return duration != null && duration.greaterThan(Duration.ZERO) && !duration.isUnknown();
    }

    // 毫秒格式化时间（彻底消除秒级浮点误差）
    private String formatTime(long millis) {
        int minutes = (int) (millis / 60000);
        int seconds = (int) ((millis % 60000) / 1000);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setPlayAction(Runnable action) {
        playButton.setOnAction(e -> action.run());
    }

    public void setPauseAction(Runnable action) {
        pauseButton.setOnAction(e -> action.run());
    }
}