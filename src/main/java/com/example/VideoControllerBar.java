package com.example;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class VideoControllerBar extends HBox {
    private Button playButton;
    private Button pauseButton;

    public VideoControllerBar() {
        playButton = new Button("播放");
        pauseButton = new Button("暂停");
        this.getChildren().addAll(playButton, pauseButton);
        this.setSpacing(10);
        this.setStyle("-fx-padding: 10;");
    }

    public void setPlayAction(Runnable action) {
        playButton.setOnAction(e -> action.run());
    }

    public void setPauseAction(Runnable action) {
        pauseButton.setOnAction(e -> action.run());
    }
}