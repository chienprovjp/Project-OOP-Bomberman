package bomberman.entities;

import bomberman.gui.GameMap;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lớp đại diện cho Kẻ địch Ballom (quái vật quả bóng màu cam cơ bản).
 * Kế thừa từ lớp trừu tượng Enemy.
 *
 * Đặc điểm di chuyển của Ballom:
 * - Đi thẳng theo hướng hiện tại.
 * - Khi gặp vật cản (tường đá, tường gạch, quả bom), tự động rẽ sang hướng thông thoáng.
 * - Thỉnh thoảng tự đổi hướng ngẫu nhiên tại các ngã rẽ.
 */
public class Ballom extends Enemy {
    // Biến đếm khung hình để tạo hiệu ứng phập phồng khi di chuyển
    private int animationTick = 0;

    // Biến đếm thời gian cho hiệu ứng biến mất khi bị nổ trúng
    private int deathAnimation = 0;

    public Ballom(int startX, int startY) {
        // Kích thước Ballom chiếm khoảng 70% kích thước ô gạch để di chuyển linh hoạt
        super(startX, startY,
              (int) (GameMap.TILE_SIZE * 0.70),
              (int) (GameMap.TILE_SIZE * 0.70),
              2); // Tốc độ di chuyển = 2 pixel / khung hình
        this.scoreReward = 100;
    }

    /**
     * Cập nhật hiệu ứng hoạt họa ở mỗi khung hình.
     */
    @Override
    public void update() {
        animationTick++;
        if (!alive) {
            deathAnimation++;
        }
    }

    /**
     * Xử lý di chuyển và va chạm của Ballom:
     * - Bước 1: Tính khoảng dịch chuyển dự kiến (dx, dy).
     * - Bước 2: Kiểm tra xem vị trí mới có đâm vào vật cản (tường, gạch, bom) hay không.
     * - Bước 3: Di chuyển hoặc rẽ hướng.
     */
    @Override
    public void move(GameMap map, List<Bomb> bombs) {
        if (!alive) return;

        // Bước 1: Tính độ dời theo hướng đang đi
        int dx = 0;
        int dy = 0;
        switch (direction) {
            case UP    -> dy = -speed;
            case DOWN  -> dy = speed;
            case LEFT  -> dx = -speed;
            case RIGHT -> dx = speed;
        }

        // Bước 2: Kiểm tra va chạm với bản đồ và bom
        boolean willCollide = map.checkCollision(x + dx, y + dy, width, height)
                || checkBombCollision(x + dx, y + dy, bombs);

        // Bước 3: Xử lý hành động
        if (!willCollide) {
            // Không bị chặn: tiến bước
            x += dx;
            y += dy;

            // Tại các ngã rẽ thông thoáng, có xác suất nhỏ (~2%) tự đổi hướng để di chuyển linh hoạt
            if (random.nextInt(100) < 2) {
                tryChangeToAvailableDirection(map, bombs);
            }
        } else {
            // Bị vật cản chặn lại: tìm hướng đi trống khác để rẽ
            tryChangeToAvailableDirection(map, bombs);
        }
    }

    /**
     * Tìm các hướng đi khả dụng (không bị cản) và chọn ngẫu nhiên một hướng để đi.
     */
    private void tryChangeToAvailableDirection(GameMap map, List<Bomb> bombs) {
        List<Player.Direction> candidates = new ArrayList<>();

        for (Player.Direction dir : Player.Direction.values()) {
            if (dir == this.direction) continue; // Ưu tiên chọn hướng khác hướng hiện tại

            int testDx = 0;
            int testDy = 0;
            switch (dir) {
                case UP    -> testDy = -speed;
                case DOWN  -> testDy = speed;
                case LEFT  -> testDx = -speed;
                case RIGHT -> testDx = speed;
            }

            // Nếu hướng này không bị đâm vào tường/gạch/bom thì thêm vào danh sách
            if (!map.checkCollision(x + testDx, y + testDy, width, height)
                    && !checkBombCollision(x + testDx, y + testDy, bombs)) {
                candidates.add(dir);
            }
        }

        if (!candidates.isEmpty()) {
            // Xáo trộn và chọn ngẫu nhiên 1 trong các hướng đi được
            Collections.shuffle(candidates);
            this.direction = candidates.get(0);
        } else {
            // Nếu không có lối rẽ nào (ngõ cụt), quay đầu lại 180 độ
            this.direction = switch (this.direction) {
                case UP    -> Player.Direction.DOWN;
                case DOWN  -> Player.Direction.UP;
                case LEFT  -> Player.Direction.RIGHT;
                case RIGHT -> Player.Direction.LEFT;
            };
        }
    }

