package bomberman.gui;

import bomberman.entities.Ballom;
import bomberman.entities.Bomb;
import bomberman.entities.Enemy;
import bomberman.entities.Explosion;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bomberman.data.SaveManager;
import bomberman.data.GameSaveData;

public class GamePanel extends JPanel {
    private GameMap map;
    private Player player;
    private Menu menu;
    private GameState state = GameState.MENU;
    private Timer gameTimer;
    private int level = 1;
    private int score = 0;
    private int lives = 3;
    private int highScore = 0;

    // Danh sách bom, vụ nổ và kẻ địch
    private List<Bomb> bombs = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();

    private int maxBombs = 1; // Số lượng bom tối đa đặt cùng lúc
    private int bombRadius = 1; // Bán kính nổ (1 ô)
    private int invulnerableTimer = 0; // Bộ đếm thời gian bất tử sau khi hồi sinh

    public GamePanel() {
        this.map = new GameMap();
        this.player = new Player(0, 0);
        this.player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.menu = new Menu();

        // Đọc tiến trình và kỷ lục cũ từ ổ cứng lên
        GameSaveData savedData = SaveManager.loadProgress();
        this.level = savedData.getLevel();
        this.score = savedData.getScore();
        this.lives = savedData.getLives();
        this.highScore = savedData.getHighScore();

        setPreferredSize(new Dimension(map.getWidthInPixels(), map.getHeightInPixels()));
        setBackground(Color.BLACK);
        setFocusable(true);

        setupEventHandlers();
        spawnEnemies(4);

        // Vòng lặp game (~60 FPS)
        gameTimer = new Timer(16, e -> {
            if (state == GameState.PLAYING) {
                updateGameLogic();
            }
            menu.tick();
            repaint();
        });
        gameTimer.start();
    }

    private void updateGameLogic() {
        player.move(map, bombs);

        if (invulnerableTimer > 0) {
            invulnerableTimer--;
        }

        // 1. Cập nhật danh sách Bom
        Iterator<Bomb> bombIt = bombs.iterator();
        while (bombIt.hasNext()) {
            Bomb b = bombIt.next();
            b.update();
            b.checkPlayerExit(player);

            if (b.isExploded()) {
                explosions.add(new Explosion(b.getTileCol(), b.getTileRow(), b.getRadius(), map));
                bombIt.remove();
            }
        }

        // 2. Cập nhật danh sách Vụ nổ (Explosions)
        Iterator<Explosion> expIt = explosions.iterator();
        while (expIt.hasNext()) {
            Explosion exp = expIt.next();
            exp.update();

            // Kiểm tra lửa thiêu trúng Player
            if (invulnerableTimer <= 0 && exp.hitsEntity(player)) {
                handlePlayerDamage();
            }

            // Kiểm tra lửa thiêu trúng Kẻ địch
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && exp.hitsEntity(enemy)) {
                    enemy.kill();
                    score += enemy.getScoreReward();
                    if (score > highScore) {
                        highScore = score;
                    }
                }
            }

