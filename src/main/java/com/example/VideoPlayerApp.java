package com.example;

// 导入JavaFX和相关库
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import java.io.File;
import javafx.stage.Modality;

/**
 * 主应用程序类，实现JavaFX视频播放器功能
 */
public class VideoPlayerApp extends Application {
    // 媒体播放相关组件
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private VideoControllerBar controllerBar;  // 自定义视频控制条
    private StackPane mediaContainer;         // 媒体容器
    private Label standbyLabel;               // 待机状态显示文本
    private Stage primaryStage;               // 主舞台
    private HBox menuBar;                     // 顶部菜单栏
    private Label fileNameLabel;              // 文件名显示标签
    private boolean hasVideoPlaying = false;  // 视频播放状态标志
    private FadeTransition fadeOutTransition; // 菜单栏淡出动画
    private HostServices hostServices;        // 主机服务（用于打开链接等）

    /**
     * JavaFX应用程序入口方法
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.hostServices = getHostServices();
        
        // 创建主布局容器
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        initMenuBar();  // 初始化菜单栏
        root.setTop(menuBar);

        // 初始化媒体容器
        mediaContainer = new StackPane();
        mediaContainer.setMinWidth(0);
        standbyLabel = new Label("DogPlayer");
        standbyLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: rgba(255,255,255,0.3);");
        StackPane.setAlignment(standbyLabel, Pos.CENTER);

        // 设置媒体视图
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);  // 保持视频比例
        mediaContainer.getChildren().addAll(mediaView, standbyLabel);
        mediaContainer.setStyle("-fx-background-color: black;");

        // 初始化控制条
        controllerBar = new VideoControllerBar(null, primaryStage, mediaView);
        BorderPane.setMargin(controllerBar, new Insets(0));

        // 媒体容器尺寸监听器
        mediaContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitWidth(newVal.doubleValue());
        });
        mediaContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            mediaView.setFitHeight(newVal.doubleValue() - 60);
        });

        // 设置主布局
        root.setCenter(mediaContainer);
        root.setBottom(controllerBar);

        // 创建场景并配置事件处理
        Scene scene = new Scene(root, 800, 600);
        scene.setFill(javafx.scene.paint.Color.BLACK);

        // 空格键播放/暂停控制
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
                if (mediaPlayer != null) {
                    controllerBar.togglePlayPause();
                }
            }
        });

        // 鼠标移动显示控制条
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> controllerBar.handleMouseMove());

        // 全屏模式监听器
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> {
                    mediaView.setFitWidth(mediaContainer.getWidth());
                    mediaView.setFitHeight(mediaContainer.getHeight() - 60);
                    root.requestLayout();
                });
            }
        });

        // 拖放文件处理
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
                    showUnsupportedFileAlert();
                    event.setDropCompleted(false);
                }
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        // 显示主窗口
        primaryStage.setScene(scene);
        primaryStage.setTitle("DogPlayer");
        primaryStage.show();
    }

    /**
     * 初始化菜单栏
     */
    private void initMenuBar() {
        Button menuButton = new Button("DogPlayer");
        menuButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");

        fileNameLabel = new Label("未打开文件");
        fileNameLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");

        menuBar = new HBox(10);
        menuBar.setPadding(new Insets(5));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.getChildren().addAll(menuButton, fileNameLabel);
        menuBar.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        initContextMenu(menuButton);  // 初始化右键菜单
        setupMenuBarHoverEffect();    // 设置悬停效果
    }

    /**
     * 初始化上下文菜单
     * @param menuButton 关联的菜单按钮
     */
    private void initContextMenu(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
                "-fx-background-color: #2B2B2B;" +
                "-fx-background-radius: 5;" +
                "-fx-border-radius: 5;" +
                "-fx-border-color: #404040;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);" +
                "-fx-padding: 8 0 8 0;");
    
        // 文件菜单
        Menu menuFile = createModernMenu("文件", "\uE8A5");
        MenuItem openItem = createModernMenuItem("打开", "#00C896");
        openItem.setOnAction(e -> loadNewVideo());
        menuFile.getItems().add(openItem);
    
        // 其他菜单项
        MenuItem settingsItem = createModernMenuItem("设置", "#FFA500");
        settingsItem.setOnAction(e -> showSettingsWindow());
        
        // 分隔符
        SeparatorMenuItem separator = new SeparatorMenuItem();
        separator.setStyle("-fx-padding: 5 0 5 0;");
        
        // 退出项
        MenuItem exitItem = createModernMenuItem("退出", "#FF4757");
        exitItem.setOnAction(e -> Platform.exit());
    
        // 构建菜单结构
        contextMenu.getItems().addAll(
            menuFile,
            separator,
            settingsItem,
            exitItem
        );
    
