package bomberman.gui;

import bomberman.entities.Ballom;
import bomberman.entities.Bomb;
import bomberman.entities.Enemy;
import bomberman.entities.Explosion;
import bomberman.entities.Player;
import bomberman.data.SaveManager;
import bomberman.data.GameSaveData;
import bomberman.data.MapData;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        setPreferredSize(new Dimension(GameMap.DEFAULT_COLS * GameMap.TILE_SIZE, GameMap.DEFAULT_ROWS * GameMap.TILE_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        setupEventHandlers();
        spawnEnemies(3);

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
        if (state != GameState.PLAYING) return;

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
                if (state != GameState.PLAYING) return;
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
                    if (state != GameState.PLAYING) return;
                }
            } else {
                enemyIt.remove();
            }
        }

        // 4. Kiểm tra điều kiện thắng màn: Đã tiêu diệt hết quái vật!
        if (enemies.isEmpty()) {
            triggerVictory();
        }
    }

    /**
     * Đặt bom tại vị trí ô gạch mà Player đang đứng.
     */
    private void placeBomb() {
        if (bombs.size() >= maxBombs)
            return;

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
            triggerGameOver();
        } else {
            player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        }
    }

    /**
     * Kích hoạt trạng thái Game Over và lưu điểm kỷ lục.
     */
    public void triggerGameOver() {
        player.stopMoving();
        SaveManager.saveProgress(1, 0, 3, highScore);
        this.state = GameState.GAME_OVER;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Kích hoạt trạng thái Chiến Thắng (Victory).
     */
    public void triggerVictory() {
        player.stopMoving();
        bombs.clear();
        explosions.clear();
        if (score > highScore) {
            highScore = score;
        }
        SaveManager.saveProgress(level, score, lives, highScore);
        this.state = GameState.VICTORY;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Bắt đầu màn chơi được chọn (Level 1, 2, 3...)
     */
    public void startLevel(int chosenLevel) {
        this.level = chosenLevel;
        map.loadLevel(chosenLevel);
        bombs.clear();
        explosions.clear();
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        spawnEnemies(2 + chosenLevel);
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Chuyển sang màn tiếp theo khi chiến thắng
     */
    public void nextLevel() {
        if (level < MapData.LEVEL_COUNT) {
            startLevel(level + 1);
        } else {
            restartGame();
        }
    }

    /**
     * Chơi lại màn hiện tại
     */
    public void restartCurrentLevel() {
        startLevel(this.level);
    }

    /**
     * Chơi lại game từ Màn 1 (reset mạng và điểm)
     */
    public void restartGame() {
        this.lives = 3;
        this.score = 0;
        startLevel(1);
    }

    /**
     * Sinh ngẫu nhiên kẻ địch tại các vị trí cỏ an toàn cách xa điểm xuất phát.
     */
    private void spawnEnemies(int count) {
        enemies.clear();
        int spawned = 0;
        int mapRows = map.getRows();
        int mapCols = map.getCols();

        for (int r = mapRows - 2; r >= 1 && spawned < count; r--) {
            for (int c = mapCols - 2; c >= 1 && spawned < count; c--) {
                if (map.getTile(r, c) == GameMap.GRASS) {
                    int distFromSpawn = Math.abs(r - 1) + Math.abs(c - 1);
                    if (distFromSpawn >= 4) {
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
                    case LEVEL_SELECT -> menu.handleLevelSelectKeyPress(e, GamePanel.this);
                    case GAME_OVER -> menu.handleGameOverKeyPress(e, GamePanel.this);
                    case VICTORY -> menu.handleVictoryKeyPress(e, GamePanel.this, level >= MapData.LEVEL_COUNT);
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
                            restartCurrentLevel();
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
                            restartCurrentLevel();
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
                    case LEVEL_SELECT -> menu.handleLevelSelectMouseMove(mx, my, GamePanel.this);
                    case GAME_OVER -> menu.handleGameOverMouseMove(mx, my, GamePanel.this);
                    case VICTORY -> menu.handleVictoryMouseMove(mx, my, GamePanel.this, level >= MapData.LEVEL_COUNT);
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
                    case LEVEL_SELECT -> menu.handleLevelSelectMouseClick(mx, my, GamePanel.this);
                    case GAME_OVER -> menu.handleGameOverMouseClick(mx, my, GamePanel.this);
                    case VICTORY -> menu.handleVictoryMouseClick(mx, my, GamePanel.this, level >= MapData.LEVEL_COUNT);
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
            case LEVEL_SELECT -> {
                menu.drawLevelSelect(g, width, height);
            }
            case PLAYING, PAUSED, GAME_OVER, VICTORY -> {
                // Tự động căn giữa bản đồ nếu kích thước bản đồ nhỏ hơn cửa sổ
                int offsetX = Math.max(0, (width - map.getWidthInPixels()) / 2);
                int offsetY = Math.max(0, (height - map.getHeightInPixels()) / 2);

                g.setColor(new Color(15, 23, 42));
                g.fillRect(0, 0, width, height);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.translate(offsetX, offsetY);

                // 1. Vẽ bản đồ (cỏ, tường đá, gạch)
                map.draw(g2d);

                // 2. Vẽ Bombs
                for (Bomb b : bombs) {
                    b.draw(g2d);
                }

                // 3. Vẽ Vụ nổ
                for (Explosion exp : explosions) {
                    exp.draw(g2d);
                }

                // 4. Vẽ Kẻ địch
                for (Enemy enemy : enemies) {
                    enemy.draw(g2d);
                }

                // 5. Vẽ Player (nhấp nháy khi bất tử)
                if (state != GameState.GAME_OVER) {
                    if (invulnerableTimer == 0 || (invulnerableTimer / 5) % 2 == 0) {
                        player.draw(g2d);
                    }
                }

                g2d.dispose();

                // 6. HUD hiển thị thông tin
                drawHUD(g);

                // 7. Lớp phủ tương ứng với trạng thái
                if (state == GameState.PAUSED) {
                    menu.drawPause(g, width, height);
                } else if (state == GameState.GAME_OVER) {
                    menu.drawGameOver(g, width, height, score, highScore, level);
                } else if (state == GameState.VICTORY) {
                    menu.drawVictory(g, width, height, score, highScore, level);
                }
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
        g.drawString("MÀN: " + level + "  |  ĐIỂM: " + score + "  |  KỶ LỤC: " + highScore + "  |  MẠNG: " + lives
                + "  |  SPACE: ĐẶT BOM", 20, 27);
    }

    // Các phương thức chuyển trạng thái Game
    public void startGame() {
        startLevel(1);
    }

    public void resumeGame() {
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

    public GameState getState() {
        return state;
    }

    public GameMap getMap() {
        return map;
    }

    public Player getPlayer() {
        return player;
    }

    public Menu getMenu() {
        return menu;
    }

    public List<Bomb> getBombs() {
        return bombs;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }
}
