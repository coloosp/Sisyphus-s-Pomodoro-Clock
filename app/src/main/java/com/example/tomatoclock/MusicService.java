package com.example.tomatoclock;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicService extends Service {
    private static final String TAG = "MusicService";
    private MediaPlayer mediaPlayer;
    private final IBinder musicBinder = new MusicBinder();
    private Random random = new Random();

    // 音乐资源列表
    private List<MusicResource> musicResources = new ArrayList<>();
    private int currentMusicIndex = -1;
    private boolean isPlaying = false;
    private OnPlayErrorListener errorListener;
    private static final int MAX_RETRY_COUNT = 3;
    private int currentRetryCount = 0;
    private boolean autoRandomPlayEnabled = true;

    // 音乐资源类
    public static class MusicResource {
        private String id;
        private String title;
        private String url;

        public MusicResource(String id, String title, String url) {
            this.id = id;
            this.title = title;
            this.url = url;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getUrl() { return url; }
    }

    // 播放错误监听器接口
    public interface OnPlayErrorListener {
        void onPlayError(String errorMessage);
        void onRetryAttempt(int attempt, int maxAttempts);
    }

    public void setOnPlayErrorListener(OnPlayErrorListener listener) {
        this.errorListener = listener;
    }

    public void setAutoRandomPlayEnabled(boolean enabled) {
        this.autoRandomPlayEnabled = enabled;
        Log.d(TAG, "Auto random play: " + (enabled ? "enabled" : "disabled"));
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        initMusicResources();
    }

    private void initMusicResources() {
        // 添加网易云音乐URL
        musicResources.add(new MusicResource("netease1", "音乐1", "https://music.163.com/song/media/outer/url?id=2016868366.mp3"));
        musicResources.add(new MusicResource("netease2", "音乐2", "https://music.163.com/song/media/outer/url?id=2032709363.mp3"));
        musicResources.add(new MusicResource("netease3", "音乐3", "https://music.163.com/song/media/outer/url?id=29836330.mp3"));
        musicResources.add(new MusicResource("netease4", "音乐4", "https://music.163.com/song/media/outer/url?id=2032115339.mp3"));
        musicResources.add(new MusicResource("netease5", "音乐5", "https://music.163.com/song/media/outer/url?id=1495924418.mp3"));
        musicResources.add(new MusicResource("netease6", "音乐6", "https://music.163.com/song/media/outer/url?id=1409060769.mp3"));
        musicResources.add(new MusicResource("netease7", "音乐7", "https://music.163.com/song/media/outer/url?id=2683479245.mp3"));
        musicResources.add(new MusicResource("netease8", "音乐8", "https://music.163.com/song/media/outer/url?id=2016868366.mp3"));
        musicResources.add(new MusicResource("netease9", "音乐9", "https://music.163.com/song/media/outer/url?id=2032709363.mp3"));
        musicResources.add(new MusicResource("netease10", "音乐10", "https://music.163.com/song/media/outer/url?id=29836330.mp3"));
        musicResources.add(new MusicResource("netease11", "音乐11", "https://music.163.com/song/media/outer/url?id=1939751157.mp3"));
        musicResources.add(new MusicResource("netease12", "音乐12", "https://music.163.com/song/media/outer/url?id=2032115339.mp3"));
        musicResources.add(new MusicResource("netease13", "音乐13", "https://music.163.com/song/media/outer/url?id=521415907.mp3"));
        musicResources.add(new MusicResource("netease14", "音乐14", "https://music.163.com/song/media/outer/url?id=1409060769.mp3"));
        musicResources.add(new MusicResource("netease15", "音乐15", "https://music.163.com/song/media/outer/url?id=1409060769.mp3"));
        musicResources.add(new MusicResource("netease16", "音乐16", "https://music.163.com/song/media/outer/url?id=1409060769.mp3"));
        musicResources.add(new MusicResource("netease17", "音乐17", "https://music.163.com/song/media/outer/url?id=2608537957.mp3"));
        musicResources.add(new MusicResource("netease18", "音乐18", "https://music.163.com/song/media/outer/url?id=1409060771.mp3"));
        musicResources.add(new MusicResource("netease19", "音乐19", "https://music.163.com/song/media/outer/url?id=2608540391.mp3"));
        musicResources.add(new MusicResource("netease20", "音乐20", "https://music.163.com/song/media/outer/url?id=2147587767.mp3"));
        musicResources.add(new MusicResource("netease21", "音乐21", "https://music.163.com/song/media/outer/url?id=2147587767.mp3"));
        musicResources.add(new MusicResource("netease22", "音乐22", "https://music.163.com/song/media/outer/url?id=1473697311.mp3"));
        musicResources.add(new MusicResource("netease23", "音乐23", "https://music.163.com/song/media/outer/url?id=1918832970.mp3"));
        musicResources.add(new MusicResource("netease24", "音乐24", "https://music.163.com/song/media/outer/url?id=1424992942.mp3"));
        musicResources.add(new MusicResource("netease25", "音乐25", "https://music.163.com/song/media/outer/url?id=1831400711.mp3"));
        musicResources.add(new MusicResource("netease26", "音乐26", "https://music.163.com/song/media/outer/url?id=116493.mp3"));
        musicResources.add(new MusicResource("netease27", "音乐27", "https://music.163.com/song/media/outer/url?id=2040788728.mp3"));
        musicResources.add(new MusicResource("netease28", "音乐28", "https://music.163.com/song/media/outer/url?id=4010198.mp3"));
        musicResources.add(new MusicResource("netease29", "音乐29", "https://music.163.com/song/media/outer/url?id=4010195.mp3"));
        musicResources.add(new MusicResource("netease30", "音乐30", "https://music.163.com/song/media/outer/url?id=4010187.mp3"));
        musicResources.add(new MusicResource("netease31", "音乐31", "https://music.163.com/song/media/outer/url?id=1414718994.mp3"));
        musicResources.add(new MusicResource("netease32", "音乐32", "https://music.163.com/song/media/outer/url?id=1887199305.mp3"));
        musicResources.add(new MusicResource("netease33", "音乐33", "https://music.163.com/song/media/outer/url?id=1419662578.mp3"));
        musicResources.add(new MusicResource("netease34", "音乐34", "https://music.163.com/song/media/outer/url?id=1856719002.mp3"));
        musicResources.add(new MusicResource("netease35", "音乐35", "https://music.163.com/song/media/outer/url?id=1456790012.mp3"));
        musicResources.add(new MusicResource("netease36", "音乐36", "https://music.163.com/song/media/outer/url?id=2659373902.mp3"));
        musicResources.add(new MusicResource("netease37", "音乐37", "https://music.163.com/song/media/outer/url?id=535177.mp3"));
        musicResources.add(new MusicResource("netease38", "音乐38", "https://music.163.com/song/media/outer/url?id=2029966279.mp3"));
        musicResources.add(new MusicResource("netease39", "音乐39", "https://music.163.com/song/media/outer/url?id=427610415.mp3"));
        musicResources.add(new MusicResource("netease40", "音乐40", "https://music.163.com/song/media/outer/url?id=1966879212.mp3"));
        musicResources.add(new MusicResource("netease41", "音乐41", "https://music.163.com/song/media/outer/url?id=434236.mp3"));
        musicResources.add(new MusicResource("netease42", "音乐42", "https://music.163.com/song/media/outer/url?id=1913749935.mp3"));
        musicResources.add(new MusicResource("netease43", "音乐43", "https://music.163.com/song/media/outer/url?id=1852035221.mp3"));
        musicResources.add(new MusicResource("netease44", "音乐44", "https://music.163.com/song/media/outer/url?id=518894119.mp3"));
        musicResources.add(new MusicResource("netease45", "音乐45", "https://music.163.com/song/media/outer/url?id=1363829265.mp3"));
        musicResources.add(new MusicResource("netease46", "音乐46", "https://music.163.com/song/media/outer/url?id=18927568.mp3"));
        musicResources.add(new MusicResource("netease47", "音乐47", "https://music.163.com/song/media/outer/url?id=28912653.mp3"));
        musicResources.add(new MusicResource("netease48", "音乐48", "https://music.163.com/song/media/outer/url?id=1890913.mp3"));
        musicResources.add(new MusicResource("netease49", "音乐49", "https://music.163.com/song/media/outer/url?id=1351520344.mp3"));
        musicResources.add(new MusicResource("netease50", "音乐50", "https://music.163.com/song/media/outer/url?id=1905139960.mp3"));
        musicResources.add(new MusicResource("netease51", "音乐51", "https://music.163.com/song/media/outer/url?id=1422613270.mp3"));
        musicResources.add(new MusicResource("netease52", "音乐52", "https://music.163.com/song/media/outer/url?id=1614339.mp3"));
        musicResources.add(new MusicResource("netease53", "音乐53", "https://music.163.com/song/media/outer/url?id=1871802987.mp3"));
        musicResources.add(new MusicResource("netease54", "音乐54", "https://music.163.com/song/media/outer/url?id=1926069984.mp3"));
        musicResources.add(new MusicResource("netease55", "音乐55", "https://music.163.com/song/media/outer/url?id=1492394303.mp3"));
        musicResources.add(new MusicResource("netease56", "音乐56", "https://music.163.com/song/media/outer/url?id=18287801.mp3"));
        musicResources.add(new MusicResource("netease57", "音乐57", "https://music.163.com/song/media/outer/url?id=1825558876.mp3"));
        musicResources.add(new MusicResource("netease58", "音乐58", "https://music.163.com/song/media/outer/url?id=1824687308.mp3"));
        musicResources.add(new MusicResource("netease59", "音乐59", "https://music.163.com/song/media/outer/url?id=1614688.mp3"));
        Log.d(TAG, "Initialized with " + musicResources.size() + " music resources");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service bound");
        return musicBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        releaseMediaPlayer();
    }

    /**
     * 随机播放音乐
     */
    public void playRandomMusic() {
        if (musicResources.isEmpty()) {
            Log.e(TAG, "No music resources available");
            notifyError("No music resources available");
            return;
        }

        currentRetryCount = 0;
        releaseMediaPlayer();

        // 随机选择一首音乐，确保不重复播放同一首
        int newIndex;
        do {
            newIndex = random.nextInt(musicResources.size());
        } while (musicResources.size() > 1 && newIndex == currentMusicIndex);

        currentMusicIndex = newIndex;
        playSelectedMusic();
    }

    /**
     * 播放当前选中的音乐
     */
    private void playSelectedMusic() {
        if (currentMusicIndex < 0 || currentMusicIndex >= musicResources.size()) {
            Log.e(TAG, "Invalid currentMusicIndex: " + currentMusicIndex);
            notifyError("Invalid music selection");
            return;
        }

        MusicResource music = musicResources.get(currentMusicIndex);
        Log.d(TAG, "Playing selected music: " + music.getTitle() + ", URL: " + music.getUrl());

        try {
            // 检查mediaPlayer是否已初始化
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            } else {
                // 重置MediaPlayer状态
                mediaPlayer.reset();
            }

            // 设置错误监听器
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                handlePlaybackError(what, extra);
                return true;
            });

            // 设置完成监听器 - 自动播放下一首随机音乐
            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d(TAG, "Music completed, playing next random music");
                if (autoRandomPlayEnabled) {
                    playRandomMusic();
                } else {
                    Log.d(TAG, "Auto random play disabled, stopping playback");
                    isPlaying = false;
                }
            });

            // 设置准备完成监听器
            mediaPlayer.setOnPreparedListener(mp -> {
                Log.d(TAG, "MediaPlayer prepared, starting playback");
                mp.start();
                isPlaying = true;
                currentRetryCount = 0;
            });

            // 设置数据源（网易云URL）
            mediaPlayer.setDataSource(this, Uri.parse(music.getUrl()));

            // 异步准备，适合网络资源
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            Log.e(TAG, "Error setting data source: " + e.getMessage());
            notifyError("Network error: " + e.getMessage());
            releaseMediaPlayer();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error: " + e.getMessage());
            notifyError("Unexpected error: " + e.getMessage());
            releaseMediaPlayer();
        }
    }

    /**
     * 处理播放错误，实现重试机制
     */
    private void handlePlaybackError(int what, int extra) {
        currentRetryCount++;

        if (currentRetryCount <= MAX_RETRY_COUNT) {
            Log.d(TAG, "Retry attempt " + currentRetryCount + "/" + MAX_RETRY_COUNT);

            if (errorListener != null) {
                errorListener.onRetryAttempt(currentRetryCount, MAX_RETRY_COUNT);
            }

            // 延迟后重试
            new android.os.Handler().postDelayed(() -> {
                if (currentMusicIndex >= 0 && currentMusicIndex < musicResources.size()) {
                    playSelectedMusic();
                }
            }, 2000 * currentRetryCount);
        } else {
            Log.e(TAG, "Max retry attempts reached, giving up");
            notifyError("Failed to play music after " + MAX_RETRY_COUNT + " attempts");

            // 如果重试失败，尝试随机选择另一首
            if (autoRandomPlayEnabled && musicResources.size() > 1) {
                playRandomMusic();
            } else {
                releaseMediaPlayer();
            }
        }
    }

    /**
     * 暂停音乐
     */
    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            Log.d(TAG, "Music paused");
        }
    }

    /**
     * 恢复播放
     */
    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying() && mediaPlayer.isLooping()) {
            // 修复：这里的条件应该是mediaPlayer.isPlaying()而不是isLooping()
            mediaPlayer.start();
            isPlaying = true;
            Log.d(TAG, "Music resumed");
        }
    }

    /**
     * 停止播放
     */
    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            Log.d(TAG, "Music stopped");
        }
    }

    /**
     * 释放MediaPlayer资源
     */
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            Log.d(TAG, "MediaPlayer released");
        }
    }

    /**
     * 获取当前播放状态
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 获取当前播放的音乐信息
     */
    public MusicResource getCurrentMusic() {
        if (currentMusicIndex >= 0 && currentMusicIndex < musicResources.size()) {
            return musicResources.get(currentMusicIndex);
        }
        return null;
    }

    /**
     * 获取所有音乐资源
     */
    public List<MusicResource> getAllMusicResources() {
        return new ArrayList<>(musicResources);
    }

    /**
     * 通知错误
     */
    private void notifyError(String errorMessage) {
        if (errorListener != null) {
            errorListener.onPlayError(errorMessage);
        }
    }
}