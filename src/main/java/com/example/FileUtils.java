package com.example;

// 导入JavaFX和相关库
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

/**
 * 文件工具类，提供与文件操作相关的实用方法
 */
public class FileUtils {

    /**
     * 打开文件选择器，让用户选择视频文件
     * 
     * @param stage 当前舞台（用于显示文件选择器对话框）
     * @return 用户选择的视频文件对象，如果用户取消选择则返回 null
     */
    public static File chooseVideoFile(Stage stage) {
        // 创建文件选择器实例
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择视频文件"); // 设置文件选择器标题

        // 添加文件扩展名过滤器，限制用户只能选择支持的视频文件格式
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("视频文件", "*.mp4", "*.flv", "*.mkv", "*.avi")
        );

        // 显示文件选择器对话框并返回用户选择的文件
        return fileChooser.showOpenDialog(stage);
    }
}