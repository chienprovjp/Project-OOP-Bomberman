package bomberman;

import bomberman.gui.GamePanel;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Bomberman - OOP UET");
        GamePanel gamePanel = new GamePanel();

        // Nạp icon cho thanh tiêu đề cửa sổ nếu có ảnh
        try {
            File iconFile = new File("assets/images/player.png");
            if (!iconFile.exists()) {
                iconFile = new File("assets/images/player_down.png");
            }
            if (iconFile.exists()) {
                window.setIconImage(ImageIO.read(iconFile));
            }
        } catch (Exception ignored) {
        }

        window.add(gamePanel);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
