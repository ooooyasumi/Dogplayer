package com.example;

// 导入JavaFX和相关库
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
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Cursor;

/**
 * 视频控制条组件，用于控制视频播放、进度、音量等
 */
public class VideoControllerBar extends HBox {
    // 控件定义
    private ToggleButton playPauseButton; // 播放/暂停按钮
    private Button fullscreenButton;      // 全屏按钮
    private Slider progressSlider;        // 进度条
    private Slider volumeSlider;          // 音量条
    private Label timeLabel;              // 时间标签
    private MediaPlayer mediaPlayer;      // 媒体播放器
    private boolean isDragging = false;  // 进度条拖动状态
    private MediaPlayer.Status statusBeforeDrag; // 拖动前的播放状态
    private AnimationTimer timer;         // 动画计时器，用于更新进度
    private FadeTransition fadeTransition; // 淡出动画
    private long lastMouseMoveTime = System.currentTimeMillis(); // 最后鼠标移动时间
    private Stage stage;                  // 主舞台
    private MediaView mediaView;          // 媒体视图

    /**
     * 构造函数
     * @param mediaPlayer 媒体播放器
     * @param stage 主舞台
     * @param mediaView 媒体视图
     */
    public VideoControllerBar(MediaPlayer mediaPlayer, Stage stage, MediaView mediaView) {
        this.mediaPlayer = mediaPlayer;
        this.stage = stage;
        this.mediaView = mediaView;
        initUI(); // 初始化UI
        if (mediaPlayer != null) bindMediaPlayer(); // 绑定媒体播放器

        // 添加媒体视图的鼠标移动监听
        mediaView.setOnMouseMoved(e -> handleMouseMove());
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 初始化控件并设置默认值
        playPauseButton = new ToggleButton("▶");
        fullscreenButton = new Button("⬜");
        progressSlider = new Slider(0, 1, 0);
        volumeSlider = new Slider(0, 1, 0.5);
        timeLabel = new Label("00:00/00:00");

        // 布局样式设置
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPadding(new Insets(8));
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.6); -fx-background-radius: 8;");

        // 禁用焦点显示
        playPauseButton.setFocusTraversable(false);
        fullscreenButton.setFocusTraversable(false);
        progressSlider.setFocusTraversable(false);
        volumeSlider.setFocusTraversable(false);

        // 进度条样式
        progressSlider.setMinWidth(200);
        progressSlider.setMinHeight(4);
        progressSlider.setStyle("-track-color: rgba(255,255,255,0.3); -thumb-color: #ff4757;"
                + "-fx-control-inner-background: rgba(255,255,255,0.1);");
        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        // 音量条样式
        volumeSlider.setMinWidth(80);
        volumeSlider.setMaxWidth(100);
        volumeSlider.setStyle("-track-color: rgba(255,255,255,0.3); -thumb-color: #ffffff;");

        // 时间标签样式
        timeLabel.setStyle("-fx-text-fill: #dfe4ea; -fx-font-size: 12px;");

        // 按钮通用样式
        String buttonStyle = "-fx-background-radius: 4; -fx-min-width: 32px; -fx-min-height: 32px;"
                + "-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 16px;"
                + "-fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: rgba(255,255,255,0.1);";

        playPauseButton.setStyle(buttonStyle);
        fullscreenButton.setStyle(buttonStyle);

        // 添加悬停动画
        playPauseButton.setOnMouseEntered(e -> playPauseButton.setStyle(buttonStyle + hoverStyle));
        playPauseButton.setOnMouseExited(e -> playPauseButton.setStyle(buttonStyle));
        fullscreenButton.setOnMouseEntered(e -> fullscreenButton.setStyle(buttonStyle + hoverStyle));
        fullscreenButton.setOnMouseExited(e -> fullscreenButton.setStyle(buttonStyle));

        // 进度条悬停互动
        progressSlider.setOnMouseEntered(e -> progressSlider.setStyle(progressSlider.getStyle() + "-fx-min-height: 6px;"));
        progressSlider.setOnMouseExited(e -> progressSlider.setStyle(progressSlider.getStyle().replace("-fx-min-height: 6px;", "")));

        // 添加控件到布局
        getChildren().addAll(playPauseButton, progressSlider, timeLabel, volumeSlider, fullscreenButton);

        // 全屏按钮事件
        fullscreenButton.setOnAction(e -> {
            boolean newState = !stage.isFullScreen();
            stage.setFullScreen(newState);
            if (!newState) {
                Platform.runLater(() -> {
                    mediaView.setFitWidth(stage.getWidth());
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

        // 音量控制事件
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

    /**
     * 绑定媒体播放器，启动计时器更新进度
     */
    private void bindMediaPlayer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isDragging && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    updateProgress();
                }
                if (System.currentTimeMillis() - lastMouseMoveTime > 3000) {
                    fadeOutControlBar();
                    // 隐藏鼠标光标
                    if (stage.getScene() != null) {
                        stage.getScene().setCursor(Cursor.NONE);
                    }
                }
            }
        };
        timer.start();
    }

    /**
     * 处理鼠标移动事件
     */
    public void handleMouseMove() {
        lastMouseMoveTime = System.currentTimeMillis();
        if (!isVisible()) {
            setVisible(true);
            setOpacity(1.0);
            if (fadeTransition != null) fadeTransition.stop();
        }
        // 显示鼠标光标
        if (stage.getScene() != null) {
            stage.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * 淡出控制条
     */
    private void fadeOutControlBar() {
        if (!isVisible()) return;

        fadeTransition = new FadeTransition(Duration.millis(1000), this);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setOnFinished(e -> setVisible(false));
        fadeTransition.play();
    }

    /**
     * 更新进度条和时间标签
     */
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

    /**
     * 更新时间显示
     */
    private void updateTimeDisplay() {
        Duration total = mediaPlayer.getTotalDuration();
        if (isValidDuration(total)) {
            long totalMillis = (long) total.toMillis();
            long currentMillis = (long) (totalMillis * progressSlider.getValue());
            timeLabel.setText(formatTime(currentMillis) + "/" + formatTime(totalMillis));
        }
    }

    /**
     * 检查时间是否有效
     * @param d 时间对象
     * @return 是否有效
     */
    private boolean isValidDuration(Duration d) {
        return d != null && d.greaterThan(Duration.ZERO) && !d.isUnknown();
    }

    /**
     * 格式化时间
     * @param millis 毫秒数
     * @return 格式化后的时间字符串
     */
    private String formatTime(long millis) {
        int hours = (int)(millis / 3600000);
        int minutes = (int)(millis % 3600000) / 60000;
        int seconds = (int)((millis % 60000) / 1000);
        return hours > 0 ?
                String.format("%02d:%02d:%02d", hours, minutes, seconds) :
                String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * 切换播放/暂停状态
     */
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

    /**
     * 更新媒体播放器
     * @param mediaPlayer 新的媒体播放器
     */
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

    /**
     * 释放资源，停止计时器
     */
    public void dispose() {
        if (timer != null) timer.stop();
    }
}