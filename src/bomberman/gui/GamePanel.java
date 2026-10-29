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

public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private Timer gameTimer;

    public GamePanel() {
        this.map = new GameMap();
        // Vị trí xuất phát của Player tại ô cỏ (1, 1)
        this.player = new Player(GameMap.TILE_SIZE + 4, GameMap.TILE_SIZE + 4);

        setPreferredSize(new Dimension(map.getWidthInPixels(), map.getHeightInPixels()));
        setBackground(Color.BLACK);
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    player.reset(GameMap.TILE_SIZE + 4, GameMap.TILE_SIZE + 4);
                } else {
                    player.handleKeyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                player.handleKeyReleased(e);
            }
        });

        // Vòng lặp game (~60 FPS)
        gameTimer = new Timer(16, e -> {
            player.move(map);
            repaint();
        });
        gameTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Vẽ bản đồ (cỏ, tường, gạch)
        map.draw(g);

        // 2. Vẽ nhân vật Player
        player.draw(g);

        // 3. Thông tin hướng dẫn
        g.setColor(new Color(255, 255, 255, 200));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("WASD / Phím mũi tên: Di chuyển  |  R: Đặt lại", 12, 22);
    }

    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
}
