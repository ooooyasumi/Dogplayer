public class StyleConstants {
    public static final String DARK_BACKGROUND = "-fx-background-color: #2B2B2B;";
    public static final String WHITE_TEXT = "-fx-text-fill: white;";
    public static final String HOVER_BUTTON = "-fx-background-color: rgba(255,255,255,0.1);";
    
    public static String accentButton(String hexColor) {
        return String.format("-fx-background-color: %s; -fx-text-fill: white;", hexColor);
    }
}
