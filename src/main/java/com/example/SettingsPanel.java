package com.example;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public class SettingsPanel extends BorderPane {
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private final Stage parentStage;

    public SettingsPanel(Stage parentStage) {
        this.parentStage = parentStage;
        initUI();
    }

    private void initUI() {
        // 左侧设置选项列表
        ListView<String> optionList = new ListView<>();
        optionList.getItems().addAll("通用设置", "播放器设置");
        optionList.getSelectionModel().select(0);
        optionList.setPrefWidth(150);
        
        // 右侧设置内容面板
        StackPane contentPane = new StackPane();
        contentPane.setPadding(new Insets(15));
        
        // 通用设置面板
        VBox generalSettings = createGeneralSettings();
        contentPane.getChildren().add(generalSettings);
        
        // 布局设置
        setLeft(optionList);
        setCenter(contentPane);
        
        // 选项切换事件
        optionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            contentPane.getChildren().clear();
            switch (newVal) {
                case "通用设置":
                    contentPane.getChildren().add(generalSettings);
                    break;
                case "播放器设置":
                    contentPane.getChildren().add(createPlayerSettings());
                    break;
            }
        });
    }

    private VBox createGeneralSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        
        // 自动播放设置
        CheckBox autoPlay = new CheckBox("自动播放下一集");
        autoPlay.setSelected(prefs.getBoolean("AUTO_PLAY", false));
        autoPlay.selectedProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putBoolean("AUTO_PLAY", newVal));
        
        // 默认音量设置
        Label volumeLabel = new Label("默认音量:");
        Slider volumeSlider = new Slider(0, 1, prefs.getDouble("DEFAULT_VOLUME", 0.5));
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putDouble("DEFAULT_VOLUME", newVal.doubleValue()));
        
        panel.getChildren().addAll(
            new Label("通用设置"),
            new Separator(),
            autoPlay,
            volumeLabel,
            volumeSlider
        );
        return panel;
    }

    private VBox createPlayerSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));
        
        // 进度条灵敏度
        Label sensitivityLabel = new Label("进度条灵敏度:");
        Slider sensitivitySlider = new Slider(1, 5, prefs.getInt("SEEK_SENSITIVITY", 3));
        sensitivitySlider.setBlockIncrement(1);
        sensitivitySlider.setMajorTickUnit(1);
        sensitivitySlider.setSnapToTicks(true);
        sensitivitySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("SEEK_SENSITIVITY", newVal.intValue()));
        
        // 控制栏隐藏时间
        Label hideDelayLabel = new Label("控制栏隐藏延迟(秒):");
        Spinner<Integer> hideSpinner = new Spinner<>(1, 10, prefs.getInt("HIDE_DELAY", 3));
        hideSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("HIDE_DELAY", newVal));
        
        panel.getChildren().addAll(
            new Label("播放器设置"),
            new Separator(),
            sensitivityLabel,
            sensitivitySlider,
            hideDelayLabel,
            hideSpinner
        );
        return panel;
    }
}
