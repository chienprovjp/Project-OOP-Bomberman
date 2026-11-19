package bomberman.model;

import bomberman.util.ResourceLoader;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Quản lý toàn bộ Hiệu ứng & Bẫy Môi trường theo từng Vùng địa hình (OOP Encapsulation):
 * - Hiệu ứng hạt thời tiết rơi (Lá rừng, Bụi/Tuyết đồ chơi, Tàn lửa núi lửa).
 * - Bẫy Đóng băng (Vùng 2: Màn 4-6): Xác suất 5% đóng băng nhân vật 0.5 giây khi bước sang ô mới.
 * - Bẫy Cột lửa Phun trào (Vùng 3: Màn 7-9): Xác suất 10% cảnh báo rung mặt đất 0.6 giây
 *   trước khi phun trào cột lửa gây sát thương để người chơi có cơ hội né tránh.
 */
public class MapEffect {
    public enum HazardEvent {
        NONE, FROZEN, LAVA_HIT
    }

    private static final int FREEZE_DURATION_TICKS = 30;
    private static final double FREEZE_CHANCE = 0.05;
    private static final double FIRE_CHANCE = 0.10;
    private static final int FIRE_WARNING_TICKS = 35; // Cảnh báo ~0.58s trước khi phun trào
    private static final int FIRE_ACTIVE_TICKS = 25;  // Thời gian cột lửa duy trì ~0.42s

    private BufferedImage particleImage;
    private final BufferedImage frozenPlayerImage = ResourceLoader.loadImage("player_frozen.png");
    private final BufferedImage lavaEruptionImage = ResourceLoader.loadImage("lava_eruption.png");

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    // Theo dõi ô đứng trước đó để chỉ kích hoạt bẫy khi nhân vật thực sự bước sang ô mới
    private int lastPlayerCol = -1;
    private int lastPlayerRow = -1;

    private int freezeTimer = 0;
    private int fireTileCol = -1;
    private int fireTileRow = -1;
    private int fireWarningTimer = 0;
    private int fireActiveTimer = 0;
    private boolean fireHitPlayer = false;

    private static class Particle {
        double x, y, speedX, speedY;
        float size;

