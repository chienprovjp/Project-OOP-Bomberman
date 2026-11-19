package bomberman.component;

import java.awt.Color;

/**
 * Quản lý 3 cấp độ khó của Game (DỄ, THƯỜNG, KHÓ) theo chuẩn OOP.
 */
public enum Difficulty {
    EASY("DỄ", 0.40, 0.25, 3, false, false, 1.0,
            new Color(34, 197, 94),   // #22c55e (Xanh lá)
            new Color(22, 101, 52),   // Xanh đậm nền hover
            new Color(74, 222, 128),  // Xanh sáng viền hover
            "Item rơi nhiều (40%) | AI: BFS tầm ngắn | Thua: Chơi lại ngay tại ải hiện tại"),

    NORMAL("THƯỜNG", 0.25, 0.12, 3, false, false, 1.5,
            new Color(234, 179, 8),   // #eab308 (Vàng cam)
            new Color(161, 98, 7),    // Vàng cam nền hover
            new Color(253, 224, 71),  // Vàng sáng viền hover
            "Cân bằng (25%) | AI: BFS + A* | Game Over: Lùi về ải đầu của vùng (1, 4, 7)"),

    HARD("KHÓ", 0.12, 0.00, 2, true, true, 2.5,
            new Color(239, 68, 68),   // #ef4444 (Đỏ)
            new Color(185, 28, 28),   // Đỏ đậm nền hover
            new Color(252, 165, 165), // Đỏ sáng viền hover
            "Item hiếm (12%) | AI: A* toàn map & né bom | Chết mất đồ, Thua về Màn 1");

    private final String displayName;
    private final double brickDropRate;
    private final double enemyDropRate;
    private final int initialLives;
    private final boolean resetItemsOnNextLevel;
    private final boolean resetItemsOnDeath;
    private final double scoreMultiplier;
    private final Color themeColor;
    private final Color hoverBgColor;
    private final Color hoverBorderColor;
    private final String description;

    Difficulty(String displayName, double brickDropRate, double enemyDropRate,
               int initialLives, boolean resetItemsOnNextLevel, boolean resetItemsOnDeath,
               double scoreMultiplier, Color themeColor, Color hoverBgColor,
               Color hoverBorderColor, String description) {
        this.displayName = displayName;
        this.brickDropRate = brickDropRate;
        this.enemyDropRate = enemyDropRate;
        this.initialLives = initialLives;
        this.resetItemsOnNextLevel = resetItemsOnNextLevel;
        this.resetItemsOnDeath = resetItemsOnDeath;
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

    public boolean isResetItemsOnDeath() {
        return resetItemsOnDeath;
    }

    /**
     * Tính toán màn chơi hồi sinh khi bị Game Over tùy theo độ khó:
     * - EASY: Giữ nguyên ải vừa thua.
     * - NORMAL: Lùi về ải đầu tiên của vùng địa hình đó (1-3 -> 1, 4-6 -> 4, 7-9 -> 7).
     * - HARD: Reset toàn bộ về Ải 1 (Map Rừng).
     */
    public int getRespawnLevel(int currentLevel) {
        int safeLevel = Math.max(1, currentLevel);
        return switch (this) {
            case EASY   -> safeLevel;
            case NORMAL -> ((safeLevel - 1) / 3) * 3 + 1;
            case HARD   -> 1;
        };
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

