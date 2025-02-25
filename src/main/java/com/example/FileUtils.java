package com.example;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class FileUtils {
    public static File chooseVideoFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择视频文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.flv")
        );
        return fileChooser.showOpenDialog(stage);
    }
}