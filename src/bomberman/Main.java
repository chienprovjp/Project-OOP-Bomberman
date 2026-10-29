package bomberman;

import bomberman.gui.GamePanel;
import javax.swing.JFrame;

/**
 * Điểm bắt đầu (Entry Point) của game Bomberman.
 * Lớp này chỉ có nhiệm vụ tạo cửa sổ game và hiển thị nó lên màn hình.
 */
public class Main {
    public static void main(String[] args) {
        // 1. Tạo cửa sổ game (JFrame) với tiêu đề
        JFrame window = new JFrame("Game Bomberman - Phien Ban So Cap");

        // 2. Tạo màn hình game (GamePanel chứa map và nhân vật)
        GamePanel gamePanel = new GamePanel();

        // 3. Gắn màn hình game vào cửa sổ
        window.add(gamePanel);

        // 4. Các cài đặt cơ bản cho cửa sổ:
        // - Tắt chương trình hoàn toàn khi bấm nút X (đóng cửa sổ)
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // - Không cho người chơi kéo giãn kích thước cửa sổ để tránh lệch map
        window.setResizable(false);
        // - Tự động căn chỉnh kích thước cửa sổ vừa khít với GamePanel
        window.pack();
        // - Đặt cửa sổ xuất hiện ở chính giữa màn hình máy tính
        window.setLocationRelativeTo(null);
        // - Hiển thị cửa sổ lên
        window.setVisible(true);
    }
}

