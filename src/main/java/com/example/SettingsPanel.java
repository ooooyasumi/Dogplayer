package com.example;

// 导入JavaFX和相关库
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import javafx.scene.text.Text;

/**
 * 设置面板类，用于管理应用程序的通用设置、播放器设置和关于信息
 */
public class SettingsPanel extends BorderPane {
    // 偏好设置对象，用于保存和加载用户设置
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    // 内容面板，用于显示不同的设置选项
    private StackPane contentPane;

    /**
     * 构造函数，初始化UI
     */
    public SettingsPanel() {
        initUI();
    }

    /**
     * 初始化UI组件
     */
    private void initUI() {
        // 设置面板样式
        setStyle("-fx-background-color: #2B2B2B; -fx-border-color: #404040; -fx-border-radius: 5;");

        // 初始化左侧选项列表
        ListView<String> optionList = new ListView<>();
        optionList.getItems().addAll("通用设置", "播放器设置", "关于");
        optionList.setStyle("-fx-background-color: #2B2B2B; -fx-text-fill: white;");
        optionList.setPrefWidth(150);

        // 设置列表项样式
        optionList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("-fx-background-color: #2B2B2B; -fx-text-fill: white;");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // 初始化内容面板
        contentPane = new StackPane();
        contentPane.setPadding(new Insets(15));
        contentPane.getChildren().add(createGeneralSettings()); // 默认显示通用设置

        // 初始化底部按钮
        HBox buttonBox = new HBox(15);
        buttonBox.setPadding(new Insets(15));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // 按钮样式
        String buttonStyle = "-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-padding: 8 20;";

        // 保存按钮
        Button saveBtn = new Button("保存");
        saveBtn.setStyle(buttonStyle);
        saveBtn.setOnAction(e -> {
            try {
                prefs.flush(); // 保存设置
            } catch (Exception ex) {
                System.err.println("设置保存失败: " + ex.getMessage());
            }
            ((Stage) getScene().getWindow()).close(); // 关闭窗口
        });

        // 返回按钮
        Button cancelBtn = new Button("返回");
        cancelBtn.setStyle(buttonStyle);
        cancelBtn.setOnAction(e -> ((Stage) getScene().getWindow()).close());

        buttonBox.getChildren().addAll(saveBtn, cancelBtn);

        // 设置布局
        setLeft(optionList);      // 左侧选项列表
        setCenter(contentPane);   // 中间内容面板
        setBottom(buttonBox);     // 底部按钮

        // 选项切换监听器
        optionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contentPane.getChildren().clear(); // 清空内容面板
            switch (newVal) {
                case "通用设置":
                    contentPane.getChildren().add(createGeneralSettings());
                    break;
                case "播放器设置":
                    contentPane.getChildren().add(createPlayerSettings());
                    break;
                case "关于":
                    contentPane.getChildren().add(createAboutPanel());
                    break;
            }
        });
    }

    /**
     * 创建通用设置面板
     * @return 通用设置面板
     */
    private VBox createGeneralSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");

        // 标题
        Label title = new Label("通用设置");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16;");

        // 自动播放下一集复选框
        CheckBox autoPlay = new CheckBox("自动播放下一集");
        autoPlay.setStyle("-fx-text-fill: white;");
        autoPlay.setSelected(prefs.getBoolean("AUTO_PLAY", false)); // 从偏好设置加载
        autoPlay.selectedProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putBoolean("AUTO_PLAY", newVal)); // 保存到偏好设置

        // 默认音量标签
        Label volumeLabel = new Label("默认音量:");
        volumeLabel.setStyle("-fx-text-fill: white;");

        // 默认音量滑块
        Slider volumeSlider = new Slider(0, 1, prefs.getDouble("DEFAULT_VOLUME", 0.5));
        volumeSlider.setStyle("-fx-control-inner-background: #3C3C3C;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putDouble("DEFAULT_VOLUME", newVal.doubleValue())); // 保存到偏好设置

        // 添加组件到面板
        panel.getChildren().addAll(title, new Separator(), autoPlay, volumeLabel, volumeSlider);
        return panel;
    }

    /**
     * 创建播放器设置面板
     * @return 播放器设置面板
     */
    private VBox createPlayerSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");

        // 标题
        Label title = new Label("播放器设置");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16;");

        // 进度条灵敏度标签
        Label sensitivityLabel = new Label("进度条灵敏度:");
        sensitivityLabel.setStyle("-fx-text-fill: white;");

        // 进度条灵敏度滑块
        Slider sensitivitySlider = new Slider(1, 5, prefs.getInt("SEEK_SENSITIVITY", 3));
        sensitivitySlider.setBlockIncrement(1);
        sensitivitySlider.setMajorTickUnit(1);
        sensitivitySlider.setSnapToTicks(true);
        sensitivitySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("SEEK_SENSITIVITY", newVal.intValue())); // 保存到偏好设置

        // 控制栏隐藏延迟标签
        Label hideDelayLabel = new Label("控制栏隐藏延迟(秒):");
        hideDelayLabel.setStyle("-fx-text-fill: white;");

        // 控制栏隐藏延迟微调器
        Spinner<Integer> hideSpinner = new Spinner<>(1, 10, prefs.getInt("HIDE_DELAY", 3));
        hideSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("HIDE_DELAY", newVal)); // 保存到偏好设置

        // 添加组件到面板
        panel.getChildren().addAll(title, new Separator(), sensitivityLabel, 
            sensitivitySlider, hideDelayLabel, hideSpinner);
        return panel;
    }

    /**
     * 创建关于面板
     * @return 关于面板
     */
    private VBox createAboutPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");

        // 标题
        Text title = new Text("DogPlayer - 极简视频播放器");
        title.setStyle("-fx-fill: white; -fx-font-size: 18;");

        // 版本信息
        Text version = new Text("版本: v0.1.1");
        version.setStyle("-fx-fill: #CCCCCC;");

        // GitHub链接
        Hyperlink githubLink = new Hyperlink("https://github.com/ooooyasumi/Dogplayer");
        githubLink.setStyle("-fx-text-fill: #00B4FF; -fx-border-color: transparent;");

        // 添加组件到面板
        panel.getChildren().addAll(title, new Separator(), version, githubLink);
        return panel;
    }
}