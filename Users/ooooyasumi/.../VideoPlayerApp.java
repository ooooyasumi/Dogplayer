// ... 省略上文 ...
        mediaView.fitWidthProperty().bind(mediaContainer.widthProperty());
        mediaView.fitHeightProperty().bind(mediaContainer.heightProperty().subtract(60));

        // 新增容器动态约束
        mediaContainer.prefWidthProperty().bind(scene.widthProperty());
        mediaContainer.prefHeightProperty().bind(scene.heightProperty().subtract(controllerBar.heightProperty()));
        root.prefHeightProperty().bind(scene.heightProperty());

        root.setCenter(mediaContainer);

        // 移除全屏监听中的手动布局代码
        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                Platform.runLater(root::requestLayout); // 简化的布局刷新
            }
        });

        // 添加自适应布局监听
        scene.widthProperty().addListener((obs, old, newVal) -> 
            Platform.runLater(root::requestLayout));
        scene.heightProperty().addListener((obs, old, newVal) -> 
            Platform.runLater(root::requestLayout));