package bomberman.ui;

import bomberman.component.Difficulty;
import bomberman.component.GameState;
import bomberman.model.GameMap;
import bomberman.model.MapData;
import bomberman.util.GameSaveData;
import bomberman.util.ResourceLoader;
import bomberman.util.SaveManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Menu {
    private int selectedIndex = 0;
    private int hoveredIndex = -1;
    private Rectangle[] menuBounds;

    // Trạng thái nút cho màn hình Hướng dẫn
    private Rectangle helpBackBounds;
    private boolean isHelpBackHovered = false;

    // Trạng thái nút cho màn hình Tạm dừng
    private final String[] pauseOptions = { "TIẾP TỤC", "CHƠI LẠI", "MENU CHÍNH" };
    private Rectangle[] pauseBounds;
    private int hoveredPauseIndex = -1;

    // Trạng thái cho hệ thống 3 Độ Khó (DỄ, THƯỜNG, KHÓ)
    private Difficulty selectedDifficulty = Difficulty.NORMAL;
    private Rectangle[] difficultyBounds;
    private int hoveredDifficultyIndex = -1;

    // Tọa độ chuẩn 9 màn chơi bám sát các bệ đá tròn trên con đường bản đồ (canvas chuẩn 1280x720)
    private static final double[][] LEVEL_COORDINATES = {
        { 115.0 / 1280.0, 260.0 / 720.0 }, // Màn 1: Cứ điểm bệ đá đường trên Rừng Xanh
        { 110.0 / 1280.0, 540.0 / 720.0 }, // Màn 2: Cứ điểm khúc cua đường dưới Rừng Xanh
        { 445.0 / 1280.0, 465.0 / 720.0 }, // Màn 3: Cứ điểm bệ đá đường dốc Rừng Xanh
        { 590.0 / 1280.0, 325.0 / 720.0 }, // Màn 4: Cứ điểm cổng ranh giới sang Khu Đồ Chơi
        { 790.0 / 1280.0, 255.0 / 720.0 }, // Màn 5: Cứ điểm đỉnh vòm Khu Đồ Chơi
        { 805.0 / 1280.0, 595.0 / 720.0 }, // Màn 6: Cứ điểm cổng dưới Khu Đồ Chơi
        { 1160.0 / 1280.0, 260.0 / 720.0 },// Màn 7: Cứ điểm đường nhập cảnh Núi Lửa
        { 1205.0 / 1280.0, 345.0 / 720.0 },// Màn 8: Cứ điểm bờ hồ dung nham Núi Lửa
        { 1040.0 / 1280.0, 490.0 / 720.0 } // Màn 9: Cứ điểm lòng chảo hạ lưu Núi Lửa
    };

    private Rectangle[] levelNodeBounds = new Rectangle[9];
    private int hoveredLevelNode = -1;
    private Rectangle levelBackBounds;
    private boolean isLevelBackHovered = false;

    // Trạng thái nút cho màn hình Game Over
    private final String[] gameOverOptions = { "CHƠI LẠI [R]", "CHỌN MÀN [L]", "MENU CHÍNH [M]" };
    private Rectangle[] gameOverBounds;
    private int hoveredGameOverIndex = -1;

    // Trạng thái nút cho màn hình Chiến thắng thường (Level 1, 2)
    private final String[] victoryOptions = { "MÀN TIẾP THEO [ENTER]", "CHƠI LẠI MÀN [R]", "CHỌN MÀN [L]", "MENU CHÍNH [M]" };
    private Rectangle[] victoryBounds;
    private int hoveredVictoryIndex = -1;

    // Trạng thái nút khi Hoàn thành tất cả các màn (Endgame)
    private final String[] finalVictoryOptions = { "CHƠI LẠI TỪ ĐẦU [ENTER]", "CHỌN MÀN [L]", "MENU CHÍNH [M]" };
    private Rectangle[] finalVictoryBounds;
    private int hoveredFinalVictoryIndex = -1;

    private int animTick = 0;
    private BufferedImage playerIcon;
    private BufferedImage backgroundImage;
    private BufferedImage backgroundLevelImage;
    private BufferedImage playButtonImage;
    private BufferedImage itemBombIcon;
    private BufferedImage itemFlameIcon;
    private BufferedImage itemSpeedIcon;
    private BufferedImage itemLifeIcon;
    private BufferedImage portalIcon;

    public Menu() {
        loadAssets();
        updateBounds(GameMap.DEFAULT_COLS * GameMap.TILE_SIZE, GameMap.DEFAULT_ROWS * GameMap.TILE_SIZE);
    }

    private void loadAssets() {
        backgroundImage = ResourceLoader.loadImage("assets/Background.png");
        if (backgroundImage == null) {
            backgroundImage = ResourceLoader.loadImage("assets/background.png");
        }

        backgroundLevelImage = ResourceLoader.loadImage("assets/backgroundlevel.png");

        playButtonImage = ResourceLoader.loadImage("assets/Buttons/Play.png");
        if (playButtonImage == null) {
            playButtonImage = ResourceLoader.loadImage("assets/Buttons/play.png");
        }

        playerIcon = ResourceLoader.loadImage("player_down.png");
        if (playerIcon == null) {
            playerIcon = ResourceLoader.loadImage("player.png");
        }

        itemBombIcon = ResourceLoader.loadImage("item_bomb.png");
        itemFlameIcon = ResourceLoader.loadImage("item_flame.png");
        itemSpeedIcon = ResourceLoader.loadImage("item_speed.png");
        itemLifeIcon = ResourceLoader.loadImage("item_life.png");
        portalIcon = ResourceLoader.loadImage("portal.png");
    }

    /**
     * Lấy danh sách các tùy chọn Menu chính động:
     * Nếu có bản lưu ván dở dang -> Hiện nút TIẾP TỤC [MÀN X] lên đầu.
     */
    public String[] getCurrentMenuOptions() {
        if (SaveManager.hasOngoingGame()) {
            GameSaveData data = SaveManager.loadProgress();
            return new String[] {
                "TIẾP TỤC [MÀN " + data.getLevel() + "]",
                "CHƠI MỚI",
                "HƯỚNG DẪN",
                "THOÁT GAME"
            };
        } else {
            return new String[] {
                "CHƠI NGAY",
                "HƯỚNG DẪN",
                "THOÁT GAME"
            };
        }
    }

    /**
     * Tự động tính toán vị trí nút responsive theo kích thước màn hình
     */
    public void updateBounds(int width, int height) {
        if (width <= 0) width = GameMap.DEFAULT_COLS * GameMap.TILE_SIZE;
        if (height <= 0) height = GameMap.DEFAULT_ROWS * GameMap.TILE_SIZE;

        // --- 1. Menu chính ---
        String[] opts = getCurrentMenuOptions();
        int optCount = opts.length;
        boolean hasOngoing = (optCount == 4);

        int btnW = (int) (width * 0.24);
        if (btnW < 190) btnW = 190;
        if (btnW > 250) btnW = 250;

        int playH = (!hasOngoing) ? (int) (btnW * (265.0 / 940.0)) : Math.max(38, (int) (btnW * 0.18));
        int subBtnH = Math.max(34, (int) (btnW * 0.16));

        int startX = (int) (width * 0.08);
        int startY = hasOngoing ? (int) (height * 0.38) : (int) (height * 0.44);

        if (menuBounds == null || menuBounds.length != optCount) {
            menuBounds = new Rectangle[optCount];
        }

        int currY = startY;
        for (int i = 0; i < optCount; i++) {
            int h = (i == 0 && !hasOngoing) ? playH : subBtnH;
            menuBounds[i] = new Rectangle(startX, currY, btnW, h);
            currY += h + (hasOngoing ? 10 : 12);
        }

        // --- 2. Help Back ---
        int helpBtnW = Math.max(180, (int) (width * 0.2));
        helpBackBounds = new Rectangle((width - helpBtnW) / 2, height - 70, helpBtnW, 38);

        // --- 3. Pause Menu ---
        int pauseBtnW = Math.max(190, (int) (width * 0.22));
        int pauseBtnH = 38;
        int pauseStartX = (width - pauseBtnW) / 2;
        int pauseSpacing = 48;
        int pauseStartY = (height - (pauseOptions.length * pauseSpacing)) / 2 + 25;
        if (pauseBounds == null || pauseBounds.length != pauseOptions.length) {
            pauseBounds = new Rectangle[pauseOptions.length];
        }
        for (int i = 0; i < pauseOptions.length; i++) {
            pauseBounds[i] = new Rectangle(pauseStartX, pauseStartY + i * pauseSpacing, pauseBtnW, pauseBtnH);
        }

        // --- 4. Level & Difficulty Select (Bản đồ thế giới 9 màn chơi) ---
        int diffCount = Difficulty.values().length;
        if (difficultyBounds == null || difficultyBounds.length != diffCount) {
            difficultyBounds = new Rectangle[diffCount];
        }
        int diffBtnW = Math.max(110, (int) (width * 0.11));
        int diffBtnH = 34;
        int diffGap = 12;
        int totalDiffW = diffCount * diffBtnW + (diffCount - 1) * diffGap;
        int diffStartX = (width - totalDiffW) / 2;
        int diffStartY = 76;

        for (int i = 0; i < diffCount; i++) {
            difficultyBounds[i] = new Rectangle(diffStartX + i * (diffBtnW + diffGap), diffStartY, diffBtnW, diffBtnH);
        }

        // Tọa độ 9 cứ điểm (Map Nodes) trên bản đồ
        if (levelNodeBounds == null || levelNodeBounds.length != 9) {
            levelNodeBounds = new Rectangle[9];
        }
        int nodeSize = 44;
        for (int i = 0; i < 9; i++) {
            int cx = (int) (LEVEL_COORDINATES[i][0] * width);
            int cy = (int) (LEVEL_COORDINATES[i][1] * height);
            levelNodeBounds[i] = new Rectangle(cx - nodeSize / 2, cy - nodeSize / 2, nodeSize, nodeSize);
        }

        // Nút Quay lại đặt ở góc dưới bên trái
        levelBackBounds = new Rectangle(24, height - 54, 140, 36);

        // --- 5. Game Over ---
        int goBtnW = Math.max(190, (int) (width * 0.22));
        int goBtnH = 38;
        int goStartX = (width - goBtnW) / 2;
        int goSpacing = 46;
        int goStartY = (height - (gameOverOptions.length * goSpacing)) / 2 + 55;
        if (gameOverBounds == null || gameOverBounds.length != gameOverOptions.length) {
            gameOverBounds = new Rectangle[gameOverOptions.length];
        }
        for (int i = 0; i < gameOverOptions.length; i++) {
            gameOverBounds[i] = new Rectangle(goStartX, goStartY + i * goSpacing, goBtnW, goBtnH);
        }

        // --- 6. Victory (Màn thường) ---
        int vicBtnW = Math.max(220, (int) (width * 0.25));
        int vicBtnH = 36;
        int vicStartX = (width - vicBtnW) / 2;
        int vicSpacing = 44;
        int vicStartY = (height - (victoryOptions.length * vicSpacing)) / 2 + 65;
        if (victoryBounds == null || victoryBounds.length != victoryOptions.length) {
            victoryBounds = new Rectangle[victoryOptions.length];
        }
        for (int i = 0; i < victoryOptions.length; i++) {
            victoryBounds[i] = new Rectangle(vicStartX, vicStartY + i * vicSpacing, vicBtnW, vicBtnH);
        }

        // --- 7. Final Victory (Hết toàn bộ các màn) ---
        int fVicBtnW = Math.max(230, (int) (width * 0.26));
        int fVicBtnH = 38;
        int fVicStartX = (width - fVicBtnW) / 2;
        int fVicSpacing = 48;
        int fVicStartY = (height - (finalVictoryOptions.length * fVicSpacing)) / 2 + 75;
        if (finalVictoryBounds == null || finalVictoryBounds.length != finalVictoryOptions.length) {
            finalVictoryBounds = new Rectangle[finalVictoryOptions.length];
        }
        for (int i = 0; i < finalVictoryOptions.length; i++) {
            finalVictoryBounds[i] = new Rectangle(fVicStartX, fVicStartY + i * fVicSpacing, fVicBtnW, fVicBtnH);
        }
    }

    public void tick() {
        animTick++;
    }

    // ==========================================
    // 1. VẼ MÀN HÌNH CHÍNH (MAIN MENU)
    // ==========================================
    public void drawMenu(Graphics g, int width, int height) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, width, height, null);
        } else {
            g2d.setColor(new Color(15, 23, 42));
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(new Color(250, 204, 21));
            g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
            g2d.drawString("BOMBERMAN", (width - 250) / 2, 80);
        }

        String[] opts = getCurrentMenuOptions();
        boolean hasOngoing = (opts.length == 4);

        for (int i = 0; i < opts.length; i++) {
            Rectangle r = menuBounds[i];
            boolean isHighlighted = (hoveredIndex == i || selectedIndex == i);

            if (i == 0 && !hasOngoing && playButtonImage != null) {
                if (isHighlighted) {
                    g2d.setColor(new Color(250, 204, 21, 100));
                    g2d.fillRoundRect(r.x - 4, r.y - 4, r.width + 8, r.height + 8, 16, 16);
                }
                g2d.drawImage(playButtonImage, r.x, r.y, r.width, r.height, null);
            } else if (i == 0 && hasOngoing) {
                // Nút TIẾP TỤC nổi bật
                drawContinueButton(g2d, r, opts[i], isHighlighted);
            } else {
                drawMenuButton(g2d, r, opts[i], isHighlighted);
            }
        }
    }

    private void drawContinueButton(Graphics2D g2d, Rectangle r, String text, boolean isHighlighted) {
        if (isHighlighted) {
            g2d.setColor(new Color(22, 101, 52, 240));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(74, 222, 128));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        } else {
            g2d.setColor(new Color(20, 83, 45, 200));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(34, 197, 94));
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        }

        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(isHighlighted ? new Color(240, 253, 244) : Color.WHITE);
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }

    private void drawMenuButton(Graphics2D g2d, Rectangle r, String text, boolean isHighlighted) {
        if (isHighlighted) {
            g2d.setColor(new Color(30, 41, 59, 230));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(250, 204, 21));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        } else {
            g2d.setColor(new Color(15, 23, 42, 180));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(100, 116, 139));
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        }

        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(isHighlighted ? new Color(254, 240, 138) : Color.WHITE);
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + ((r.height - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(text, tx, ty);
    }

    // ==========================================
    // 2. VẼ MÀN HÌNH CHỌN MÀN - BẢN ĐỒ THẾ GIỚI
    // ==========================================
    public void drawLevelSelect(Graphics g, int width, int height) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 1. Vẽ hình nền toàn cảnh bản đồ thế giới (1376x768 scale fit)
        if (backgroundLevelImage != null) {
            g2d.drawImage(backgroundLevelImage, 0, 0, width, height, null);
        } else {
            g2d.setColor(new Color(15, 23, 42));
            g2d.fillRect(0, 0, width, height);
        }

        // Lớp phủ mờ phần trên để thanh điều khiển và độ khó nổi bật rõ nét, giữ trọn cảnh quan con đường
        g2d.setColor(new Color(15, 23, 42, 160));
        g2d.fillRect(0, 0, width, 140);
        g2d.setColor(new Color(56, 189, 248, 80));
        g2d.drawLine(0, 140, width, 140);

        // Tiêu đề
        g2d.setFont(new Font("SansSerif", Font.BOLD, 26));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "BẢN ĐỒ THẾ GIỚI - CHỌN MÀN CHƠI";
        int titleX = (width - tfm.stringWidth(title)) / 2;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(title, titleX + 2, 40);
        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString(title, titleX, 38);

        // --- KHU VỰC CHỌN ĐỘ KHÓ (DỄ - THƯỜNG - KHÓ) ---
        Font diffFont = new Font("SansSerif", Font.BOLD, 13);
        g2d.setFont(diffFont);
        FontMetrics dfm = g2d.getFontMetrics();

        Difficulty[] diffValues = Difficulty.values();
        for (int i = 0; i < diffValues.length; i++) {
            Difficulty diff = diffValues[i];
            Rectangle r = difficultyBounds[i];
            boolean isHovered = (i == hoveredDifficultyIndex);
            boolean isSelected = (selectedDifficulty == diff);

            // Bỏ hình tròn cạnh chữ, hover/select đổi màu rực rỡ
            if (isHovered) {
                g2d.setColor(diff.getHoverBgColor());
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(diff.getHoverBorderColor());
                g2d.setStroke(new BasicStroke(2.2f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            } else if (isSelected) {
                if (diff == Difficulty.EASY) g2d.setColor(new Color(20, 83, 45, 230));
                else if (diff == Difficulty.NORMAL) g2d.setColor(new Color(133, 77, 14, 230));
                else g2d.setColor(new Color(153, 27, 27, 230));

                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(diff.getThemeColor());
                g2d.setStroke(new BasicStroke(2.2f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            } else {
                g2d.setColor(new Color(30, 41, 59, 210));
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(new Color(71, 85, 105));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            }

            String diffName = diff.getDisplayName();
            g2d.setColor(isHovered || isSelected ? Color.WHITE : new Color(148, 163, 184));
            int dtx = r.x + (r.width - dfm.stringWidth(diffName)) / 2;
            int dty = r.y + ((r.height - dfm.getHeight()) / 2) + dfm.getAscent();
            g2d.drawString(diffName, dtx, dty);
        }

        // Dòng phụ đề mô tả độ khó
        Difficulty activeDiff = (hoveredDifficultyIndex >= 0) ? diffValues[hoveredDifficultyIndex] : selectedDifficulty;
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics descFm = g2d.getFontMetrics();
        String descText = activeDiff.getDescription();
        int descX = (width - descFm.stringWidth(descText)) / 2;
        int descY = difficultyBounds[0].y + difficultyBounds[0].height + 20;
        g2d.setColor(activeDiff.getThemeColor());
        g2d.drawString(descText, descX, descY);

        // --- NHÃN 3 VÙNG ĐỊA HÌNH TRÊN BẢN ĐỒ ---
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        drawZoneBadge(g2d, (int) (width * 0.13), 158, "🌿 VÙNG RỪNG XANH (1-3)", new Color(34, 197, 94));
        drawZoneBadge(g2d, (int) (width * 0.53), 158, "🎪 KHU ĐỒ CHƠI (4-6)", new Color(14, 165, 233));
        drawZoneBadge(g2d, (int) (width * 0.86), 158, "🌋 NÚI LỬA (7-9)", new Color(239, 68, 68));

        // --- 2. VẼ 9 CỨ ĐIỂM (STAGE NODES) TRÊN CON ĐƯỜNG BẢN ĐỒ ---
        for (int i = 0; i < 9; i++) {
            Rectangle r = levelNodeBounds[i];
            boolean isHovered = (i == hoveredLevelNode);
            int levelNum = i + 1;

            // Phân loại màu sắc theo 3 cụm thế giới
            Color baseColor, glowColor, bgFill;
            String zoneName;
            if (levelNum <= 3) {
                baseColor = new Color(34, 197, 94);  // Xanh lá
                glowColor = new Color(134, 239, 172);
                bgFill = new Color(20, 83, 45);
                zoneName = "RỪNG XANH";
            } else if (levelNum <= 6) {
                baseColor = new Color(14, 165, 233); // Xanh cyan
                glowColor = new Color(186, 230, 253);
                bgFill = new Color(7, 89, 133);
                zoneName = "KHU ĐỒ CHƠI";
            } else {
                baseColor = new Color(239, 68, 68);  // Đỏ nham thạch
                glowColor = new Color(254, 202, 202);
                bgFill = new Color(153, 27, 27);
                zoneName = "NÚI LỬA";
            }

            // Hiệu ứng nhấp nhô nhẹ (Floating)
            int bounce = (int) (Math.sin((animTick + i * 18) * 0.12) * 3);
            int cx = r.x + r.width / 2;
            int cy = r.y + r.height / 2 + bounce;
            int radius = isHovered ? 26 : 22;

            // Bóng mờ đổ xuống mặt đường
            g2d.setColor(new Color(0, 0, 0, 95));
            g2d.fillOval(cx - 20, r.y + r.height - 4, 40, 10);

            // Vòng hào quang phát sáng khi rê chuột
            if (isHovered) {
                g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 90));
                g2d.fillOval(cx - radius - 8, cy - radius - 8, (radius + 8) * 2, (radius + 8) * 2);
                g2d.setColor(glowColor);
                g2d.setStroke(new BasicStroke(2.8f));
                g2d.drawOval(cx - radius - 2, cy - radius - 2, (radius + 2) * 2, (radius + 2) * 2);
            }

            // Thân nút tròn cứ điểm
            g2d.setColor(isHovered ? baseColor : bgFill);
            g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Viền nút
            g2d.setColor(isHovered ? Color.WHITE : baseColor);
            g2d.setStroke(new BasicStroke(isHovered ? 2.5f : 2.0f));
            g2d.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Vệt bóng ánh sáng cong phía trên tạo độ nổi 3D
            g2d.setColor(new Color(255, 255, 255, 110));
            g2d.fillOval(cx - radius + 5, cy - radius + 3, radius * 2 - 10, radius - 2);

            // Số thứ tự màn chơi
            g2d.setFont(new Font("SansSerif", Font.BOLD, isHovered ? 18 : 16));
            FontMetrics nfm = g2d.getFontMetrics();
            String numStr = String.valueOf(levelNum);
            int nx = cx - nfm.stringWidth(numStr) / 2;
            int ny = cy + nfm.getAscent() / 2 - 2;

            // Đổ bóng chữ
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.drawString(numStr, nx + 1, ny + 1);
            g2d.setColor(Color.WHITE);
            g2d.drawString(numStr, nx, ny);

            // Thẻ thông tin nổi (Tooltip) khi hover vào nút
            if (isHovered) {
                drawStageTooltip(g2d, cx, cy - radius - 10, levelNum, zoneName, baseColor);
            }
        }

        // --- 3. NÚT QUAY LẠI [ESC] ---
        if (levelBackBounds != null) {
            Rectangle rb = levelBackBounds;
            if (isLevelBackHovered) {
                g2d.setColor(new Color(220, 38, 38));
                g2d.fillRoundRect(rb.x, rb.y, rb.width, rb.height, 8, 8);
                g2d.setColor(new Color(252, 165, 165));
                g2d.drawRoundRect(rb.x, rb.y, rb.width, rb.height, 8, 8);
            } else {
                g2d.setColor(new Color(30, 41, 59, 230));
                g2d.fillRoundRect(rb.x, rb.y, rb.width, rb.height, 8, 8);
                g2d.setColor(new Color(71, 85, 105));
                g2d.drawRoundRect(rb.x, rb.y, rb.width, rb.height, 8, 8);
            }

            g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics bfm = g2d.getFontMetrics();
            String bText = "QUAY LẠI [ESC]";
            g2d.setColor(Color.WHITE);
            int btx = rb.x + (rb.width - bfm.stringWidth(bText)) / 2;
            int bty = rb.y + ((rb.height - bfm.getHeight()) / 2) + bfm.getAscent();
            g2d.drawString(bText, btx, bty);
        }
    }

    private void drawZoneBadge(Graphics2D g2d, int cx, int cy, String text, Color color) {
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(text);
        int bx = cx - tw / 2 - 10;
        int by = cy - 14;
        g2d.setColor(new Color(15, 23, 42, 220));
        g2d.fillRoundRect(bx, by, tw + 20, 22, 12, 12);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(bx, by, tw + 20, 22, 12, 12);
        g2d.drawString(text, cx - tw / 2, cy + 2);
    }

    private void drawStageTooltip(Graphics2D g2d, int cx, int cy, int level, String zone, Color theme) {
        String line1 = "MÀN " + level + " • " + zone;
        String line2 = "Click chuột để xuất trận!";
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int cardW = Math.max(fm.stringWidth(line1), fm.stringWidth(line2)) + 24;
        int cardH = 46;
        int cardX = cx - cardW / 2;
        int cardY = cy - cardH;

        g2d.setColor(new Color(15, 23, 42, 245));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 10, 10);
        g2d.setColor(theme);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 10, 10);

        g2d.drawString(line1, cardX + (cardW - fm.stringWidth(line1)) / 2, cardY + 18);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2d.setColor(Color.WHITE);
        g2d.drawString(line2, cardX + (cardW - g2d.getFontMetrics().stringWidth(line2)) / 2, cardY + 36);
    }

    // ==========================================
    // 3. VẼ MÀN HÌNH CHIẾN THẮNG (VICTORY)
    // ==========================================
    public void drawVictory(Graphics g, int width, int height, int score, int highScore, int level) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền tối bán trong suốt
        g2d.setColor(new Color(15, 23, 42, 230));
        g2d.fillRect(0, 0, width, height);

        boolean isFinal = (level >= MapData.LEVEL_COUNT);

        int cardW = Math.min(480, width - 40);
        int cardH = Math.min(isFinal ? 330 : 360, height - 40);
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2;

        // Thân khung thông báo
        g2d.setColor(new Color(30, 41, 59, 250));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g2d.setColor(isFinal ? new Color(250, 204, 21) : new Color(34, 197, 94));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        // Tiêu đề
        g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = isFinal ? "CHÚC MỪNG CHIẾN THẮNG!" : "QUA MÀN THÀNH CÔNG!";
        int tx = cardX + (cardW - tfm.stringWidth(title)) / 2;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(title, tx + 2, cardY + 44);
        g2d.setColor(isFinal ? new Color(250, 204, 21) : new Color(74, 222, 128));
        g2d.drawString(title, tx, cardY + 42);

        // Dòng phụ chú
        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        FontMetrics subFm = g2d.getFontMetrics();
        String subText = isFinal 
            ? "BẠN ĐÃ HOÀN THÀNH HẾT CÁC MÀN CHƠI!" 
            : "Đã tiêu diệt toàn bộ quái vật ở Màn " + level + "!";
        int subX = cardX + (cardW - subFm.stringWidth(subText)) / 2;
        g2d.setColor(isFinal ? new Color(253, 224, 71) : new Color(226, 232, 240));
        g2d.drawString(subText, subX, cardY + 74);

        // Điểm số & Kỷ lục
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics sfm = g2d.getFontMetrics();
        String scoreStr = "ĐIỂM HIỆN TẠI: " + score + "   |   KỶ LỤC: " + highScore;
        int sx = cardX + (cardW - sfm.stringWidth(scoreStr)) / 2;
        g2d.setColor(new Color(56, 189, 248));
        g2d.drawString(scoreStr, sx, cardY + 102);

        // Vẽ các nút
        Font btnFont = new Font("SansSerif", Font.BOLD, 13);
        g2d.setFont(btnFont);
        FontMetrics bfm = g2d.getFontMetrics();

        String[] currentOptions = isFinal ? finalVictoryOptions : victoryOptions;
        Rectangle[] currentBounds = isFinal ? finalVictoryBounds : victoryBounds;
        int currentHovered = isFinal ? hoveredFinalVictoryIndex : hoveredVictoryIndex;

        for (int i = 0; i < currentOptions.length; i++) {
            Rectangle r = currentBounds[i];
            boolean isHovered = (i == currentHovered);

            if (isHovered) {
                if (i == 0) g2d.setColor(new Color(34, 197, 94)); // Xanh lá
                else if (i == 1) g2d.setColor(new Color(14, 165, 233)); // Xanh dương
                else g2d.setColor(new Color(100, 116, 139));

                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            } else {
                g2d.setColor(new Color(51, 65, 85));
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(new Color(100, 116, 139));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            }

            String text = currentOptions[i];
            g2d.setColor(Color.WHITE);
            int bx = r.x + (r.width - bfm.stringWidth(text)) / 2;
            int by = r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent();
            g2d.drawString(text, bx, by);
        }
    }

    // ==========================================
    // 4. VẼ MÀN HÌNH GAME OVER
    // ==========================================
    public void drawGameOver(Graphics g, int width, int height, int score, int highScore, int level) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lớp phủ màu tối đỏ
        g2d.setColor(new Color(15, 23, 42, 230));
        g2d.fillRect(0, 0, width, height);

        int cardW = Math.min(380, width - 60);
        int cardH = Math.min(310, height - 60);
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2;

        // Hộp thông tin Game Over
        g2d.setColor(new Color(30, 41, 59, 250));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g2d.setColor(new Color(239, 68, 68));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        // Tiêu đề GAME OVER
        g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "GAME OVER";
        int tx = cardX + (cardW - tfm.stringWidth(title)) / 2;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(title, tx + 2, cardY + 47);
        g2d.setColor(new Color(239, 68, 68));
        g2d.drawString(title, tx, cardY + 45);

        // Thông tin điểm số và kỷ lục
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics sfm = g2d.getFontMetrics();

        String scoreStr = "ĐIỂM ĐẠT ĐƯỢC: " + score;
        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString(scoreStr, cardX + (cardW - sfm.stringWidth(scoreStr)) / 2, cardY + 80);

        String highStr = "ĐIỂM KỶ LỤC: " + highScore;
        g2d.setColor(new Color(56, 189, 248));
        g2d.drawString(highStr, cardX + (cardW - sfm.stringWidth(highStr)) / 2, cardY + 104);

        String levelStr = "MÀN HIỆN TẠI: Màn " + level;
        g2d.setColor(new Color(203, 213, 225));
        g2d.drawString(levelStr, cardX + (cardW - sfm.stringWidth(levelStr)) / 2, cardY + 128);

        // Vẽ 3 nút lựa chọn
        Font btnFont = new Font("SansSerif", Font.BOLD, 13);
        g2d.setFont(btnFont);
        FontMetrics bfm = g2d.getFontMetrics();

        for (int i = 0; i < gameOverOptions.length; i++) {
            Rectangle r = gameOverBounds[i];
            boolean isHovered = (i == hoveredGameOverIndex);

            if (isHovered) {
                if (i == 0) g2d.setColor(new Color(34, 197, 94)); // Xanh lá
                else if (i == 1) g2d.setColor(new Color(14, 165, 233)); // Xanh dương
                else g2d.setColor(new Color(220, 38, 38)); // Đỏ

                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            } else {
                g2d.setColor(new Color(51, 65, 85));
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(new Color(100, 116, 139));
                g2d.setStroke(new BasicStroke(1.2f));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            }

            String text = gameOverOptions[i];
            g2d.setColor(Color.WHITE);
            int bx = r.x + (r.width - bfm.stringWidth(text)) / 2;
            int by = r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent();
            g2d.drawString(text, bx, by);
        }
    }

    // ==========================================
    // 5. VẼ MÀN HÌNH TẠM DỪNG (PAUSE)
    // ==========================================
    public void drawPause(Graphics g, int width, int height) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRect(0, 0, width, height);

        int cardW = Math.min(340, width - 60);
        int cardH = Math.min(250, height - 80);
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2;

        g2d.setColor(new Color(30, 41, 59, 240));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 14, 14);
        g2d.setColor(new Color(245, 158, 11));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics tfm = g2d.getFontMetrics();
        String pauseTitle = "TẠM DỪNG";
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(pauseTitle, cardX + (cardW - tfm.stringWidth(pauseTitle)) / 2, cardY + 36);

        Font btnFont = new Font("SansSerif", Font.BOLD, 13);
        g2d.setFont(btnFont);
        FontMetrics bfm = g2d.getFontMetrics();

        for (int i = 0; i < pauseOptions.length; i++) {
            Rectangle r = pauseBounds[i];
            boolean isHovered = (i == hoveredPauseIndex);

            if (isHovered) {
                g2d.setColor(new Color(37, 99, 235));
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(new Color(147, 197, 253));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            } else {
                g2d.setColor(new Color(51, 65, 85));
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
                g2d.setColor(new Color(100, 116, 139));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            }

            String text = pauseOptions[i];
            g2d.setColor(Color.WHITE);
            int tx = r.x + (r.width - bfm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent();
            g2d.drawString(text, tx, ty);
        }
    }

    // ==========================================
    // 6. VẼ MÀN HÌNH HƯỚNG DẪN (HELP)
    // ==========================================
    public void drawHelp(Graphics g, int width, int height) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(new Color(15, 23, 42));
        g2d.fillRect(0, 0, width, height);

        int panelW = Math.min(940, width - 40);
        int panelH = Math.min(520, height - 80);
        int px = (width - panelW) / 2;
        int py = 15;

        g2d.setColor(new Color(30, 41, 59, 235));
        g2d.fillRoundRect(px, py, panelW, panelH, 16, 16);
        g2d.setColor(new Color(71, 85, 105));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(px, py, panelW, panelH, 16, 16);

        // Header
        g2d.setFont(new Font("SansSerif", Font.BOLD, 21));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "HƯỚNG DẪN ĐIỀU KHIỂN & HỆ THỐNG VẬT PHẨM (ITEMS)";
        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString(title, px + (panelW - tfm.stringWidth(title)) / 2, py + 34);

        int midX = px + panelW / 2;

        // Đường phân cách giữa 2 cột
        g2d.setColor(new Color(51, 65, 85));
        g2d.drawLine(midX, py + 50, midX, py + panelH - 25);

        // ========================================================
        // CỘT TRÁI: ĐIỀU KHIỂN & LUẬT CHƠI
        // ========================================================
        int col1X = px + 28;
        int textY = py + 72;
        int lineGap = 26;

        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2d.setColor(new Color(56, 189, 248)); // Xanh Cyan
        g2d.drawString("🎮 ĐIỀU KHIỂN & LUẬT CHƠI", col1X, textY);
        textY += lineGap + 4;

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));

        g2d.drawString("• DI CHUYỂN: Dùng phím W / A / S / D hoặc 4 phím mũi tên.", col1X, textY);
        textY += lineGap;
        g2d.drawString("• ĐẶT BOM: Nhấn phím SPACE để đặt bom tại ô đang đứng.", col1X, textY);
        textY += lineGap;
        g2d.drawString("• TẠM DỪNG: Nhấn ESC để tạm dừng hoặc mở menu tùy chọn.", col1X, textY);
        textY += lineGap;
        g2d.drawString("• LƯU TIẾN TRÌNH: Tự động lưu khi về Menu [M] hoặc tắt cửa sổ.", col1X, textY);
        textY += lineGap;
        g2d.drawString("• TIẾP TỤC: Chọn 'TIẾP TỤC [MÀN X]' ở Menu để chơi tiếp.", col1X, textY);
        textY += lineGap;
        g2d.drawString("• MỤC TIÊU: Đặt bom phá gạch, tiêu diệt toàn bộ quái vật Ballom.", col1X, textY);
        textY += lineGap;
        g2d.setColor(new Color(248, 113, 113)); // Đỏ cảnh báo
        g2d.drawString("• CẨN THẬN: Tránh xa tia lửa bom và không để quái chạm vào!", col1X, textY);
        textY += lineGap + 10;

        // 3 Cấp độ khó
        g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2d.setColor(new Color(168, 85, 247)); // Tím neon
        g2d.drawString("🏆 3 CẤP ĐỘ ĐỘ KHÓ (CHỌN TẠI MÀN HÌNH CHỌN MÀN):", col1X, textY);
        textY += 22;

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(74, 222, 128));
        g2d.drawString("• DỄ: Rơi item nhiều (40% gạch, 25% quái), giữ nguyên chỉ số qua màn.", col1X, textY);
        textY += 20;

        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString("• THƯỜNG: Cân bằng (25% gạch, 12% quái), giữ nguyên chỉ số qua màn.", col1X, textY);
        textY += 20;

        g2d.setColor(new Color(248, 113, 113));
        g2d.drawString("• KHÓ: Item hiếm (12% gạch, 0% quái), QUA MÀN RESET SẠCH CHỈ SỐ!", col1X, textY);

        // ========================================================
        // CỘT PHẢI: HỆ THỐNG VẬT PHẨM (ITEMS) & CỔNG
        // ========================================================
        int col2X = midX + 28;
        int itemY = py + 72;
        int iconSize = 22;

        g2d.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2d.setColor(new Color(250, 204, 21)); // Vàng Gold
        g2d.drawString("🎁 VẬT PHẨM (ITEMS) & CỔNG QUA MÀN", col2X, itemY);
        itemY += 24;

        g2d.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2d.setColor(new Color(148, 163, 184));
        g2d.drawString("(Rơi ra khi nổ gạch ~40% hoặc tiêu diệt quái ~25%)", col2X, itemY);
        itemY += lineGap - 2;

        // 1. Bomb Item
        if (itemBombIcon != null) g2d.drawImage(itemBombIcon, col2X, itemY - 14, iconSize, iconSize, null);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(253, 224, 71));
        g2d.drawString("BOM (Bomb Up):", col2X + iconSize + 8, itemY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));
        g2d.drawString("Tăng số bom đặt cùng lúc (+1).", col2X + iconSize + 130, itemY);
        itemY += lineGap + 2;

        // 2. Flame Item
        if (itemFlameIcon != null) g2d.drawImage(itemFlameIcon, col2X, itemY - 14, iconSize, iconSize, null);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(249, 115, 22));
        g2d.drawString("LỬA (Fire Up):", col2X + iconSize + 8, itemY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));
        g2d.drawString("Tăng độ dài tia nổ thêm 1 ô (+1).", col2X + iconSize + 130, itemY);
        itemY += lineGap + 2;

        // 3. Speed Item
        if (itemSpeedIcon != null) g2d.drawImage(itemSpeedIcon, col2X, itemY - 14, iconSize, iconSize, null);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(56, 189, 248));
        g2d.drawString("GIÀY (Speed Up):", col2X + iconSize + 8, itemY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));
        g2d.drawString("Tăng tốc độ di chuyển nhân vật.", col2X + iconSize + 130, itemY);
        itemY += lineGap + 2;

        // 4. Life Item
        if (itemLifeIcon != null) g2d.drawImage(itemLifeIcon, col2X, itemY - 14, iconSize, iconSize, null);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(239, 68, 68));
        g2d.drawString("TIM (Life Up):", col2X + iconSize + 8, itemY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));
        g2d.drawString("Cộng thêm 1 mạng chơi (+1).", col2X + iconSize + 130, itemY);
        itemY += lineGap + 2;

        // 5. Portal
        if (portalIcon != null) g2d.drawImage(portalIcon, col2X, itemY - 14, iconSize, iconSize, null);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2d.setColor(new Color(168, 85, 247));
        g2d.drawString("CỔNG (Portal):", col2X + iconSize + 8, itemY);
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2d.setColor(new Color(241, 245, 249));
        g2d.drawString("Diệt hết quái cổng sẽ mở để qua màn!", col2X + iconSize + 130, itemY);
        itemY += lineGap + 2;

        // Lưu ý
        g2d.setFont(new Font("SansSerif", Font.ITALIC, 11));
        g2d.setColor(new Color(252, 165, 165));
        g2d.drawString("⚠️ Lưu ý: Tia lửa bom quét trúng sẽ thiêu rụi Item trên đất!", col2X, itemY);

        // Nút QUAY LẠI
        Rectangle r = helpBackBounds;
        if (isHelpBackHovered) {
            g2d.setColor(new Color(220, 38, 38));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(252, 165, 165));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        } else {
            g2d.setColor(new Color(51, 65, 85));
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g2d.setColor(new Color(100, 116, 139));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
        }

        g2d.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics bfm = g2d.getFontMetrics();
        String backText = "QUAY LẠI [ESC]";
        g2d.setColor(Color.WHITE);
        g2d.drawString(backText, r.x + (r.width - bfm.stringWidth(backText)) / 2,
                r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent());
    }

    // ==========================================
    // 7. XỬ LÝ SỰ KIỆN CHUỘT & PHÍM
    // ==========================================

    // --- MAIN MENU ---
    public void handleMenuMouseMove(int mx, int my, GamePanel panel) {
        hoveredIndex = -1;
        if (menuBounds != null) {
            for (int i = 0; i < menuBounds.length; i++) {
                if (menuBounds[i].contains(mx, my)) {
                    hoveredIndex = i;
                    selectedIndex = i;
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleMenuMouseClick(int mx, int my, GamePanel panel) {
        if (menuBounds != null) {
            for (int i = 0; i < menuBounds.length; i++) {
                if (menuBounds[i].contains(mx, my)) {
                    executeMenuAction(i, panel);
                    return;
                }
            }
        }
    }

    public void handleMenuKeyPress(KeyEvent e, GamePanel panel) {
        String[] opts = getCurrentMenuOptions();
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            selectedIndex = (selectedIndex - 1 + opts.length) % opts.length;
        } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            selectedIndex = (selectedIndex + 1) % opts.length;
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            executeMenuAction(selectedIndex, panel);
        }
    }

    private void executeMenuAction(int index, GamePanel panel) {
        panel.setCursor(Cursor.getDefaultCursor());
        String[] opts = getCurrentMenuOptions();
        if (opts.length == 4) {
            switch (index) {
                case 0 -> panel.continueSavedGame(); // Tiếp tục ván chơi dở
                case 1 -> panel.startNewGame();      // Chơi mới
                case 2 -> panel.setState(GameState.HELP);
                case 3 -> panel.onExitGame();
            }
        } else {
            switch (index) {
                case 0 -> panel.startNewGame();
                case 1 -> panel.setState(GameState.HELP);
                case 2 -> panel.onExitGame();
            }
        }
    }

    // --- LEVEL SELECT (BẢN ĐỒ THẾ GIỚI) ---
    public void handleLevelSelectMouseMove(int mx, int my, GamePanel panel) {
        hoveredDifficultyIndex = -1;
        if (difficultyBounds != null) {
            for (int i = 0; i < difficultyBounds.length; i++) {
                if (difficultyBounds[i].contains(mx, my)) {
                    hoveredDifficultyIndex = i;
                    hoveredLevelNode = -1;
                    isLevelBackHovered = false;
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
            }
        }

        hoveredLevelNode = -1;
        if (levelNodeBounds != null) {
            for (int i = 0; i < 9; i++) {
                Rectangle r = levelNodeBounds[i];
                int cx = r.x + r.width / 2;
                int cy = r.y + r.height / 2;
                int distSq = (mx - cx) * (mx - cx) + (my - cy) * (my - cy);
                // Bán kính click thoải mái 28px
                if (distSq <= 28 * 28) {
                    hoveredLevelNode = i;
                    isLevelBackHovered = false;
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
            }
        }

        isLevelBackHovered = (levelBackBounds != null && levelBackBounds.contains(mx, my));
        if (isLevelBackHovered) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }

        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleLevelSelectMouseClick(int mx, int my, GamePanel panel) {
        // 1. Click chọn độ khó (DỄ / THƯỜNG / KHÓ)
        if (difficultyBounds != null) {
            for (int i = 0; i < difficultyBounds.length; i++) {
                if (difficultyBounds[i].contains(mx, my)) {
                    selectedDifficulty = Difficulty.values()[i];
                    panel.setDifficulty(selectedDifficulty);
                    return;
                }
            }
        }

        // 2. Click chọn 1 trong 9 cứ điểm màn chơi trên Bản đồ
        if (levelNodeBounds != null) {
            for (int i = 0; i < 9; i++) {
                Rectangle r = levelNodeBounds[i];
                int cx = r.x + r.width / 2;
                int cy = r.y + r.height / 2;
                int distSq = (mx - cx) * (mx - cx) + (my - cy) * (my - cy);
                if (distSq <= 28 * 28) {
                    panel.setCursor(Cursor.getDefaultCursor());
                    panel.startLevelWithDifficulty(i + 1, selectedDifficulty);
                    return;
                }
            }
        }

        // 3. Click nút Quay lại
        if (levelBackBounds != null && levelBackBounds.contains(mx, my)) {
            panel.setCursor(Cursor.getDefaultCursor());
            panel.setState(GameState.MENU);
        }
    }

    public void handleLevelSelectKeyPress(KeyEvent e, GamePanel panel) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            panel.setState(GameState.MENU);
        } else if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9) {
            int lvl = code - KeyEvent.VK_0;
            panel.startLevelWithDifficulty(lvl, selectedDifficulty);
        } else if (code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD9) {
            int lvl = code - KeyEvent.VK_NUMPAD0;
            panel.startLevelWithDifficulty(lvl, selectedDifficulty);
        } else if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_A) {
            int prev = (selectedDifficulty.ordinal() - 1 + Difficulty.values().length) % Difficulty.values().length;
            selectedDifficulty = Difficulty.values()[prev];
            panel.setDifficulty(selectedDifficulty);
        } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_D) {
            int next = (selectedDifficulty.ordinal() + 1) % Difficulty.values().length;
            selectedDifficulty = Difficulty.values()[next];
            panel.setDifficulty(selectedDifficulty);
        }
    }

    // --- VICTORY ---
    public void handleVictoryMouseMove(int mx, int my, GamePanel panel, boolean isFinal) {
        Rectangle[] currentBounds = isFinal ? finalVictoryBounds : victoryBounds;
        if (isFinal) hoveredFinalVictoryIndex = -1;
        else hoveredVictoryIndex = -1;

        if (currentBounds != null) {
            for (int i = 0; i < currentBounds.length; i++) {
                if (currentBounds[i].contains(mx, my)) {
                    if (isFinal) hoveredFinalVictoryIndex = i;
                    else hoveredVictoryIndex = i;
                    panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleVictoryMouseClick(int mx, int my, GamePanel panel, boolean isFinal) {
        Rectangle[] currentBounds = isFinal ? finalVictoryBounds : victoryBounds;
        if (currentBounds == null) return;

        for (int i = 0; i < currentBounds.length; i++) {
            if (currentBounds[i].contains(mx, my)) {
                panel.setCursor(Cursor.getDefaultCursor());
                if (isFinal) {
                    switch (i) {
                        case 0 -> panel.restartGame(); // Chơi lại từ đầu
                        case 1 -> panel.setState(GameState.LEVEL_SELECT);
                        case 2 -> panel.returnToMenu();
                    }
                } else {
                    switch (i) {
                        case 0 -> panel.nextLevel(); // Sang màn tiếp theo
                        case 1 -> panel.restartCurrentLevel(); // Chơi lại màn hiện tại
                        case 2 -> panel.setState(GameState.LEVEL_SELECT);
                        case 3 -> panel.returnToMenu();
                    }
                }
                return;
            }
        }
    }

    public void handleVictoryKeyPress(KeyEvent e, GamePanel panel, boolean isFinal) {
        int code = e.getKeyCode();
        if (isFinal) {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_R) {
                panel.restartGame();
            } else if (code == KeyEvent.VK_L) {
                panel.setState(GameState.LEVEL_SELECT);
            } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_M) {
                panel.returnToMenu();
            }
        } else {
            if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                panel.nextLevel();
            } else if (code == KeyEvent.VK_R) {
                panel.restartCurrentLevel();
            } else if (code == KeyEvent.VK_L) {
                panel.setState(GameState.LEVEL_SELECT);
            } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_M) {
                panel.returnToMenu();
            }
        }
    }

    // --- GAME OVER ---
    public void handleGameOverMouseMove(int mx, int my, GamePanel panel) {
        hoveredGameOverIndex = -1;
        for (int i = 0; i < gameOverBounds.length; i++) {
            if (gameOverBounds[i].contains(mx, my)) {
                hoveredGameOverIndex = i;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleGameOverMouseClick(int mx, int my, GamePanel panel) {
        for (int i = 0; i < gameOverBounds.length; i++) {
            if (gameOverBounds[i].contains(mx, my)) {
                panel.setCursor(Cursor.getDefaultCursor());
                switch (i) {
                    case 0 -> panel.restartCurrentLevel();
                    case 1 -> panel.setState(GameState.LEVEL_SELECT);
                    case 2 -> panel.returnToMenu();
                }
                return;
            }
        }
    }

    public void handleGameOverKeyPress(KeyEvent e, GamePanel panel) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_R) {
            panel.restartCurrentLevel();
        } else if (code == KeyEvent.VK_L) {
            panel.setState(GameState.LEVEL_SELECT);
        } else if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_M) {
            panel.returnToMenu();
        }
    }

    // --- HELP ---
    public void handleHelpMouseMove(int mx, int my, GamePanel panel) {
        isHelpBackHovered = helpBackBounds != null && helpBackBounds.contains(mx, my);
        panel.setCursor(isHelpBackHovered ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    public void handleHelpMouseClick(int mx, int my, GamePanel panel) {
        if (helpBackBounds != null && helpBackBounds.contains(mx, my)) {
            panel.setCursor(Cursor.getDefaultCursor());
            panel.setState(GameState.MENU);
        }
    }

    // --- PAUSE ---
    public void handlePauseMouseMove(int mx, int my, GamePanel panel) {
        hoveredPauseIndex = -1;
        for (int i = 0; i < pauseBounds.length; i++) {
            if (pauseBounds[i].contains(mx, my)) {
                hoveredPauseIndex = i;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handlePauseMouseClick(int mx, int my, GamePanel panel) {
        for (int i = 0; i < pauseBounds.length; i++) {
            if (pauseBounds[i].contains(mx, my)) {
                panel.setCursor(Cursor.getDefaultCursor());
                switch (i) {
                    case 0 -> panel.resumeGame();
                    case 1 -> panel.restartCurrentLevel();
                    case 2 -> panel.returnToMenu();
                }
                return;
            }
        }
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public BufferedImage getPlayButtonImage() {
        return playButtonImage;
    }

    public Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }

    public void setSelectedDifficulty(Difficulty selectedDifficulty) {
        this.selectedDifficulty = (selectedDifficulty != null) ? selectedDifficulty : Difficulty.NORMAL;
    }
}
