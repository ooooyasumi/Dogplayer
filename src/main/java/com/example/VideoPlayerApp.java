package com.example;

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
import java.io.File;

public class VideoPlayerApp extends Application {
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private VideoControllerBar controllerBar;
    private StackPane mediaContainer;
    private Label standbyLabel;
    private Stage primaryStage;
    private HBox menuBar;
    private Label fileNameLabel;
    private boolean hasVideoPlaying = false;
    private FadeTransition fadeOutTransition;
    private HostServices hostServices;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.hostServices = getHostServices();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        initMenuBar();
        root.setTop(menuBar);

        mediaContainer = new StackPane();
        mediaContainer.setMinWidth(0);
        standbyLabel = new Label("DogPlayer");
        standbyLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: rgba(255,255,255,0.3);");
        StackPane.setAlignment(standbyLabel, Pos.CENTER);

        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        mediaContainer.getChildren().addAll(mediaView, standbyLabel);
        mediaContainer.setStyle("-fx-background-color: black;");

        controllerBar = new VideoControllerBar(null, primaryStage, mediaView);
        BorderPane.setMargin(controllerBar, new Insets(0));

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

        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.SPACE) {
                e.consume();
                if (mediaPlayer != null) {
                    controllerBar.togglePlayPause();
                }
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e ->
                controllerBar.handleMouseMove()
        );

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(() -> {
                    mediaView.setFitWidth(mediaContainer.getWidth());
                    mediaView.setFitHeight(mediaContainer.getHeight() - 60);
                    root.requestLayout();
                });
            }
        });

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

        primaryStage.setScene(scene);
        primaryStage.setTitle("DogPlayer");
        primaryStage.show();
    }

    private void initMenuBar() {
        Button menuButton = new Button("DogPlayer");
        menuButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px; -fx-cursor: hand;");

        fileNameLabel = new Label("未打开文件");
        fileNameLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");

        menuBar = new HBox(10);
        menuBar.setPadding(new Insets(5));
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.getChildren().addAll(menuButton, fileNameLabel);
        menuBar.setStyle("-fx-background-color: rgba(0,0,0,0.7);");

        initContextMenu(menuButton);
        setupMenuBarHoverEffect();
    }

    private void initContextMenu(Button menuButton) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle(
                "-fx-background-color: #2B2B2B;" +
                        "-fx-background-radius: 5;" +
                        "-fx-border-radius: 5;" +
                        "-fx-border-color: #404040;" +
                        "-fx-border-width: 1;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);" +
                        "-fx-padding: 8 0 8 0;"
        );

        // 文件菜单
        Menu menuFile = createModernMenu("文件", "\uE8A5");
        MenuItem openItem = createModernMenuItem("打开", "#00C896");
        openItem.setOnAction(e -> loadNewVideo());
        menuFile.getItems().add(openItem);

        // 其他菜单项
        MenuItem aboutItem = createModernMenuItem("关于 DogPlayer", "#00B4FF");
        MenuItem exitItem = createModernMenuItem("退出", "#FF4757");

        // 分隔符样式
        SeparatorMenuItem separator = new SeparatorMenuItem();
        separator.setStyle("-fx-padding: 5 0 5 0;");
        separator.getContent().setStyle(
                "-fx-border-color: #404040;" +
                        "-fx-border-width: 1 0 0 0;"
        );

        // 构建菜单结构
        contextMenu.getItems().addAll(
                menuFile,
                separator,
                aboutItem,
                exitItem
        );

        // 事件处理
        aboutItem.setOnAction(e -> showModernAboutDialog());
        exitItem.setOnAction(e -> Platform.exit());

        // 菜单按钮交互
        menuButton.setOnAction(e -> toggleContextMenu(contextMenu, menuButton));
    }

    private Menu createModernMenu(String title, String iconCode) {
        Menu menu = new Menu(title);
        menu.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI', 'Hiragino Sans GB';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 20 8 15;" +
                        "-fx-background-color: transparent;" +
                        "-fx-graphic: '" + iconCode + "';" +
                        "-fx-graphic-text-gap: 10;"
        );
        return menu;
    }

    private MenuItem createModernMenuItem(String text, String accentColor) {
        MenuItem item = new MenuItem(text);
        item.setStyle(
                "-fx-text-fill: #FFFFFF;" +
                        "-fx-font-family: 'Segoe UI', 'Hiragino Sans GB';" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20 10 40;" +  // 增加左侧留白用于装饰线
                        "-fx-background-color: transparent;" +
                        "-fx-cursor: hand;" +
                        "-fx-background-radius: 4;" +
                        "-fx-border-width: 0 0 0 3;" +
                        "-fx-border-color: transparent;" +
                        "hover:-fx-background-color: #3C3C3C;" +
                        "hover:-fx-border-color: " + accentColor + ";" +
                        "pressed:-fx-background-color: #4D4D4D;"
        );
        return item;
    }

    private void toggleContextMenu(ContextMenu menu, Button button) {
        if (menu.isShowing()) {
            menu.hide();
        } else {
            menu.show(button, Side.BOTTOM, 0, 0);
            // 添加显示动画
            FadeTransition ft = new FadeTransition(Duration.millis(200), menu.getScene().getRoot());
            ft.setFromValue(0.9);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void showModernAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 DogPlayer");
        alert.setHeaderText(null);

        // 自定义对话框样式
        alert.getDialogPane().setStyle(
                "-fx-background-color: #2B2B2B;" +
                        "-fx-font-family: 'Hiragino Sans GB';" +
                        "-fx-text-fill: white;"
        );

        // 自定义内容
        Text title = new Text("DogPlayer - 极简视频播放器");
        title.setStyle("-fx-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");

        Text version = new Text("版本: v0.1.1");
        version.setStyle("-fx-fill: white;");

        Text developer = new Text("开发者: ooooyasumi");
        developer.setStyle("-fx-fill: white;");

        Hyperlink githubLink = new Hyperlink("GitHub: https://github.com/ooooyasumi/Dogplayer");
        githubLink.setStyle("-fx-text-fill: #00B4FF; -fx-border-color: transparent;");
        githubLink.setOnAction(e -> hostServices.showDocument("https://github.com/ooooyasumi/Dogplayer"));

        VBox content = new VBox(10, title, version, developer, githubLink);
        content.setPadding(new Insets(15));
        alert.getDialogPane().setContent(content);

        // 设置对话框按钮区域样式
        alert.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #3C3C3C;" +
                        "-fx-text-fill: white;"
        );

        alert.showAndWait();
    }

    private void setupMenuBarHoverEffect() {
        fadeOutTransition = new FadeTransition(Duration.seconds(1), menuBar);
        fadeOutTransition.setFromValue(1.0);
        fadeOutTransition.setToValue(0.0);
        fadeOutTransition.setDelay(Duration.seconds(3));

        menuBar.setOnMouseEntered(e -> {
            if (hasVideoPlaying) {
                fadeOutTransition.stop();
                menuBar.setOpacity(1.0);
            }
        });

        menuBar.setOnMouseExited(e -> {
            if (hasVideoPlaying) {
                fadeOutTransition.playFromStart();
            }
        });
    }

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

    private void loadVideoFile(File videoFile) {
        if (videoFile != null) {
            fileNameLabel.setText(videoFile.getName());
            hasVideoPlaying = true;
            fadeOutTransition.play();

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
            loadVideoFile(videoFile);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}