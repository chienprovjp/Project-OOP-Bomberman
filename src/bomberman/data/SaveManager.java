package bomberman.data;
import java.io.*;

public class SaveManager {
    // Tên file lưu trữ trên ổ cứng
    private static final String SAVE_FILE = "savegame.txt";

    /**
     * 1. GHI TIẾN TRÌNH RA FILE (SAVE)
     * Gọi hàm này khi qua màn, khi thoát game hoặc khi đạt điểm mới.
     */
    public static void saveProgress(int level, int score, int lives, int currentHighScore) {
        // Cập nhật High Score nếu điểm hiện tại cao hơn
        int newHighScore = Math.max(score, currentHighScore);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write("level=" + level);
            writer.newLine();
            writer.write("score=" + score);
            writer.newLine();
            writer.write("lives=" + lives);
            writer.newLine();
            writer.write("highScore=" + newHighScore);
            writer.newLine();
            System.out.println("[SaveManager] Đã lưu tiến trình thành công!");
        } catch (IOException e) {
            System.err.println("[SaveManager] Lỗi khi ghi file lưu: " + e.getMessage());
        }
    }

    /**
     * 2. ĐỌC TIẾN TRÌNH TỪ FILE (LOAD)
     * Gọi hàm này khi mở game hoặc khi bấm nút "TIẾP TỤC".
     */
    public static GameSaveData loadProgress() {
        File file = new File(SAVE_FILE);
        // Nếu chưa từng chơi hoặc chưa có file, trả về giá trị mặc định ban đầu
        if (!file.exists()) {
            return new GameSaveData(1, 0, 3, 0); // Màn 1, 0 điểm, 3 mạng, high score = 0
        }

        int level = 1;
        int score = 0;
        int lives = 3;
        int highScore = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    int value = Integer.parseInt(parts[1].trim());

                    switch (key) {
                        case "level" -> level = value;
                        case "score" -> score = value;
                        case "lives" -> lives = value;
                        case "highScore" -> highScore = value;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SaveManager] Lỗi khi đọc file lưu: " + e.getMessage());
        }

        return new GameSaveData(level, score, lives, highScore);
    }

    /**
     * 3. KIỂM TRA XEM CÓ BẢN LƯU CŨ ĐỂ TIẾP TỤC HAY KHÔNG
     */
    public static boolean hasSaveData() {
        File file = new File(SAVE_FILE);
        return file.exists() && file.length() > 0;
    }

    /**
     * 4. XÓA TIẾN TRÌNH (KHI BỊ GAME OVER HOẶC CHƠI LẠI TỪ ĐẦU)
     * Vẫn giữ lại High Score, chỉ reset level, score và lives.
     */
    public static void resetProgressOnly() {
        GameSaveData oldData = loadProgress();
        saveProgress(1, 0, 3, oldData.getHighScore());
    }
}