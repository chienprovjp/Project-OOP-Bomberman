package bomberman.ui;

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

    // Trạng thái nút cho màn hình Chọn Màn (Level Select)
    private final String[] levelOptions = { "MÀN 1", "MÀN 2", "MÀN 3", "QUAY LẠI" };
    private Rectangle[] levelBounds;
    private int hoveredLevelIndex = -1;

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

        // --- 4. Level Select ---
        int lvlBtnW = Math.max(200, (int) (width * 0.24));
        int lvlBtnH = 40;
        int lvlStartX = (width - lvlBtnW) / 2;
        int lvlSpacing = 52;
        int lvlStartY = (height - (levelOptions.length * lvlSpacing)) / 2 + 35;
        if (levelBounds == null || levelBounds.length != levelOptions.length) {
            levelBounds = new Rectangle[levelOptions.length];
        }
        for (int i = 0; i < levelOptions.length; i++) {
            levelBounds[i] = new Rectangle(lvlStartX, lvlStartY + i * lvlSpacing, lvlBtnW, lvlBtnH);
        }

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
    // 2. VẼ MÀN HÌNH CHỌN MÀN (LEVEL SELECT)
    // ==========================================
    public void drawLevelSelect(Graphics g, int width, int height) {
        updateBounds(width, height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền: ưu tiên ảnh backgroundlevel.png của Quốc
        if (backgroundLevelImage != null) {
            g2d.drawImage(backgroundLevelImage, 0, 0, width, height, null);
            g2d.setColor(new Color(15, 23, 42, 140)); // Phủ mờ nhẹ
            g2d.fillRect(0, 0, width, height);
        } else {
            g2d.setColor(new Color(15, 23, 42));
            g2d.fillRect(0, 0, width, height);
        }

        // Tiêu đề
        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "CHỌN MÀN CHƠI";
        int titleX = (width - tfm.stringWidth(title)) / 2;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(title, titleX + 2, 72);
        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString(title, titleX, 70);

        Font btnFont = new Font("SansSerif", Font.BOLD, 14);
        g2d.setFont(btnFont);
        FontMetrics bfm = g2d.getFontMetrics();

        for (int i = 0; i < levelOptions.length; i++) {
            Rectangle r = levelBounds[i];
            boolean isHovered = (i == hoveredLevelIndex);

            if (i == levelOptions.length - 1) { // Nút Quay Lại
                if (isHovered) {
                    g2d.setColor(new Color(220, 38, 38));
                    g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                    g2d.setColor(new Color(252, 165, 165));
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                } else {
                    g2d.setColor(new Color(51, 65, 85, 220));
                    g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                    g2d.setColor(new Color(148, 163, 184));
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                }
            } else {
                if (isHovered) {
                    g2d.setColor(new Color(14, 165, 233));
                    g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                    g2d.setColor(new Color(224, 242, 254));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                } else {
                    g2d.setColor(new Color(30, 41, 59, 220));
                    g2d.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                    g2d.setColor(new Color(56, 189, 248));
                    g2d.setStroke(new BasicStroke(1.2f));
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                }
            }

            String text = levelOptions[i];
            g2d.setColor(Color.WHITE);
            int tx = r.x + (r.width - bfm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent();
            g2d.drawString(text, tx, ty);
        }
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

        int panelW = Math.min(680, width - 40);
        int panelH = Math.min(480, height - 60);
        int px = (width - panelW) / 2;
        int py = 20;

        g2d.setColor(new Color(30, 41, 59, 230));
        g2d.fillRoundRect(px, py, panelW, panelH, 16, 16);
        g2d.setColor(new Color(71, 85, 105));
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(px, py, panelW, panelH, 16, 16);

        // Header
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "HƯỚNG DẪN ĐIỀU KHIỂN & LUẬT CHƠI";
        g2d.setColor(new Color(250, 204, 21));
        g2d.drawString(title, px + (panelW - tfm.stringWidth(title)) / 2, py + 38);

        // Nội dung hướng dẫn
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2d.setColor(new Color(241, 245, 249));

        int textY = py + 80;
        int lineGap = 28;

        g2d.drawString("• DI CHUYỂN: Dùng phím W / A / S / D hoặc 4 phím mũi tên.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• ĐẶT BOM: Nhấn phím SPACE để đặt bom tại ô đang đứng.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• TẠM DỪNG: Nhấn ESC để tạm dừng game hoặc mở menu lựa chọn.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• LƯU TIẾN TRÌNH: Khi bấm ESC -> M hoặc tắt game, tiến trình được lưu tự động.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• TIẾP TỤC: Khi vào lại game, bấm 'TIẾP TỤC' để chơi tiếp màn và điểm đang dở.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• LUẬT CHƠI: Đặt bom phá gạch, tiêu diệt toàn bộ quái Ballom để qua màn.", px + 30, textY);
        textY += lineGap;
        g2d.drawString("• CẨN THẬN: Tránh xa tia lửa bom và không để quái chạm vào người!", px + 30, textY);

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

    // --- LEVEL SELECT ---
    public void handleLevelSelectMouseMove(int mx, int my, GamePanel panel) {
        hoveredLevelIndex = -1;
        for (int i = 0; i < levelBounds.length; i++) {
            if (levelBounds[i].contains(mx, my)) {
                hoveredLevelIndex = i;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleLevelSelectMouseClick(int mx, int my, GamePanel panel) {
        for (int i = 0; i < levelBounds.length; i++) {
            if (levelBounds[i].contains(mx, my)) {
                panel.setCursor(Cursor.getDefaultCursor());
                if (i == levelOptions.length - 1) {
                    panel.setState(GameState.MENU);
                } else {
                    panel.startLevelFromBeginning(i + 1);
                }
                return;
            }
        }
    }

    public void handleLevelSelectKeyPress(KeyEvent e, GamePanel panel) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_ESCAPE) {
            panel.setState(GameState.MENU);
        } else if (code == KeyEvent.VK_1 || code == KeyEvent.VK_NUMPAD1) {
            panel.startLevelFromBeginning(1);
        } else if (code == KeyEvent.VK_2 || code == KeyEvent.VK_NUMPAD2) {
            panel.startLevelFromBeginning(2);
        } else if (code == KeyEvent.VK_3 || code == KeyEvent.VK_NUMPAD3) {
            panel.startLevelFromBeginning(3);
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
}
