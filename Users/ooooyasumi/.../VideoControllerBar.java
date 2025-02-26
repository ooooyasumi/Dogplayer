// ... 省略上文 ...
        // 修改进度条约束
        progressSlider.setMinWidth(60);  // 进一步缩小最小宽度
        progressSlider.setPrefWidth(300);
        progressSlider.setMaxWidth(Double.MAX_VALUE);
        
        // 添加容器约束
        HBox.setHgrow(this, Priority.ALWAYS);
        this.setMaxWidth(Double.MAX_VALUE);

        // 音量条样式
// ... 省略下文 ...
