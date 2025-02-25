package com.example;

import javafx.application.Platform;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView; // 导入 MediaView
import javafx.stage.Stage;
import javafx.util.Duration;

public class VideoControllerBar extends HBox {
    private ToggleButton playPauseButton;
    private Button openButton;
    private Button fullscreenButton;
    private Slider progressSlider;
    private Slider volumeSlider;
    private Label timeLabel;
    private MediaPlayer mediaPlayer;
    private boolean isDragging = false;
    private MediaPlayer.Status statusBeforeDrag;
    private AnimationTimer timer;
    private FadeTransition fadeTransition;
    private long lastMouseMoveTime = System.currentTimeMillis();
    private Stage stage;
    private MediaView mediaView; // 添加 MediaView 引用

    public VideoControllerBar(MediaPlayer mediaPlayer, Stage stage, MediaView mediaView) {
        this.mediaPlayer = mediaPlayer;
        this.stage = stage;
        this.mediaView = mediaView; // 初始化 MediaView
        initUI();
        if (mediaPlayer != null) bindMediaPlayer();
    }

    private void initUI() {
        playPauseButton = new ToggleButton("▶");
        openButton = new Button("打开");
        fullscreenButton = new Button("⛶");
        progressSlider = new Slider(0, 1, 0);
        volumeSlider = new Slider(0, 1, 0.5);
        timeLabel = new Label("00:00/00:00");

        setAlignment(Pos.CENTER);
        setSpacing(10);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // 禁用所有控件的焦点
        playPauseButton.setFocusTraversable(false);
        openButton.setFocusTraversable(false);
        fullscreenButton.setFocusTraversable(false);
        progressSlider.setFocusTraversable(false);
        volumeSlider.setFocusTraversable(false);

        progressSlider.setMinWidth(200);
        HBox.setHgrow(progressSlider, Priority.ALWAYS);
        fullscreenButton.setStyle("-fx-font-size: 14px; -fx-min-width: 40px;");
        timeLabel.setStyle("-fx-text-fill: white;");

        getChildren().addAll(openButton, playPauseButton, progressSlider, timeLabel, volumeSlider, fullscreenButton);

        // 全屏按钮事件
        fullscreenButton.setOnAction(e -> {
            boolean newState = !stage.isFullScreen();
            stage.setFullScreen(newState);
            if (!newState) {
                Platform.runLater(() -> {
                    mediaView.setFitWidth(stage.getWidth()); // 直接设置 MediaView 尺寸
                    mediaView.setFitHeight(stage.getHeight() - 60);
                });
            }
        });

        // 播放/暂停按钮事件
        playPauseButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                if (newVal) {
                    mediaPlayer.pause();
                    playPauseButton.setText("▶");
                } else {
                    mediaPlayer.play();
                    playPauseButton.setText("⏸");
                }
            }
        });

        // 音量条事件
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) mediaPlayer.setVolume(newVal.doubleValue());
        });

        // 进度条拖动事件
        progressSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                statusBeforeDrag = mediaPlayer.getStatus();
                mediaPlayer.pause();
                isDragging = true;
            }
        });

        progressSlider.setOnMouseDragged(e -> {
            if (isDragging) updateTimeDisplay();
        });

        progressSlider.setOnMouseReleased(e -> {
            if (isDragging && mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                if (isValidDuration(total)) {
                    mediaPlayer.seek(Duration.millis((long)(total.toMillis() * progressSlider.getValue())));
                }
                isDragging = false;
                if (statusBeforeDrag == MediaPlayer.Status.PLAYING) mediaPlayer.play();
            }
        });
    }

    private void bindMediaPlayer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isDragging && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    updateProgress();
                }
                if (System.currentTimeMillis() - lastMouseMoveTime > 3000) {
                    fadeOutControlBar();
                }
            }
        };
        timer.start();
    }

    public void handleMouseMove() {
        lastMouseMoveTime = System.currentTimeMillis();
        if (!isVisible()) {
            setVisible(true);
            setOpacity(1.0);
            if (fadeTransition != null) fadeTransition.stop();
        }
    }

    private void fadeOutControlBar() {
        if (!isVisible()) return;

        fadeTransition = new FadeTransition(Duration.millis(1000), this);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(e -> setVisible(false));
        fadeTransition.play();
    }

    private void updateProgress() {
        Duration current = mediaPlayer.getCurrentTime();
        Duration total = mediaPlayer.getTotalDuration();
        if (isValidDuration(total)) {
            long currentMillis = (long) current.toMillis();
            long totalMillis = (long) total.toMillis();
            progressSlider.setValue((double) currentMillis / totalMillis);
            timeLabel.setText(formatTime(currentMillis) + "/" + formatTime(totalMillis));
        }
    }

    private void updateTimeDisplay() {
        Duration total = mediaPlayer.getTotalDuration();
        if (isValidDuration(total)) {
            long totalMillis = (long) total.toMillis();
            long currentMillis = (long) (totalMillis * progressSlider.getValue());
            timeLabel.setText(formatTime(currentMillis) + "/" + formatTime(totalMillis));
        }
    }

    private boolean isValidDuration(Duration d) {
        return d != null && d.greaterThan(Duration.ZERO) && !d.isUnknown();
    }

    private String formatTime(long millis) {
        int hours = (int)(millis / 3600000);
        int minutes = (int)(millis % 3600000) / 60000;
        int seconds = (int)((millis % 60000) / 1000);
        return hours > 0 ?
                String.format("%02d:%02d:%02d", hours, minutes, seconds) :
                String.format("%02d:%02d", minutes, seconds);
    }

    public void togglePlayPause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playPauseButton.setSelected(true);
                playPauseButton.setText("▶");
            } else {
                mediaPlayer.play();
                playPauseButton.setSelected(false);
                playPauseButton.setText("⏸");
            }
        }
    }

    public void setOpenAction(Runnable action) {
        openButton.setOnAction(e -> action.run());
    }

    public void updateMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
        if (mediaPlayer != null) {
            volumeSlider.setValue(mediaPlayer.getVolume());
            playPauseButton.setDisable(false);
            bindMediaPlayer();
        } else {
            playPauseButton.setDisable(true);
            progressSlider.setValue(0);
            timeLabel.setText("00:00/00:00");
        }
    }

    public void dispose() {
        if (timer != null) timer.stop();
    }
}