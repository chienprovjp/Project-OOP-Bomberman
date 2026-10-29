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
        this.player = new Player(GameMap.TILE_SIZE + 3, GameMap.TILE_SIZE + 3);

        setPreferredSize(new Dimension(map.getWidthInPixels(), map.getHeightInPixels()));
        setBackground(Color.BLACK);
        setFocusable(true);

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

        map.draw(g);
        player.draw(g);

        g.setColor(new Color(40, 40, 40));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("WASD / Phim mũi tên: Di chuyển", 12, 22);
    }

    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
}

