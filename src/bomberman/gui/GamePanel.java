package bomberman.gui;

import bomberman.entities.Player;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private Menu menu;
    private GameState state = GameState.MENU;
    private Timer gameTimer;

    public GamePanel() {
        this.map = new GameMap();
        this.player = new Player(0, 0);
        this.player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.menu = new Menu();

        setPreferredSize(new Dimension(map.getWidthInPixels(), map.getHeightInPixels()));
        setBackground(Color.BLACK);
        setFocusable(true);

        setupEventHandlers();

        // Vòng lặp game (~60 FPS)
        gameTimer = new Timer(16, e -> {
            if (state == GameState.PLAYING) {
                player.move(map);
            }
            menu.tick();
            repaint();
        });
        gameTimer.start();
    }

    private int getPlayerSpawnX() {
        return GameMap.TILE_SIZE + (GameMap.TILE_SIZE - player.getWidth()) / 2;
    }

    private int getPlayerSpawnY() {
        return GameMap.TILE_SIZE + (GameMap.TILE_SIZE - player.getHeight()) / 2;
    }

    private void setupEventHandlers() {
        // Xử lý bàn phím
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (state) {
                    case MENU -> menu.handleMenuKeyPress(e, GamePanel.this);
                    case HELP -> {
                        int code = e.getKeyCode();
                        if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_SPACE) {
                            setState(GameState.MENU);
                        }
                    }
                    case PLAYING -> {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                            player.stopMoving();
                            setState(GameState.PAUSED);
                        } else if (e.getKeyCode() == KeyEvent.VK_R) {
                            player.reset(getPlayerSpawnX(), getPlayerSpawnY());
                        } else {
                            player.handleKeyPressed(e);
                        }
                    }
                    case PAUSED -> {
                        int code = e.getKeyCode();
                        if (code == KeyEvent.VK_ESCAPE) {
                            resumeGame();
                        } else if (code == KeyEvent.VK_R) {
                            restartGame();
                        } else if (code == KeyEvent.VK_M) {
                            returnToMenu();
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (state == GameState.PLAYING) {
                    player.handleKeyReleased(e);
                }
            }
        });

        // Xử lý chuột (Click và Rê chuột hover)
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                menu.updateBounds(getWidth(), getHeight());
                switch (state) {
                    case MENU -> menu.handleMenuMouseMove(mx, my, GamePanel.this);
                    case HELP -> menu.handleHelpMouseMove(mx, my, GamePanel.this);
                    case PAUSED -> menu.handlePauseMouseMove(mx, my, GamePanel.this);
                    default -> setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX();
                int my = e.getY();
                menu.updateBounds(getWidth(), getHeight());
                switch (state) {
                    case MENU -> menu.handleMenuMouseClick(mx, my, GamePanel.this);
                    case HELP -> menu.handleHelpMouseClick(mx, my, GamePanel.this);
                    case PAUSED -> menu.handlePauseMouseClick(mx, my, GamePanel.this);
                    default -> {}
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        switch (state) {
            case MENU -> {
                // Ưu tiên dùng Background.png của Quốc do menu vẽ; chỉ vẽ map nếu không có ảnh nền
                if (menu.getBackgroundImage() == null) {
                    map.draw(g);
                }
                menu.drawMenu(g, width, height);
            }
            case PLAYING -> {
                // 1. Vẽ bản đồ (cỏ, tường đá, tường gạch)
                map.draw(g);
                // 2. Vẽ nhân vật Player
                player.draw(g);
                // 3. HUD điều khiển
                drawHUD(g);
            }
            case PAUSED -> {
                map.draw(g);
                player.draw(g);
                menu.drawPause(g, width, height);
            }
            case HELP -> {
                menu.drawHelp(g, width, height);
            }
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(new Color(15, 23, 42, 200));
        g.fillRoundRect(10, 8, 420, 28, 8, 8);
        g.setColor(new Color(56, 189, 248));
        g.drawRoundRect(10, 8, 420, 28, 8, 8);

        g.setColor(new Color(241, 245, 249));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("WASD / Mũi tên: Di chuyển  |  R: Đặt lại  |  ESC: Tạm dừng", 20, 27);
    }

    // Các phương thức chuyển trạng thái Game
    public void startGame() {
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void resumeGame() {
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void restartGame() {
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void returnToMenu() {
        player.stopMoving();
        this.state = GameState.MENU;
        setCursor(Cursor.getDefaultCursor());
    }

    public void setState(GameState state) {
        this.state = state;
        setCursor(Cursor.getDefaultCursor());
    }

    public GameState getState() { return state; }
    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
    public Menu getMenu() { return menu; }
}
