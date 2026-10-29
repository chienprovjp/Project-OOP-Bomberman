package bomberman.gui;

import bomberman.entities.Player;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Lớp GamePanel: Màn hình vẽ chính của game.
 * - Quản lý bản đồ (GameMap) và nhân vật (Player).
 * - Lắng nghe bàn phím người dùng.
 * - Chạy vòng lặp game (Game Loop) bằng Timer để cập nhật và vẽ liên tục ~60 lần mỗi giây.
 */
public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private Timer gameTimer;

    public GamePanel() {
        // 1. Tạo bản đồ game
        this.map = new GameMap();

        // 2. Tạo nhân vật ở vị trí ban đầu (ô hàng 1, cột 1 - vị trí trống an toàn)
        int startX = GameMap.TILE_SIZE + 3;
        int startY = GameMap.TILE_SIZE + 3;
        this.player = new Player(startX, startY);

        // 3. Cài đặt kích thước màn hình khớp với kích thước của bản đồ
        setPreferredSize(new Dimension(map.getWidthInPixels(), map.getHeightInPixels()));
        setBackground(Color.BLACK);
        setFocusable(true); // Cho phép panel nhận sự kiện bàn phím

        // 4. Lắng nghe phím bấm từ người chơi và gửi cho nhân vật xử lý
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.handleKeyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyReleased(e);
            }
        });

        // 5. Khởi tạo vòng lặp game (Game Loop):
        // Cứ sau mỗi 16 mili-giây (tương đương ~60 khung hình/giây - FPS):
        // - Cập nhật vị trí nhân vật (player.move)
        // - Yêu cầu máy vẽ lại toàn bộ màn hình (repaint)
        gameTimer = new Timer(16, e -> {
            player.move(map);
            repaint();
        });
        gameTimer.start(); // Bắt đầu chạy vòng lặp game
    }

    /**
     * Hàm vẽ toàn bộ nội dung game lên màn hình.
     * Thứ tự vẽ rất quan trọng: Cái nào vẽ trước sẽ ở dưới, cái nào vẽ sau sẽ đè lên trên.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Bước 1: Vẽ bản đồ làm nền phía dưới
        map.draw(g);

        // Bước 2: Vẽ nhân vật đè lên trên nền bản đồ
        player.draw(g);

        // Bước 3: Vẽ dòng chữ hướng dẫn điều khiển ở góc trên màn hình
        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("Dieu khien: Phim WASD hoac Phim mui ten", 12, 22);
    }

    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
}