    /**
     * Vẽ hình ảnh Ballom lên màn hình.
     */
    @Override
    public void draw(Graphics g) {
        // Nếu đã bị tiêu diệt: Vẽ hiệu ứng mờ dần rồi biến mất
        if (!alive) {
            int alpha = Math.max(0, 200 - deathAnimation * 10);
            g.setColor(new Color(251, 146, 60, alpha));
            g.fillOval(x - deathAnimation / 2, y - deathAnimation / 2,
                    width + deathAnimation, height + deathAnimation);
            return;
        }

        // Bước 1: Bóng mờ dưới chân
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 2, y + height - 3, width - 4, 6);

        // Hiệu ứng phập phồng nhẹ khi di chuyển
        int pulse = (animationTick / 10) % 2 == 0 ? 1 : 0;
        int bodyW = width + pulse;
        int bodyH = height - pulse;
        int bodyX = x - pulse / 2;
        int bodyY = y + pulse;

        // Bước 2: Thân tròn màu cam rực rỡ đặc trưng của Ballom
        g.setColor(new Color(249, 115, 22)); // Màu cam
        g.fillOval(bodyX, bodyY, bodyW, bodyH);

        g.setColor(new Color(194, 65, 12)); // Viền cam đậm
        g.drawOval(bodyX, bodyY, bodyW, bodyH);

        // Bước 3: Vệt sáng bóng trên đầu
        g.setColor(new Color(255, 255, 255, 140));
        g.fillOval(bodyX + bodyW / 4, bodyY + 4, bodyW / 3, bodyH / 4);

        // Bước 4: Đôi mắt to tròn liếc nhìn theo hướng di chuyển
        int eyeOffsetX = switch (direction) {
            case LEFT  -> -3;
            case RIGHT -> 3;
            default    -> 0;
        };
        int eyeOffsetY = switch (direction) {
            case UP    -> -2;
            case DOWN  -> 2;
            default    -> 0;
        };

        int eye1X = bodyX + bodyW / 2 - 8 + eyeOffsetX;
        int eye2X = bodyX + bodyW / 2 + 2 + eyeOffsetX;
        int eyeY  = bodyY + bodyH / 3 + eyeOffsetY;

        // Tròng trắng mắt
        g.setColor(Color.WHITE);
        g.fillOval(eye1X, eyeY, 6, 8);
        g.fillOval(eye2X, eyeY, 6, 8);

        // Con ngươi đen bên trong
        g.setColor(Color.BLACK);
        int pupilOffsetX = eyeOffsetX / 2;
        int pupilOffsetY = eyeOffsetY / 2;
        g.fillOval(eye1X + 2 + pupilOffsetX, eyeY + 2 + pupilOffsetY, 3, 4);
        g.fillOval(eye2X + 2 + pupilOffsetX, eyeY + 2 + pupilOffsetY, 3, 4);

        // Bước 5: Miệng cười vui nhộn
        g.setColor(new Color(124, 45, 18));
        g.drawArc(bodyX + bodyW / 2 - 4 + eyeOffsetX, bodyY + bodyH / 2 + 4, 8, 5, 0, -180);
    }
}
