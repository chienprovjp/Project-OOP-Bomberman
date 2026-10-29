package bomberman.gui;

import javax.imageio.ImageIO;
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
import java.io.File;

public class Menu {
    private int selectedIndex = 0;
    private int hoveredIndex = -1;
    private final String[] menuOptions = { "CHƠI NGAY", "HƯỚNG DẪN", "THOÁT GAME" };
    private Rectangle[] menuBounds;

    // Trạng thái nút cho màn hình Hướng dẫn
    private Rectangle helpBackBounds;
    private boolean isHelpBackHovered = false;

    // Trạng thái nút cho màn hình Tạm dừng
    private final String[] pauseOptions = { "TIẾP TỤC", "CHƠI LẠI", "MENU CHÍNH" };
    private Rectangle[] pauseBounds;
    private int hoveredPauseIndex = -1;

    private int animTick = 0;
    private BufferedImage playerIcon;

    public Menu() {
        initBounds();
        loadIcon();
    }

    private void loadIcon() {
        try {
            File f = new File("assets/images/player_down.png");
            if (!f.exists()) f = new File("assets/images/player.png");
            if (f.exists()) {
                playerIcon = ImageIO.read(f);
            }
        } catch (Exception ignored) {
        }
    }

    private void initBounds() {
        int btnW = 220;
        int btnH = 40;
        int startX = (672 - btnW) / 2;
        int startY = 165;
        int spacing = 52;

        menuBounds = new Rectangle[menuOptions.length];
        for (int i = 0; i < menuOptions.length; i++) {
            menuBounds[i] = new Rectangle(startX, startY + i * spacing, btnW, btnH);
        }

        // Nút quay lại của Help
        helpBackBounds = new Rectangle((672 - 180) / 2, 335, 180, 36);

        // Nút trong Pause Menu
        int pauseBtnW = 180;
        int pauseBtnH = 36;
        int pauseStartX = (672 - pauseBtnW) / 2;
        int pauseStartY = 165;
        int pauseSpacing = 48;
        pauseBounds = new Rectangle[pauseOptions.length];
        for (int i = 0; i < pauseOptions.length; i++) {
            pauseBounds[i] = new Rectangle(pauseStartX, pauseStartY + i * pauseSpacing, pauseBtnW, pauseBtnH);
        }
    }

    public void tick() {
        animTick++;
    }

