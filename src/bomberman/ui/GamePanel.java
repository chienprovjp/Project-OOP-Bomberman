package bomberman.ui;

import bomberman.component.GameState;
import bomberman.model.Ballom;
import bomberman.model.Bomb;
import bomberman.model.Enemy;
import bomberman.model.Explosion;
import bomberman.model.GameMap;
import bomberman.model.Item;
import bomberman.model.MapData;
import bomberman.model.Player;
import bomberman.model.Portal;
import bomberman.util.GameSaveData;
import bomberman.util.SaveManager;

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

    // Danh sách bom, vụ nổ, kẻ địch và vật phẩm
    private List<Bomb> bombs = new ArrayList<>();
    private List<Explosion> explosions = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private Portal portal = null; // Cổng qua màn (xuất hiện sau khi diệt hết quái)
    private int lastEnemyX = -1;
    private int lastEnemyY = -1;

    private int maxBombs = 1; // Số lượng bom tối đa đặt cùng lúc
    private int bombRadius = 1; // Bán kính nổ (1 ô)
    private int invulnerableTimer = 0; // Bộ đếm thời gian bất tử sau khi hồi sinh
    private String notificationText = ""; // Thông báo khi nhặt được item
    private int notificationTimer = 0;

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

        setPreferredSize(
                new Dimension(GameMap.DEFAULT_COLS * GameMap.TILE_SIZE, GameMap.DEFAULT_ROWS * GameMap.TILE_SIZE));
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
        if (state != GameState.PLAYING)
            return;

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
                explosions.add(new Explosion(b.getTileCol(), b.getTileRow(), b.getRadius(), map, items));
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
                if (state != GameState.PLAYING)
                    return;
            }

            // Kiểm tra lửa thiêu trúng Kẻ địch
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && exp.hitsEntity(enemy)) {
                    enemy.kill();
                    lastEnemyX = enemy.getX();
                    lastEnemyY = enemy.getY();
                    score += enemy.getScoreReward();
                    if (score > highScore) {
                        highScore = score;
                    }

                    // Cơ chế 2: Rơi Item ngẫu nhiên khi diệt quái (25% tỉ lệ)
                    if (Math.random() < 0.25) {
                        int tileCol = (enemy.getX() + enemy.getWidth() / 2) / GameMap.TILE_SIZE;
                        int tileRow = (enemy.getY() + enemy.getHeight() / 2) / GameMap.TILE_SIZE;
                        if (map.getTile(tileRow, tileCol) == GameMap.GRASS) {
                            items.add(Item.createRandomItem(tileCol * GameMap.TILE_SIZE, tileRow * GameMap.TILE_SIZE));
                        }
                    }
                }
            }

            // Kiểm tra tia lửa thiêu rụi Item trên đất (Luật chuẩn Bomberman - chỉ cháy khi đã hết thời gian bảo vệ)
            Iterator<Item> itemBurnIt = items.iterator();
            while (itemBurnIt.hasNext()) {
                Item it = itemBurnIt.next();
                if (it.canBeBurned() && exp.hitsEntity(it)) {
                    itemBurnIt.remove();
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
                    if (state != GameState.PLAYING)
                        return;
                }
            } else {
                enemyIt.remove();
            }
        }

        // 4. Cập nhật danh sách Item (Nhặt vật phẩm)
        Iterator<Item> itemIt = items.iterator();
        while (itemIt.hasNext()) {
            Item item = itemIt.next();
            item.update();
            if (item.intersects(player)) {
                item.applyEffect(player, this);
                setNotification("NHẬN ĐƯỢC: " + item.getItemName());
                itemIt.remove();
            }
        }

        if (notificationTimer > 0) {
            notificationTimer--;
        }

        // 5. Kiểm tra điều kiện mở cổng: Đã tiêu diệt hết kẻ địch!
        if (enemies.isEmpty()) {
            if (portal == null) {
                int pCol, pRow;
                if (lastEnemyX >= 0 && lastEnemyY >= 0) {
                    pCol = (lastEnemyX + 16) / GameMap.TILE_SIZE;
                    pRow = (lastEnemyY + 16) / GameMap.TILE_SIZE;
                } else {
                    pCol = map.getCols() - 2;
                    pRow = map.getRows() - 2;
                }
                // Đảm bảo không đặt cổng vào tường cố định
                if (pRow <= 0 || pRow >= map.getRows() - 1 || pCol <= 0 || pCol >= map.getCols() - 1 
                        || map.getTile(pRow, pCol) == GameMap.WALL) {
                    pCol = map.getCols() - 2;
                    pRow = map.getRows() - 2;
                }
                portal = new Portal(pCol * GameMap.TILE_SIZE, pRow * GameMap.TILE_SIZE);
                setNotification("ĐÃ DIỆT HẾT QUÁI! HÃY BƯỚC VÀO CỔNG ĐỂ QUA MÀN!");
            } else {
                portal.update();
                // Người chơi bước vào cổng -> Kích hoạt qua màn / chiến thắng!
                if (player.intersects(portal)) {
                    triggerVictory();
                }
            }
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
        // Hết mạng -> lưu điểm kỷ lục, kết thúc ván chơi dở dang (ongoing = false)
        SaveManager.saveProgress(1, 0, 3, highScore, false);
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

        boolean isFinal = (level >= MapData.LEVEL_COUNT);
        // Nếu đã hoàn thành màn cuối -> ongoing = false; nếu chưa thì ongoing = true
        SaveManager.saveProgress(level, score, lives, highScore, !isFinal);

        this.state = GameState.VICTORY;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Tiếp tục ván chơi dở dang từ file lưu (Continue Game)
     */
    public void continueSavedGame() {
        GameSaveData data = SaveManager.loadProgress();
        this.level = Math.max(1, data.getLevel());
        this.score = Math.max(0, data.getScore());
        this.lives = data.getLives() > 0 ? data.getLives() : 3;
        this.highScore = data.getHighScore();

        map.loadLevel(this.level);
        bombs.clear();
        explosions.clear();
        items.clear();
        portal = null;
        lastEnemyX = -1;
        lastEnemyY = -1;
        notificationTimer = 0;
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        spawnEnemies(2 + this.level);
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
        System.out.println(
                "[GamePanel] Tiếp tục chơi: Màn " + this.level + " | Điểm: " + this.score + " | Mạng: " + this.lives);
    }

    /**
     * Mở màn hình chọn màn để bắt đầu ván mới hoàn toàn
     */
    public void startNewGame() {
        setState(GameState.LEVEL_SELECT);
    }

    /**
     * Bắt đầu một màn chơi mới từ đầu (reset điểm về 0, mạng về 3)
     */
    public void startLevelFromBeginning(int chosenLevel) {
        this.level = chosenLevel;
        this.score = 0;
        this.lives = 3;
        this.maxBombs = 1;
        this.bombRadius = 1;
        map.loadLevel(chosenLevel);
        bombs.clear();
        explosions.clear();
        items.clear();
        portal = null;
        lastEnemyX = -1;
        lastEnemyY = -1;
        notificationTimer = 0;
        player.reset(getPlayerSpawnX(), getPlayerSpawnY());
        spawnEnemies(2 + chosenLevel);
        // Lưu tiến trình bắt đầu
        SaveManager.saveProgress(chosenLevel, 0, 3, highScore, true);
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Bắt đầu màn chơi được chọn (giữ nguyên điểm và mạng hiện có)
     */
    public void startLevel(int chosenLevel) {
        this.level = chosenLevel;
        map.loadLevel(chosenLevel);
        bombs.clear();
        explosions.clear();
        items.clear();
        portal = null;
        lastEnemyX = -1;
        lastEnemyY = -1;
        notificationTimer = 0;
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
            this.level++;
            map.loadLevel(this.level);
            bombs.clear();
            explosions.clear();
            items.clear();
            portal = null;
            lastEnemyX = -1;
            lastEnemyY = -1;
            notificationTimer = 0;
            player.reset(getPlayerSpawnX(), getPlayerSpawnY());
            spawnEnemies(2 + this.level);
            // Lưu ngay khi vừa bước sang màn mới
            SaveManager.saveProgress(level, score, lives, highScore, true);
            this.state = GameState.PLAYING;
            setCursor(Cursor.getDefaultCursor());
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
        startLevelFromBeginning(1);
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
                    default -> {
                    }
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

                // 1.5. Vẽ Cổng qua màn nếu đã mở (nằm trên cỏ, dưới chân người chơi)
                if (portal != null) {
                    portal.draw(g2d);
                }

                // 2. Vẽ Items trên mặt đất (Cơ chế 1 & 2)
                for (Item item : items) {
                    item.draw(g2d);
                }

                // 3. Vẽ Bombs
                for (Bomb b : bombs) {
                    b.draw(g2d);
                }

                // 4. Vẽ Vụ nổ
                for (Explosion exp : explosions) {
                    exp.draw(g2d);
                }

                // 5. Vẽ Kẻ địch
                for (Enemy enemy : enemies) {
                    enemy.draw(g2d);
                }

                // 6. Vẽ Player (nhấp nháy khi bất tử)
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
        int hudWidth = 720;
        g.setColor(new Color(15, 23, 42, 215));
        g.fillRoundRect(10, 8, hudWidth, 28, 8, 8);
        g.setColor(new Color(56, 189, 248));
        g.drawRoundRect(10, 8, hudWidth, 28, 8, 8);

        g.setColor(new Color(241, 245, 249));
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("MÀN: " + level + "  |  ĐIỂM: " + score + "  |  MẠNG: " + lives
                + "  |  BOM: " + maxBombs + "  |  LỬA: " + bombRadius + "  |  TỐC ĐỘ: " + player.getSpeed()
                + "  |  SPACE: ĐẶT BOM", 18, 27);

        // Hiển thị thông báo khi vừa nhặt được Item
        if (notificationTimer > 0 && !notificationText.isEmpty()) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            java.awt.FontMetrics fm = g.getFontMetrics();
            int textW = fm.stringWidth(notificationText);
            int boxW = textW + 30;
            int boxX = (getWidth() - boxW) / 2;
            int boxY = 44;

            g.setColor(new Color(15, 23, 42, 235));
            g.fillRoundRect(boxX, boxY, boxW, 28, 8, 8);
            g.setColor(new Color(250, 204, 21)); // Viền vàng neon
            g.drawRoundRect(boxX, boxY, boxW, 28, 8, 8);

            g.setColor(new Color(253, 224, 71)); // Chữ vàng rực
            g.drawString(notificationText, boxX + 15, boxY + 19);
        }
    }

    public void increaseMaxBombs() {
        this.maxBombs = Math.min(8, this.maxBombs + 1);
    }

    public void increaseBombRadius() {
        this.bombRadius = Math.min(6, this.bombRadius + 1);
    }

    public void increaseLives() {
        this.lives = Math.min(9, this.lives + 1);
    }

    public void setNotification(String text) {
        this.notificationText = text;
        this.notificationTimer = 100; // ~1.6 giây
    }

    public List<Item> getItems() {
        return items;
    }

    // Các phương thức chuyển trạng thái Game
    public void startGame() {
        startLevelFromBeginning(1);
    }

    public void resumeGame() {
        this.state = GameState.PLAYING;
        setCursor(Cursor.getDefaultCursor());
    }

    public void returnToMenu() {
        player.stopMoving();
        bombs.clear();
        explosions.clear();
        items.clear();
        portal = null;
        lastEnemyX = -1;
        lastEnemyY = -1;
        notificationTimer = 0;
        // Thoát ra menu khi chưa xong ván -> lưu lại tiến trình dở dang
        SaveManager.saveProgress(level, score, lives, highScore, true);
        this.state = GameState.MENU;
        setCursor(Cursor.getDefaultCursor());
    }

    /**
     * Tự động lưu tiến trình khi người dùng tắt cửa sổ ứng dụng
     */
    public void onWindowClosing() {
        if (state == GameState.PLAYING || state == GameState.PAUSED) {
            player.stopMoving();
            SaveManager.saveProgress(level, score, lives, highScore, true);
            System.out.println("[GamePanel] Đã tự động lưu ván chơi dở dang trước khi thoát.");
        }
    }

    /**
     * Xử lý khi bấm nút Thoát Game trong Menu
     */
    public void onExitGame() {
        onWindowClosing();
        System.exit(0);
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
