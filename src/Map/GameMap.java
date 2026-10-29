package src.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import src.Entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameMap extends Canvas {

    // Kích thước mỗi ô: 48x48px (13x13 ô = 624x624px, rất sắc nét trên màn hình 1280x720)
    public static final int TILE_SIZE = 48;

    private final GraphicsContext gc;

    private final Image grassImage;
    private final Image wallImage;
    private final Image brickImage;

    // Danh sách 3 người chơi
    private final List<Player> players = new ArrayList<>();

    public GameMap() {
        super(
            MapData.COLS * TILE_SIZE,
            MapData.ROWS * TILE_SIZE
        );

        gc = getGraphicsContext2D();
        gc.setImageSmoothing(false);

        grassImage = loadImage("/assets/images/grass.png");
        wallImage = loadImage("/assets/images/wall.png");
        brickImage = loadImage("/assets/images/brick.png");

        initPlayers();
        render();
    }

    private void initPlayers() {
        players.clear();

        // Player 1: Góc trên bên trái (1, 1) - Phím W, S, A, D - Đặt bom: SPACE
        Player p1 = new Player(
            1, "Player 1",
            1 * TILE_SIZE + 7, 1 * TILE_SIZE + 7,
            KeyCode.W, KeyCode.S, KeyCode.A, KeyCode.D, KeyCode.SPACE,
            Color.rgb(30, 144, 255)
        );

        // Player 2: Góc trên bên phải (1, 11) - Phím Mũi tên - Đặt bom: ENTER
        Player p2 = new Player(
            2, "Player 2",
            11 * TILE_SIZE + 7, 1 * TILE_SIZE + 7,
            KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER,
            Color.rgb(235, 59, 90)
        );

        // Player 3: Góc dưới bên trái (11, 1) - Phím I, K, J, L - Đặt bom: O
        Player p3 = new Player(
            3, "Player 3",
            1 * TILE_SIZE + 7, 11 * TILE_SIZE + 7,
            KeyCode.I, KeyCode.K, KeyCode.J, KeyCode.L, KeyCode.O,
            Color.rgb(38, 222, 129)
        );

        players.add(p1);
        players.add(p2);
        players.add(p3);
    }

    private Image loadImage(String path) {
        try {
            var url = getClass().getResource(path);
            if (url != null) {
                return new Image(url.toExternalForm());
            }
        } catch (Exception ignored) {}

        // Fallback kiểm tra trực tiếp từ thư mục assets
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        File file = new File(cleanPath);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        File fileInAssets = new File("assets/images/" + new File(path).getName());
        if (fileInAssets.exists()) {
            return new Image(fileInAssets.toURI().toString());
        }

        throw new IllegalArgumentException("Khong tim thay hinh anh: " + path);
    }

    /**
     * Cập nhật logic game (gọi mỗi frame trong AnimationTimer).
     */
    public void update() {
        for (Player p : players) {
            p.move();
        }
    }

    /**
     * Vẽ lại toàn bộ bản đồ và các nhân vật.
     */
    public void render() {
        drawMap();
        for (Player p : players) {
            p.draw(gc);
        }
    }

    public void drawMap() {
        gc.clearRect(0, 0, getWidth(), getHeight());

        for (int row = 0; row < MapData.ROWS; row++) {
            for (int col = 0; col < MapData.COLS; col++) {
                int tile = MapData.getTile(row, col);

                Image image;
                if (tile == MapData.GRASS) {
                    image = grassImage;
                } else if (tile == MapData.WALL) {
                    image = wallImage;
                } else {
                    image = brickImage;
                }

                gc.drawImage(
                    image,
                    col * TILE_SIZE,
                    row * TILE_SIZE,
                    TILE_SIZE,
                    TILE_SIZE
                );
            }
        }
    }

    public void handleKeyPressed(KeyCode code) {
        if (code == KeyCode.R) {
            resetPlayers();
            return;
        }
        for (Player p : players) {
            p.handleKeyPressed(code);
        }
    }

    public void handleKeyReleased(KeyCode code) {
        for (Player p : players) {
            p.handleKeyReleased(code);
        }
    }

    public void resetPlayers() {
        for (Player p : players) {
            p.reset();
        }
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getPlayer(int id) {
        for (Player p : players) {
            if (p.getId() == id) return p;
        }
        return null;
    }
}
