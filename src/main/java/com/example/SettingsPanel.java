package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.prefs.Preferences;
import javafx.scene.text.Text;

public class SettingsPanel extends BorderPane {
    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private StackPane contentPane;

    public SettingsPanel() {
        initUI();
    }

    private void initUI() {
      setStyle("-fx-background-color: #2B2B2B; -fx-border-color: #404040; -fx-border-radius: 5;");
      
      // 初始化左侧选项列表（更新背景色）
      ListView<String> optionList = new ListView<>();
      optionList.getItems().addAll("通用设置", "播放器设置", "关于");
      optionList.setStyle("-fx-background-color: #2B2B2B; -fx-text-fill: white;");
      optionList.setPrefWidth(150);
      
      // 添加列表项样式（修复白色背景问题）
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
      contentPane.getChildren().add(createGeneralSettings());
      
      // 初始化底部按钮
      HBox buttonBox = new HBox(15);
      buttonBox.setPadding(new Insets(15));
      buttonBox.setAlignment(Pos.CENTER_RIGHT);
      
      String buttonStyle = "-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-padding: 8 20;";
      
      Button saveBtn = new Button("保存");
      saveBtn.setStyle(buttonStyle);
      saveBtn.setOnAction(e -> {
          try {
              prefs.flush();
          } catch (Exception ex) {
              System.err.println("设置保存失败: " + ex.getMessage());
          }
          ((Stage) getScene().getWindow()).close();
      });
  
      Button cancelBtn = new Button("返回");
      cancelBtn.setStyle(buttonStyle);
      cancelBtn.setOnAction(e -> ((Stage) getScene().getWindow()).close());
  
      buttonBox.getChildren().addAll(saveBtn, cancelBtn);
      
      // 设置布局
      setLeft(optionList);
      setCenter(contentPane);
      setBottom(buttonBox);
  
      // 选项切换监听器
      optionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
          contentPane.getChildren().clear();
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

    private VBox createGeneralSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");
        
        Label title = new Label("通用设置");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        
        CheckBox autoPlay = new CheckBox("自动播放下一集");
        autoPlay.setStyle("-fx-text-fill: white;");
        autoPlay.setSelected(prefs.getBoolean("AUTO_PLAY", false));
        autoPlay.selectedProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putBoolean("AUTO_PLAY", newVal));
        
        Label volumeLabel = new Label("默认音量:");
        volumeLabel.setStyle("-fx-text-fill: white;");
        Slider volumeSlider = new Slider(0, 1, prefs.getDouble("DEFAULT_VOLUME", 0.5));
        volumeSlider.setStyle("-fx-control-inner-background: #3C3C3C;");
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putDouble("DEFAULT_VOLUME", newVal.doubleValue()));
        
        panel.getChildren().addAll(title, new Separator(), autoPlay, volumeLabel, volumeSlider);
        return panel;
    }

    private VBox createPlayerSettings() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");
        
        Label title = new Label("播放器设置");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
        
        Label sensitivityLabel = new Label("进度条灵敏度:");
        sensitivityLabel.setStyle("-fx-text-fill: white;");
        Slider sensitivitySlider = new Slider(1, 5, prefs.getInt("SEEK_SENSITIVITY", 3));
        sensitivitySlider.setBlockIncrement(1);
        sensitivitySlider.setMajorTickUnit(1);
        sensitivitySlider.setSnapToTicks(true);
        sensitivitySlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("SEEK_SENSITIVITY", newVal.intValue()));
        
        Label hideDelayLabel = new Label("控制栏隐藏延迟(秒):");
        hideDelayLabel.setStyle("-fx-text-fill: white;");
        Spinner<Integer> hideSpinner = new Spinner<>(1, 10, prefs.getInt("HIDE_DELAY", 3));
        hideSpinner.getValueFactory().valueProperty().addListener((obs, oldVal, newVal) -> 
            prefs.putInt("HIDE_DELAY", newVal));
        
        panel.getChildren().addAll(title, new Separator(), sensitivityLabel, 
            sensitivitySlider, hideDelayLabel, hideSpinner);
        return panel;
    }

    private VBox createAboutPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: #2B2B2B;");
        
        Text title = new Text("DogPlayer - 极简视频播放器");
        title.setStyle("-fx-fill: white; -fx-font-size: 18;");
        
        Text version = new Text("版本: v0.1.1");
        version.setStyle("-fx-fill: #CCCCCC;");
        
        Hyperlink githubLink = new Hyperlink("GitHub仓库");
        githubLink.setStyle("-fx-text-fill: #00B4FF; -fx-border-color: transparent;");
        
        panel.getChildren().addAll(title, new Separator(), version, githubLink);
        return panel;
    }
}