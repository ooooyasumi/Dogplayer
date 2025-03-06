public class ThemeManager {
    // 修改前
    private static final Map<String, String> THEMES = new HashMap<>() {{
        put("暗黑主题", "/themes/dark.css");
        put("浅色主题", "/themes/light.css");
        put("护眼模式", "/themes/green.css");
    }};
    
    // 修改后（添加泛型类型声明）
    private static final Map<String, String> THEMES = new HashMap<String, String>() {{
        put("暗黑主题", "/themes/dark.css");
        put("浅色主题", "/themes/light.css");
        put("护眼模式", "/themes/green.css");
    }};
}