            if (exp.isFinished()) {
                expIt.remove();
            }
        }

        // 3. Cập nhật danh sách Kẻ địch (Enemies)
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update();
            if (enemy.isAlive()) {
                enemy.move(map, bombs);
                // Kẻ địch chạm vào Player
                if (invulnerableTimer <= 0 && enemy.intersects(player)) {
                    handlePlayerDamage();
                }
            } else {
                enemyIt.remove();
            }
        }
    }

    /**
     * Đặt bom tại vị trí ô gạch mà Player đang đứng.
     */
    private void placeBomb() {
        if (bombs.size() >= maxBombs) return;

        int pCol = (player.getX() + player.getWidth() / 2) / GameMap.TILE_SIZE;
        int pRow = (player.getY() + player.getHeight() / 2) / GameMap.TILE_SIZE;

        // Không đặt 2 quả bom cùng một ô
        for (Bomb b : bombs) {
            if (b.getTileCol() == pCol && b.getTileRow() == pRow) {
                return;
            }
        }

        bombs.add(new Bomb(pCol, pRow, bombRadius));
    }

    /**
     * Xử lý khi Player trúng lửa hoặc chạm phải kẻ địch.
     */
    private void handlePlayerDamage() {
        lives--;
        invulnerableTimer = 90; // ~1.5 giây bất tử nhấp nháy

        if (lives <= 0) {
            // Hết mạng -> Lưu kỷ lục và quay về Menu
            SaveManager.saveProgress(1, 0, 3, highScore);
            lives = 3;
            score = 0;
            level = 1;
            bombs.clear();
            explosions.clear();
            spawnEnemies(4);
            player.reset(getPlayerSpawnX(), getPlayerSpawnY());
            setState(GameState.MENU);
        } else {
            player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        }
    }

    /**
     * Sinh ngẫu nhiên số lượng kẻ địch tại các vị trí cỏ an toàn cách xa Player.
     */
    private void spawnEnemies(int count) {
        enemies.clear();
        int spawned = 0;
        for (int r = GameMap.ROWS - 2; r >= 1 && spawned < count; r--) {
            for (int c = GameMap.COLS - 2; c >= 1 && spawned < count; c--) {
                if (map.getTile(r, c) == GameMap.GRASS) {
                    int distFromSpawn = Math.abs(r - 1) + Math.abs(c - 1);
                    if (distFromSpawn >= 6) {
                        int spawnX = c * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - (int) (GameMap.TILE_SIZE * 0.70)) / 2;
                        int spawnY = r * GameMap.TILE_SIZE + (GameMap.TILE_SIZE - (int) (GameMap.TILE_SIZE * 0.70)) / 2;
                        enemies.add(new Ballom(spawnX, spawnY));
                        spawned++;
                    }
                }
            }
        }
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
                            restartGame();
                        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                            placeBomb();
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
                if (menu.getBackgroundImage() == null) {
                    map.draw(g);
                }
                menu.drawMenu(g, width, height);
            }
            case PLAYING -> {
                // 1. Vẽ bản đồ (cỏ, tường đá, gạch)
                map.draw(g);

                // 2. Vẽ Bombs
                for (Bomb b : bombs) {
                    b.draw(g);
                }

                // 3. Vẽ Vụ nổ
                for (Explosion exp : explosions) {
                    exp.draw(g);
                }

                // 4. Vẽ Kẻ địch
                for (Enemy enemy : enemies) {
                    enemy.draw(g);
                }

                // 5. Vẽ Player (nhấp nháy khi bất tử)
                if (invulnerableTimer == 0 || (invulnerableTimer / 5) % 2 == 0) {
                    player.draw(g);
                }

                // 6. HUD hiển thị thông tin
                drawHUD(g);
            }
            case PAUSED -> {
                map.draw(g);
                for (Bomb b : bombs) b.draw(g);
                for (Enemy enemy : enemies) enemy.draw(g);
                player.draw(g);
                menu.drawPause(g, width, height);
            }
            case HELP -> {
                menu.drawHelp(g, width, height);
            }
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(new Color(15, 23, 42, 210));
        g.fillRoundRect(10, 8, 560, 28, 8, 8);
        g.setColor(new Color(56, 189, 248));
        g.drawRoundRect(10, 8, 560, 28, 8, 8);

        g.setColor(new Color(241, 245, 249));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("MÀN: " + level + "  |  ĐIỂM: " + score + "  |  KỶ LỤC: " + highScore + "  |  MẠNG: " + lives + "  |  SPACE: ĐẶT BOM", 20, 27);
    }

    // Các phương thức chuyển trạng thái Game
    public void startGame() {
        bombs.clear();
        explosions.clear();
        spawnEnemies(4);
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void resumeGame() {
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void restartGame() {
        bombs.clear();
        explosions.clear();
        spawnEnemies(4);
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void returnToMenu() {
        player.stopMoving();
        bombs.clear();
        explosions.clear();
        SaveManager.saveProgress(level, score, lives, highScore);
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
    public List<Bomb> getBombs() { return bombs; }
    public List<Enemy> getEnemies() { return enemies; }
}
