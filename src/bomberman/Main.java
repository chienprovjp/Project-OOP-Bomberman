package bomberman;

import bomberman.ui.GamePanel;
import bomberman.util.ResourceLoader;

import javax.swing.JFrame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Bomberman - OOP UET");
        GamePanel gamePanel = new GamePanel();

        // Nạp icon cho thanh tiêu đề cửa sổ nếu có ảnh
        try {
            Image icon = ResourceLoader.loadImage("player.png");
            if (icon == null) {
                icon = ResourceLoader.loadImage("player_down.png");
            }
            if (icon != null) {
                window.setIconImage(icon);
            }
        } catch (Exception ignored) {}

        window.add(gamePanel);
        
        // Bắt sự kiện tắt cửa sổ để tự động lưu tiến trình dở dang
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gamePanel.onWindowClosing();
                window.dispose();
                System.exit(0);
            }
        });

        window.setResizable(false);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
