package src.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameMap extends Canvas {

    public static final int TILE_SIZE = 32;

    private final GraphicsContext gc;

    private final Image grassImage;
    private final Image wallImage;
    private final Image brickImage;

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

        drawMap();
    }

    private Image loadImage(String path) {
        var url = getClass().getResource(path);

        if (url == null) {
            throw new IllegalArgumentException(
                "Khong tim thay hinh anh: " + path
            );
        }

        return new Image(url.toExternalForm());
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

    public void updateMap() {
        drawMap();
    }
}
