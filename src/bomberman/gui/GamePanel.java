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
 * Màn hình game chính: chứa GameMap và Player.
 */
public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private Timer gameTimer;

    public GamePanel() {
        this.map = new GameMap();
        // Khởi tạo Player ở vị trí an toàn (ô x=1, y=1)
        this.player = new Player(GameMap.TILE_SIZE + 3, GameMap.TILE_SIZE + 3);

        setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Bắt sự kiện bàn phím chuyển đến Player
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                player.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.keyReleased(e);
            }
        });

        // Game loop: ~60 FPS (16ms một lần cập nhật)
        gameTimer = new Timer(16, e -> {
            player.update(map);
            repaint();
        });
        gameTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Vẽ map
        map.render(g);

        // 2. Vẽ nhân vật
        player.render(g);

        // 3. Hướng dẫn sơ bộ góc trên
        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("WASD / Phím mũi tên: Di chuyển", 12, 22);
    }

    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }
}
