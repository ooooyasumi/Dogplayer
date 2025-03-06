package com.example;

import java.util.prefs.Preferences;
// 添加缺失的导入
import javafx.application.HostServices;

public class ServiceLocator {
  private static Preferences preferences;
  private static HostServices hostServices; // 现在可以识别该类型

  // ... 其他代码保持不变 ...

  public static void setHostServices(HostServices services) {
    hostServices = services;
  }

  public static HostServices getHostServices() {
    return hostServices;
  }
}