    // ==========================================
    // 1. VẼ MÀN HÌNH CHÍNH (MAIN MENU)
    // ==========================================
    public void drawMenu(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lớp phủ nền mờ phong cách Dark Arcade
        g2d.setColor(new Color(15, 23, 42, 220));
        g2d.fillRect(0, 0, width, height);

        // Hiệu ứng nhịp nhẹ cho Tiêu đề
        int floatOffset = (int) (Math.sin(animTick * 0.08) * 3);

        // Tiêu đề BOMBERMAN
        int titleY = 75 + floatOffset;
        String title = "BOMBERMAN";
        Font titleFont = new Font("SansSerif", Font.BOLD, 42);
        g2d.setFont(titleFont);
        FontMetrics tfm = g2d.getFontMetrics();
        int titleX = (width - tfm.stringWidth(title)) / 2;

        // Bóng đổ tiêu đề (3D drop shadow)
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.drawString(title, titleX + 3, titleY + 3);

        // Viền đỏ cam và chữ vàng nổi bật
        g2d.setColor(new Color(255, 107, 0));
        g2d.drawString(title, titleX - 1, titleY - 1);
        g2d.drawString(title, titleX + 1, titleY + 1);
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(title, titleX, titleY);

        // Huy hiệu phụ đề (Badge)
        String sub = "★ OOP PROJECT - UET VNU ★";
        Font subFont = new Font("SansSerif", Font.BOLD, 12);
        g2d.setFont(subFont);
        FontMetrics sfm = g2d.getFontMetrics();
        int subX = (width - sfm.stringWidth(sub)) / 2;
        int subY = titleY + 28;

        g2d.setColor(new Color(30, 41, 59, 180));
        g2d.fillRoundRect(subX - 12, subY - 14, sfm.stringWidth(sub) + 24, 20, 10, 10);
        g2d.setColor(new Color(56, 189, 248));
        g2d.drawRoundRect(subX - 12, subY - 14, sfm.stringWidth(sub) + 24, 20, 10, 10);
        g2d.setColor(new Color(224, 242, 254));
        g2d.drawString(sub, subX, subY);

        // Vẽ icon player trang trí hai bên tiêu đề nếu có
        if (playerIcon != null) {
            int iconSize = 36;
            g2d.drawImage(playerIcon, titleX - iconSize - 18, titleY - 32, iconSize, iconSize, null);
            g2d.drawImage(playerIcon, titleX + tfm.stringWidth(title) + 18, titleY - 32, iconSize, iconSize, null);
        }

        // Vẽ các nút Menu
        Font btnFont = new Font("SansSerif", Font.BOLD, 15);
        g2d.setFont(btnFont);
        FontMetrics bfm = g2d.getFontMetrics();

        for (int i = 0; i < menuOptions.length; i++) {
            Rectangle r = menuBounds[i];
            boolean isCurrent = (i == selectedIndex);

            // Nền nút
            if (isCurrent) {
                g2d.setColor(new Color(37, 99, 235, 230)); // Xanh dương sáng khi hover / chọn
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
                g2d.setColor(new Color(96, 165, 250));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);

                // Mũi tên chỉ thị bên trái
                g2d.setColor(new Color(254, 240, 138));
                g2d.drawString("▶", r.x + 14, r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent());
            } else {
                g2d.setColor(new Color(30, 41, 59, 180)); // Xám mờ tinh tế
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);
                g2d.setColor(new Color(71, 85, 105));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 12, 12);
            }

            // Chữ trong nút
            String text = menuOptions[i];
            int tx = r.x + (r.width - bfm.stringWidth(text)) / 2;
            int ty = r.y + ((r.height - bfm.getHeight()) / 2) + bfm.getAscent();

            g2d.setColor(isCurrent ? Color.WHITE : new Color(203, 213, 225));
            g2d.drawString(text, tx, ty);
        }

        // Footer hướng dẫn
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2d.setColor(new Color(148, 163, 184));
        String footer = "[ ↑ / ↓ ]: Chọn  |  [ ENTER / SPACE ]: Xác nhận  |  Hỗ trợ nhấp Chuột";
        FontMetrics ffm = g2d.getFontMetrics();
        g2d.drawString(footer, (width - ffm.stringWidth(footer)) / 2, height - 20);
    }

    // ==========================================
    // 2. VẼ MÀN HÌNH HƯỚNG DẪN (HELP SCREEN)
    // ==========================================
    public void drawHelp(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Nền tối mờ
        g2d.setColor(new Color(15, 23, 42, 235));
        g2d.fillRect(0, 0, width, height);

        // Khung card trung tâm
        int cardW = 540;
        int cardH = 340;
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2 - 10;

        g2d.setColor(new Color(30, 41, 59, 240));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 16, 16);
        g2d.setColor(new Color(56, 189, 248));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 16, 16);

        // Tiêu đề card
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics tfm = g2d.getFontMetrics();
        String title = "HƯỚNG DẪN ĐIỀU KHIỂN & LUẬT CHƠI";
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(title, cardX + (cardW - tfm.stringWidth(title)) / 2, cardY + 38);

        // Bảng danh sách phím
        String[][] instructions = {
            { "W, A, S, D  /  Phím Mũi Tên", "Di chuyển nhân vật (hỗ trợ trượt bo góc tường)" },
            { "Phím R", "Đặt lại vị trí nhân vật về điểm xuất phát" },
            { "Phím ESC", "Tạm dừng trò chơi hoặc quay lại Menu" },
            { "Mục tiêu", "Khám phá bản đồ, vượt chướng ngại vật & tránh quái vật" }
        };

        int rowY = cardY + 75;
        for (String[] row : instructions) {
            // Hộp phím bên trái
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2d.setColor(new Color(51, 65, 85));
            g2d.fillRoundRect(cardX + 24, rowY - 14, 180, 26, 6, 6);
            g2d.setColor(new Color(148, 163, 184));
            g2d.drawRoundRect(cardX + 24, rowY - 14, 180, 26, 6, 6);
            g2d.setColor(new Color(254, 240, 138));
            g2d.drawString(row[0], cardX + 32, rowY + 3);

            // Nội dung giải thích bên phải
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2d.setColor(new Color(241, 245, 249));
            g2d.drawString(row[1], cardX + 216, rowY + 3);

            rowY += 40;
        }

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
    // 3. VẼ MÀN HÌNH TẠM DỪNG (PAUSE OVERLAY)
    // ==========================================
    public void drawPause(Graphics g, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lớp tối phủ mờ sàn game đang chơi
        g2d.setColor(new Color(0, 0, 0, 190));
        g2d.fillRect(0, 0, width, height);

        // Khung card tạm dừng
        int cardW = 300;
        int cardH = 220;
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2;

        g2d.setColor(new Color(30, 41, 59, 240));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 14, 14);
        g2d.setColor(new Color(245, 158, 11));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);

        // Tiêu đề TẠM DỪNG
        g2d.setFont(new Font("SansSerif", Font.BOLD, 22));
        FontMetrics tfm = g2d.getFontMetrics();
        String pauseTitle = "TẠM DỪNG";
        g2d.setColor(new Color(255, 215, 0));
        g2d.drawString(pauseTitle, cardX + (cardW - tfm.stringWidth(pauseTitle)) / 2, cardY + 36);

        // Các nút trong bảng Tạm dừng
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
    // 4. XỬ LÝ SỰ KIỆN CHUỘT & PHÍM
    // ==========================================
    public void handleMenuMouseMove(int mx, int my, GamePanel panel) {
        hoveredIndex = -1;
        for (int i = 0; i < menuBounds.length; i++) {
            if (menuBounds[i].contains(mx, my)) {
                hoveredIndex = i;
                selectedIndex = i;
                panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        panel.setCursor(Cursor.getDefaultCursor());
    }

    public void handleMenuMouseClick(int mx, int my, GamePanel panel) {
        for (int i = 0; i < menuBounds.length; i++) {
            if (menuBounds[i].contains(mx, my)) {
                executeMenuAction(i, panel);
                return;
            }
        }
    }

    public void handleMenuKeyPress(KeyEvent e, GamePanel panel) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_UP || code == KeyEvent.VK_W) {
            selectedIndex = (selectedIndex - 1 + menuOptions.length) % menuOptions.length;
        } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_S) {
            selectedIndex = (selectedIndex + 1) % menuOptions.length;
        } else if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
            executeMenuAction(selectedIndex, panel);
        }
    }

    private void executeMenuAction(int index, GamePanel panel) {
        panel.setCursor(Cursor.getDefaultCursor());
        switch (index) {
            case 0 -> panel.startGame();
            case 1 -> panel.setState(GameState.HELP);
            case 2 -> System.exit(0);
        }
    }

    public void handleHelpMouseMove(int mx, int my, GamePanel panel) {
        isHelpBackHovered = helpBackBounds.contains(mx, my);
        panel.setCursor(isHelpBackHovered ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    public void handleHelpMouseClick(int mx, int my, GamePanel panel) {
        if (helpBackBounds.contains(mx, my)) {
            panel.setCursor(Cursor.getDefaultCursor());
            panel.setState(GameState.MENU);
        }
    }

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
                    case 1 -> panel.restartGame();
                    case 2 -> panel.returnToMenu();
                }
                return;
            }
        }
    }
}