        menuButton.setOnMouseClicked(e -> toggleContextMenu(contextMenu, menuButton));
    }

    /**
     * 创建现代风格菜单
     * @param title 菜单标题
     * @param iconCode 图标Unicode
     * @return 创建好的Menu对象
     */
    private Menu createModernMenu(String title, String iconCode) {
        Menu menu = new Menu(title);
        menu.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI', 'Hiragino Sans GB';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 20 8 15;" +
                        "-fx-background-color: transparent;" +
                        "-fx-graphic: '" + iconCode + "';" +
                        "-fx-graphic-text-gap: 10;");
        return menu;
    }

    /**
     * 创建现代风格菜单项
     * @param text 显示文本
     * @param accentColor 强调色
     * @return 创建好的MenuItem对象
     */
    private MenuItem createModernMenuItem(String text, String accentColor) {
        MenuItem item = new MenuItem(text);
        item.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI', 'Hiragino Sans GB';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20 10 40;" +
                        "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-width: 0 0 0 3;" +
                        "-fx-border-color: transparent;" +
                        "hover:-fx-background-color: #3C3C3C;" +
                        "hover:-fx-border-color: " + accentColor + ";" +
                        "pressed:-fx-background-color: #4D4D4D;");
        return item;
    }

    /**
     * 切换上下文菜单显示状态
     * @param menu 上下文菜单
     * @param button 关联按钮
     */
    private void toggleContextMenu(ContextMenu menu, Button button) {
        if (menu.isShowing()) {
            menu.hide();
        } else {
            menu.show(button, Side.BOTTOM, 0, 0);
            // 淡入动画
            FadeTransition ft = new FadeTransition(Duration.millis(200), menu.getScene().getRoot());
            ft.setFromValue(0.9);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    /**
     * 设置菜单栏悬停效果
     */
    private void setupMenuBarHoverEffect() {
        fadeOutTransition = new FadeTransition(Duration.seconds(1), menuBar);
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setDelay(Duration.seconds(3));

        // 鼠标进入时停止淡出
        menuBar.setOnMouseEntered(e -> {
            if (hasVideoPlaying) {
                fadeOutTransition.stop();
                menuBar.setOpacity(1.0);
            }
        });

        // 鼠标离开时开始淡出
        menuBar.setOnMouseExited(e -> {
            if (hasVideoPlaying) {
                fadeOutTransition.playFromStart();
            }
        });
    }

    /**
     * 检查文件是否为支持的视频格式
     * @param file 待检查文件
     * @return 是否支持
     */
    private boolean isVideoFile(File file) {
        String[] supportedExtensions = { ".mp4", ".flv", ".mkv", ".avi" };
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
     * @param videoFile 视频文件对象
     */
    private void loadVideoFile(File videoFile) {
        if (videoFile != null) {
            fileNameLabel.setText(videoFile.getName());
            hasVideoPlaying = true;
            fadeOutTransition.play();  // 触发菜单栏淡出

            // 清理现有媒体资源
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
                controllerBar.dispose();
            }

            // 创建新媒体播放器
            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnEndOfMedia(() -> {  // 视频结束回调
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.pause();
            });

            mediaView.setMediaPlayer(mediaPlayer);
            standbyLabel.setVisible(false);  // 隐藏待机文字
            controllerBar.updateMediaPlayer(mediaPlayer);  // 更新控制条
        }
    }

    /**
     * 显示不支持文件警告
     */
    private void showUnsupportedFileAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText("仅支持导入的视频文件格式（如 .mp4, .flv, .mkv, .avi）。");
        alert.showAndWait();
    }

    /**
     * 加载新视频文件（通过文件选择器）
     */
    private void loadNewVideo() {
        File videoFile = FileUtils.chooseVideoFile(primaryStage);
        if (videoFile != null) {
            loadVideoFile(videoFile);
        }
    }

    /**
     * 显示设置窗口
     */
    private void showSettingsWindow() {
        Stage settingsStage = new Stage();
        SettingsPanel settingsPanel = new SettingsPanel();  // 假设存在设置面板类
        
        Scene scene = new Scene(settingsPanel, 600, 400);
        scene.setFill(Color.TRANSPARENT);
        
        // 窗口属性设置
        settingsStage.initOwner(primaryStage);
        settingsStage.initModality(Modality.WINDOW_MODAL);
        settingsStage.setScene(scene);
        settingsStage.setTitle("设置");
        
        // 窗口样式设置
        settingsStage.getScene().getRoot().setStyle(
            "-fx-background-color: #2B2B2B;" +
            "-fx-background-radius: 5;" +
            "-fx-border-color: #404040;" +
            "-fx-border-radius: 5;" +
            "-fx-border-width: 1;"
        );
        
        settingsStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}