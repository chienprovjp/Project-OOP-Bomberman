package bomberman.component;

import java.awt.Color;

/**
 * Quản lý 3 cấp độ khó của Game (DỄ, THƯỜNG, KHÓ) theo chuẩn OOP.
 */
public enum Difficulty {
    EASY("DỄ", 0.40, 0.25, 3, false, 1.0,
            new Color(34, 197, 94),   // #22c55e (Xanh lá)
            new Color(22, 101, 52),   // Xanh đậm nền hover
            new Color(74, 222, 128),  // Xanh sáng viền hover
            "Item rơi nhiều (40% gạch, 25% quái) | Giữ nguyên chỉ số qua màn"),

    NORMAL("THƯỜNG", 0.25, 0.12, 3, false, 1.5,
            new Color(234, 179, 8),   // #eab308 (Vàng cam)
            new Color(161, 98, 7),    // Vàng cam nền hover
            new Color(253, 224, 71),  // Vàng sáng viền hover
            "Cân bằng (25% gạch, 12% quái) | Giữ nguyên chỉ số qua màn"),

    HARD("KHÓ", 0.12, 0.00, 2, true, 2.5,
            new Color(239, 68, 68),   // #ef4444 (Đỏ)
            new Color(185, 28, 28),   // Đỏ đậm nền hover
            new Color(252, 165, 165), // Đỏ sáng viền hover
            "Item cực hiếm (12% gạch, 0% quái) | QUA MÀN LÀ RESET SẠCH VẬT PHẨM");

    private final String displayName;
    private final double brickDropRate;
    private final double enemyDropRate;
    private final int initialLives;
    private final boolean resetItemsOnNextLevel;
    private final double scoreMultiplier;
    private final Color themeColor;
    private final Color hoverBgColor;
    private final Color hoverBorderColor;
    private final String description;

    Difficulty(String displayName, double brickDropRate, double enemyDropRate,
               int initialLives, boolean resetItemsOnNextLevel, double scoreMultiplier,
               Color themeColor, Color hoverBgColor, Color hoverBorderColor, String description) {
        this.displayName = displayName;
        this.brickDropRate = brickDropRate;
        this.enemyDropRate = enemyDropRate;
        this.initialLives = initialLives;
        this.resetItemsOnNextLevel = resetItemsOnNextLevel;
        this.scoreMultiplier = scoreMultiplier;
        this.themeColor = themeColor;
        this.hoverBgColor = hoverBgColor;
        this.hoverBorderColor = hoverBorderColor;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBrickDropRate() {
        return brickDropRate;
    }

    public double getEnemyDropRate() {
        return enemyDropRate;
    }

    public int getInitialLives() {
        return initialLives;
    }

    public boolean isResetItemsOnNextLevel() {
        return resetItemsOnNextLevel;
    }

    public double getScoreMultiplier() {
        return scoreMultiplier;
    }

    public Color getThemeColor() {
        return themeColor;
    }

    public Color getHoverBgColor() {
        return hoverBgColor;
    }

    public Color getHoverBorderColor() {
        return hoverBorderColor;
    }

    public String getDescription() {
        return description;
    }
}
