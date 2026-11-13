package bomberman.util;

import java.io.*;

public class SaveManager {
    // Tên file lưu trữ trên ổ cứng
    private static final String SAVE_FILE = "savegame.txt";

    /**
     * 1. GHI TIẾN TRÌNH RA FILE (SAVE)
     * @param hasOngoingGame Đánh dấu ván chơi có đang dở dang chưa hoàn thành hay không
     */
    public static void saveProgress(int level, int score, int lives, int currentHighScore, boolean hasOngoingGame) {
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
            writer.write("hasOngoingGame=" + hasOngoingGame);
            writer.newLine();
            System.out.println("[SaveManager] Đã lưu tiến trình thành công! (Ongoing: " + hasOngoingGame + ")");
        } catch (IOException e) {
            System.err.println("[SaveManager] Lỗi khi ghi file lưu: " + e.getMessage());
        }
    }

    public static void saveProgress(int level, int score, int lives, int currentHighScore) {
        saveProgress(level, score, lives, currentHighScore, true);
    }

    /**
     * 2. ĐỌC TIẾN TRÌNH TỪ FILE (LOAD)
     */
    public static GameSaveData loadProgress() {
        File file = new File(SAVE_FILE);
        // Nếu chưa từng chơi hoặc chưa có file, trả về giá trị mặc định ban đầu
        if (!file.exists()) {
            return new GameSaveData(1, 0, 3, 0, false);
        }

        int level = 1;
        int score = 0;
        int lives = 3;
        int highScore = 0;
        boolean hasOngoingGame = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String valStr = parts[1].trim();

                    switch (key) {
                        case "level" -> level = Math.max(1, Integer.parseInt(valStr));
                        case "score" -> score = Math.max(0, Integer.parseInt(valStr));
                        case "lives" -> lives = Integer.parseInt(valStr);
                        case "highScore" -> highScore = Math.max(0, Integer.parseInt(valStr));
                        case "hasOngoingGame" -> hasOngoingGame = Boolean.parseBoolean(valStr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[SaveManager] Lỗi khi đọc file lưu: " + e.getMessage());
        }

        // Tự động bảo vệ: Nếu số mạng <= 0 thì reset về 3 và hủy cờ ván dở dang
        if (lives <= 0) {
            lives = 3;
            hasOngoingGame = false;
        }

        return new GameSaveData(level, score, lives, highScore, hasOngoingGame);
    }

    /**
     * 3. KIỂM TRA XEM CÓ BẢN LƯU DỞ ĐỂ TIẾP TỤC HAY KHÔNG
     */
    public static boolean hasOngoingGame() {
        GameSaveData data = loadProgress();
        return data.hasOngoingGame() && data.getLives() > 0;
    }

    public static boolean hasSaveData() {
        File file = new File(SAVE_FILE);
        return file.exists() && file.length() > 0;
    }

    /**
     * 4. XÓA TIẾN TRÌNH DỞ DANG (KHI GAME OVER HOẶC BẮT ĐẦU VÁN MỚI)
     * Vẫn bảo tồn High Score, chỉ reset level, score, lives và tắt cờ ongoing.
     */
    public static void clearOngoingGameOnly() {
        GameSaveData oldData = loadProgress();
        saveProgress(1, 0, 3, oldData.getHighScore(), false);
    }

    public static void resetProgressOnly() {
        clearOngoingGameOnly();
    }
}
