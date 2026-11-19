package bomberman.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hệ thống quản lý Âm thanh thuần Java (javax.sound.sampled).
 * Hỗ trợ:
 * 1. Tự động tìm và phát file .wav trong assets/sounds/ nếu có.
 * 2. Bộ tổng hợp âm thanh Retro 8-bit PCM tích hợp sẵn làm dự phòng
 *    (đảm bảo game luôn có đầy đủ hiệu ứng âm thanh dù chưa có file .wav).
 * 3. Chạy bất đồng bộ trên luồng daemon riêng để không gây giật lag Game Loop 60 FPS.
 */
public final class SoundManager {
    private static final float SAMPLE_RATE = 22050.0f;
    private static volatile boolean muted = false;

    private static final ExecutorService AUDIO_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Bomberman-Audio");
        t.setDaemon(true);
        return t;
    });

    private SoundManager() {
    }

    public static boolean isMuted() {
        return muted;
    }

    public static boolean toggleMute() {
        muted = !muted;
        return muted;
    }

    public static void setMuted(boolean isMuted) {
        muted = isMuted;
    }

    public static void playBombPlace() {
        playEffect("place_bomb.wav", () -> {
            // Âm thanh đặt bom: 2 nốt trầm ngắn (220Hz -> 160Hz)
            playTone(240, 45, 0.25, Waveform.SQUARE);
            playTone(165, 55, 0.22, Waveform.SQUARE);
        });
    }

    public static void playExplosion() {
        playEffect("explosion.wav", () -> {
            // Tiếng nổ bom: Nhiễu trắng (White noise) giảm dần âm lượng + âm bass trầm
            playExplosionNoise(320, 0.38);
        });
    }

    public static void playPickup() {
        playEffect("pickup.wav", () -> {
            // Nhặt vật phẩm: Hợp âm rải 3 nốt vui tai đi lên
            playTone(523, 55, 0.22, Waveform.SQUARE);
            playTone(659, 55, 0.22, Waveform.SQUARE);
            playTone(784, 90, 0.25, Waveform.SQUARE);
        });
    }

    public static void playPlayerHurt() {
        playEffect("hurt.wav", () -> {
            // Trúng đòn / mất mạng: Âm tụt xuống nhanh
            playTone(380, 75, 0.28, Waveform.SAWTOOTH);
            playTone(250, 90, 0.28, Waveform.SAWTOOTH);
            playTone(160, 120, 0.25, Waveform.SAWTOOTH);
        });
    }

    public static void playPortalOpen() {
        playEffect("portal.wav", () -> {
            // Mở cổng qua màn: Chime lấp lánh 4 nốt
            playTone(587, 65, 0.20, Waveform.SINE);
            playTone(740, 65, 0.20, Waveform.SINE);
            playTone(880, 65, 0.22, Waveform.SINE);
            playTone(1174, 150, 0.24, Waveform.SINE);
        });
    }

    public static void playVictory() {
        playEffect("victory.wav", () -> {
            // Nhạc ngắn chiến thắng
            playTone(523, 85, 0.24, Waveform.SQUARE);
            playTone(659, 85, 0.24, Waveform.SQUARE);
            playTone(784, 85, 0.24, Waveform.SQUARE);
            playTone(1046, 220, 0.28, Waveform.SQUARE);
        });
    }

    public static void playGameOver() {
        playEffect("gameover.wav", () -> {
            // Nhạc Game Over đi xuống trầm buồn
            playTone(392, 130, 0.25, Waveform.SAWTOOTH);
            playTone(349, 130, 0.25, Waveform.SAWTOOTH);
            playTone(311, 130, 0.25, Waveform.SAWTOOTH);
            playTone(261, 260, 0.25, Waveform.SAWTOOTH);
        });
    }

    public static void playMenuClick() {
        playEffect("click.wav", () -> playTone(660, 35, 0.16, Waveform.SINE));
    }

    private static void playEffect(String wavFileName, Runnable synthFallback) {
        if (muted) return;
        AUDIO_EXECUTOR.submit(() -> {
            if (muted) return;
            if (!tryPlayWav(wavFileName) && synthFallback != null) {
                synthFallback.run();
            }
        });
    }

    private static boolean tryPlayWav(String fileName) {
        String[] paths = {
            "assets/sounds/" + fileName,
            "Project-OOP-Bomberman/assets/sounds/" + fileName
        };
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(f)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    return true;
                } catch (Exception ignored) {
                    return false;
                }
            }
        }
        try (InputStream is = SoundManager.class.getResourceAsStream("/assets/sounds/" + fileName)) {
            if (is != null) {
                try (AudioInputStream ais = AudioSystem.getAudioInputStream(is)) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.start();
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private enum Waveform {
        SINE, SQUARE, SAWTOOTH
    }

    private static void playTone(int freqHz, int durationMs, double volume, Waveform type) {
        if (muted) return;
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) return;

        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[numSamples];
        double period = SAMPLE_RATE / freqHz;

        for (int i = 0; i < numSamples; i++) {
            double phase = (i % period) / period;
            double raw = switch (type) {
                case SINE     -> Math.sin(2.0 * Math.PI * phase);
                case SQUARE   -> (phase < 0.5) ? 1.0 : -1.0;
                case SAWTOOTH -> 2.0 * phase - 1.0;
            };
            // Envelope giảm nhẹ ở cuối nốt để không bị tiếng lách tách ("click")
            double env = (i > numSamples * 0.75)
                    ? (numSamples - i) / (numSamples * 0.25)
                    : 1.0;
            buffer[i] = (byte) (raw * env * volume * 127.0);
        }

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        } catch (Exception ignored) {
        }
    }

    private static void playExplosionNoise(int durationMs, double volume) {
        if (muted) return;
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) return;

        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[numSamples];
        double lastSample = 0;

        for (int i = 0; i < numSamples; i++) {
            double decay = Math.pow(1.0 - (double) i / numSamples, 1.8);
            double noise = (Math.random() * 2.0 - 1.0);
            // Bộ lọc thông thấp (low-pass) tạo tiếng nổ ầm trầm thay vì xì xì
            lastSample = lastSample * 0.72 + noise * 0.28;
            double bass = Math.sin(2.0 * Math.PI * i * 70.0 / SAMPLE_RATE) * 0.45;
            double mixed = (lastSample * 1.4 + bass) * decay * volume;
            mixed = Math.max(-1.0, Math.min(1.0, mixed));
            buffer[i] = (byte) (mixed * 127.0);
        }

        try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        } catch (Exception ignored) {
        }
    }
}
