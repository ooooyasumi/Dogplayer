private VBox createGeneralSettings() {
    // ... 原有代码 ...
    
    // 在音量滑块后添加主题选择
    Label themeLabel = new Label("界面主题:");
    themeLabel.setStyle("-fx-text-fill: white;");
    
    ComboBox<String> themeBox = new ComboBox<>();
    themeBox.getItems().addAll("暗黑主题", "浅色主题", "护眼模式");
    themeBox.setValue("暗黑主题");
    themeBox.getStyleClass().add("theme-combo");
    
    panel.getChildren().addAll(new Separator(), themeLabel, themeBox);
    return panel;
}