        Particle(double x, double y, double speedX, double speedY, float size) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.size = size;
        }
    }

    public void loadThemeForLevel(int level) {
        int theme = 1;
        if (level >= 4 && level <= 6) {
            theme = 2;
        } else if (level >= 7 && level <= 9) {
            theme = 3;
        }
        this.particleImage = ResourceLoader.loadImage("particle" + theme + ".png");
    }

    public void reset(int level, int width, int height) {
        loadThemeForLevel(level);
        initializeParticles(width, height);
        this.freezeTimer = 0;
        this.fireTileCol = -1;
        this.fireTileRow = -1;
        this.fireWarningTimer = 0;
        this.fireActiveTimer = 0;
        this.fireHitPlayer = false;
        this.lastPlayerCol = -1;
        this.lastPlayerRow = -1;
    }

    public void initializeParticles(int width, int height) {
        particles.clear();
        if (width <= 0 || height <= 0) return;

        int particleCount = 6 + random.nextInt(5);
        for (int i = 0; i < particleCount; i++) {
            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height;
            double speedX = (random.nextDouble() - 0.5) * 1.8;
            double speedY = 0.5 + random.nextDouble() * 1.4;
            float size = 8 + random.nextFloat() * 8;
            particles.add(new Particle(x, y, speedX, speedY, size));
        }
    }

    public void updateParticles(int width, int height) {
        if (width <= 0 || height <= 0) return;
        for (Particle p : particles) {
            p.x += p.speedX;
            p.y += p.speedY;

            if (p.y > height) {
                p.y = -p.size;
                p.x = random.nextDouble() * width;
            }
            if (p.x > width) {
                p.x = -p.size;
            } else if (p.x < -p.size) {
                p.x = width;
            }
        }
    }

    /**
     * Cập nhật bộ đếm đóng băng trước khi cho phép Player di chuyển.
     */
    public boolean isPlayerFrozen() {
        return freezeTimer > 0;
    }

    public void tickFreezeTimer() {
        if (freezeTimer > 0) {
            freezeTimer--;
        }
    }

    public void clearFreeze() {
        this.freezeTimer = 0;
    }

    /**
     * Kiểm tra kích hoạt bẫy khi Player vừa bước sang một ô GRASS mới,
     * đồng thời cập nhật tiến trình cảnh báo -> phun trào cột lửa.
     */
    public HazardEvent updateHazardsAfterPlayerMove(int level, GameMap map, Player player, boolean isInvulnerable) {
        HazardEvent event = HazardEvent.NONE;

        int currentCol = (player.getX() + player.getWidth() / 2) / GameMap.TILE_SIZE;
        int currentRow = (player.getY() + player.getHeight() / 2) / GameMap.TILE_SIZE;

        boolean enteredNewGrass = (lastPlayerCol != -1 && (currentCol != lastPlayerCol || currentRow != lastPlayerRow))
                && map.getTile(currentRow, currentCol) == GameMap.GRASS;

        lastPlayerCol = currentCol;
        lastPlayerRow = currentRow;

        // Vùng 2 (Màn 4-6): Bẫy đóng băng
        if (level >= 4 && level <= 6 && enteredNewGrass && Math.random() < FREEZE_CHANCE) {
            freezeTimer = FREEZE_DURATION_TICKS;
            event = HazardEvent.FROZEN;
        }

        // Vùng 3 (Màn 7-9): Bẫy cột lửa phun trào (chỉ kích hoạt khi chưa có cột lửa đang chạy)
        if (level >= 7 && level <= 9 && enteredNewGrass
                && fireWarningTimer <= 0 && fireActiveTimer <= 0 && Math.random() < FIRE_CHANCE) {
            fireTileCol = currentCol;
            fireTileRow = currentRow;
            fireWarningTimer = FIRE_WARNING_TICKS;
            fireActiveTimer = 0;
            fireHitPlayer = false;
        }

        // Cập nhật pha cảnh báo -> pha phun trào cột lửa
        if (level < 7 || level > 9) {
            fireTileCol = -1;
            fireTileRow = -1;
            fireWarningTimer = 0;
            fireActiveTimer = 0;
            fireHitPlayer = false;
            return event;
        }

        if (fireWarningTimer > 0) {
            fireWarningTimer--;
            if (fireWarningTimer == 0) {
                fireActiveTimer = FIRE_ACTIVE_TICKS;
                fireHitPlayer = false;
            }
        } else if (fireActiveTimer > 0) {
            Rectangle fireBounds = new Rectangle(
                    fireTileCol * GameMap.TILE_SIZE + 4,
                    fireTileRow * GameMap.TILE_SIZE + 4,
                    GameMap.TILE_SIZE - 8,
                    GameMap.TILE_SIZE - 8);
            if (!fireHitPlayer && !isInvulnerable && fireBounds.intersects(player.getBounds())) {
                fireHitPlayer = true;
                event = HazardEvent.LAVA_HIT;
            }
            fireActiveTimer--;
            if (fireActiveTimer == 0) {
                fireTileCol = -1;
                fireTileRow = -1;
                fireHitPlayer = false;
            }
        }

        return event;
    }

    /**
     * Vẽ vòng cảnh báo hoặc cột lửa phun trào trên mặt đất.
     */
    public void drawGroundHazards(Graphics2D g2d) {
        if (fireTileCol < 0 || fireTileRow < 0) return;

        int x = fireTileCol * GameMap.TILE_SIZE;
        int y = fireTileRow * GameMap.TILE_SIZE;

        if (fireWarningTimer > 0) {
            // Vòng cảnh báo nhấp nháy giúp người chơi nhìn thấy và né kịp
            boolean blink = (fireWarningTimer / 4) % 2 == 0;
            g2d.setColor(blink ? new Color(239, 68, 68, 125) : new Color(249, 115, 22, 85));
            g2d.fillOval(x + 4, y + 4, GameMap.TILE_SIZE - 8, GameMap.TILE_SIZE - 8);
            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2.2f));
            g2d.drawOval(x + 4, y + 4, GameMap.TILE_SIZE - 8, GameMap.TILE_SIZE - 8);
        } else if (fireActiveTimer > 0) {
            int fireHeight = GameMap.TILE_SIZE * 3 / 2;
            if (lavaEruptionImage != null) {
                g2d.drawImage(lavaEruptionImage, x, y - fireHeight + GameMap.TILE_SIZE,
                        GameMap.TILE_SIZE, fireHeight, null);
            } else {
                g2d.setColor(new Color(239, 68, 68, 200));
                g2d.fillOval(x + 4, y - 10, GameMap.TILE_SIZE - 8, GameMap.TILE_SIZE + 10);
            }
        }
    }

    /**
     * Vẽ nhân vật khi đang bị đóng băng (trả về true nếu đã vẽ thay cho Player).
     */
    public boolean drawFrozenPlayerIfActive(Graphics2D g2d, Player player) {
        if (freezeTimer > 0 && frozenPlayerImage != null) {
            g2d.drawImage(frozenPlayerImage, player.getX(), player.getY(),
                    player.getWidth(), player.getHeight(), null);
            return true;
        }
        return false;
    }

    public void drawParticles(Graphics2D g2d) {
        if (particleImage == null) return;
        for (Particle p : particles) {
            g2d.drawImage(particleImage, (int) p.x, (int) p.y,
                    (int) p.size, (int) p.size, null);
        }
    }
}
