package com.example.tomatoclock;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class PomodoroActivity extends AppCompatActivity {
    private static final String TAG = "PomodoroActivity";

    // 时间常量
    private static final long SHORT_BREAK = 5 * 60 * 1000;
    private static final long LONG_BREAK = 10 * 60 * 1000;

    // UI组件
    private TextView timerTextView;
    private TextView statusTextView;
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private ParticleProgressView progressBar;
    private VideoView tomatoVideoView; // 视频播放组件

    // 计时器相关
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;
    private boolean isTimerRunning;
    private boolean isWorkTime;
    private long currentPhaseTotalTime;

    // 配置参数
    private long focusTime;
    private int totalTomatoCount;
    private int completedTomatoCount;
    private String pomodoroName;
    private boolean fromHistory;
    private String pomodoroId;

    // 广播动作常量
    public static final String ACTION_POMODORO_COMPLETED = "com.example.tomatoclock.ACTION_POMODORO_COMPLETED";

    // 音乐相关
    private MusicService musicService;
    private boolean isMusicBound = false;
    private boolean isMusicPlaying = false;
    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private MediaPlayer mediaPlayer3;
    private MediaPlayer mediaPlayer4;

    // 状态存储
    private PomodoroStateStorage stateStorage;

    // 服务连接
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isMusicBound = true;
            Log.d(TAG, "Music service connected");

            setupMusicErrorListener();
            musicService.setAutoRandomPlayEnabled(true);

            // 恢复音乐播放状态
            if (isMusicPlaying) {
                playRandomMusic();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isMusicBound = false;
            musicService = null;
            Log.d(TAG, "Music service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);


        //别管日志了，让我们创建音乐对象
        mediaPlayer1 = MediaPlayer.create(this, R.raw.levelup);
        mediaPlayer2 = MediaPlayer.create(this, R.raw._break);
        mediaPlayer3=MediaPlayer.create(this,R.raw.successful_hit);
        mediaPlayer4=MediaPlayer.create(this,R.raw.say1);

        Log.d(TAG, "Activity created");

        loadIntentParams();
        Log.d(TAG, "Pomodoro ID: " + pomodoroId);

        stateStorage = new PomodoroStateStorage(this, pomodoroId);
        PomodoroState savedState = stateStorage.loadState();
        if (savedState != null) {
            Log.d(TAG, "Saved state found for ID: " + pomodoroId);
        } else {
            Log.d(TAG, "No saved state found for ID: " + pomodoroId);
        }

        bindMusicService();
        initViews();
        restoreSavedState();
        setupUI();
        setupButtonListeners();

        if (isTimerRunning) {
            startTimer();
        }
    }

    private void setupMusicErrorListener() {
        if (musicService != null) {
            musicService.setOnPlayErrorListener(new MusicService.OnPlayErrorListener() {
                @Override
                public void onPlayError(String errorMessage) {
                    Log.e(TAG, "Music error: " + errorMessage);
                    runOnUiThread(() -> {
                        Toast.makeText(PomodoroActivity.this,
                                "音乐播放错误: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onRetryAttempt(int attempt, int maxAttempts) {
                    Log.d(TAG, "Music retry attempt " + attempt + "/" + maxAttempts);
                    // 可选：显示重试提示
                }
            });
        }
    }

    private void initViews() {
        timerTextView = findViewById(R.id.timerTextView);
        statusTextView = findViewById(R.id.statusTextView);
        startButton = findViewById(R.id.startButton);
        pauseButton = findViewById(R.id.pauseButton);
        resetButton = findViewById(R.id.resetButton);

        progressBar = findViewById(R.id.particleProgress);

        // 初始化视频播放组件
        tomatoVideoView = findViewById(R.id.tomatoVideoView);
        if (tomatoVideoView != null) {
            // 设置视频路径
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.minecraft);
            tomatoVideoView.setVideoURI(videoUri);

            // 设置循环播放
            // 设置循环播放
            tomatoVideoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
            });

            // 开始播放视频
            tomatoVideoView.start();
        }

        if (progressBar != null) {
            progressBar.setProgress(0);
            progressBar.setProgressColor(isWorkTime ? Color.parseColor("#FF5722") : Color.parseColor("#4CAF50"));
            progressBar.setSpeedMultiplier(1.0f);
        } else {
            Log.e(TAG, "ParticleProgressView not found in layout with ID particleProgress");
        }

        updateButtons();
    }


    private void loadIntentParams() {
        Intent intent = getIntent();
        focusTime = intent.getLongExtra("focusTime", 25 * 60 * 1000);
        totalTomatoCount = intent.getIntExtra("tomatoCount", 4);
        pomodoroName = intent.getStringExtra("pomodoroName");
        fromHistory = intent.getBooleanExtra("fromHistory", false);

        pomodoroId = intent.getStringExtra("pomodoroId");
        if (pomodoroId == null || pomodoroId.isEmpty()) {
            pomodoroId = UUID.randomUUID().toString();
            Log.d(TAG, "Generated new Pomodoro ID: " + pomodoroId);
        } else {
            Log.d(TAG, "Received Pomodoro ID from intent: " + pomodoroId);
        }

        Log.d(TAG, "Loaded intent params: focusTime=" + focusTime +
                ", totalTomatoCount=" + totalTomatoCount +
                ", pomodoroName=" + pomodoroName +
                ", fromHistory=" + fromHistory +
                ", pomodoroId=" + pomodoroId);
    }

    private void restoreSavedState() {
        Log.d(TAG, "Restoring state, current focusTime: " + focusTime);
        Log.d(TAG, "Attempting to restore state for pomodoroId: " + pomodoroId);

        PomodoroState storageState = stateStorage.loadState();
        PomodoroState intentState = extractStateFromIntent();
        PomodoroState finalState = chooseNewestState(storageState, intentState);

        if (finalState != null) {
            Log.d(TAG, "Using restored state: timeLeft=" + finalState.getTimeLeftInMillis() +
                    ", running=" + finalState.isTimerRunning() +
                    ", workTime=" + finalState.isWorkTime() +
                    ", completed=" + finalState.getCompletedTomatoCount());

            timeLeftInMillis = finalState.getTimeLeftInMillis();
            isTimerRunning = finalState.isTimerRunning();
            isWorkTime = finalState.isWorkTime();
            completedTomatoCount = finalState.getCompletedTomatoCount();
            currentPhaseTotalTime = finalState.getCurrentPhaseTotalTime();

            if (currentPhaseTotalTime <= 0) {
                Log.w(TAG, "Invalid currentPhaseTotalTime, using default focusTime: " + focusTime);
                currentPhaseTotalTime = focusTime;
            }

            isMusicPlaying = finalState.isMusicPlaying();
        } else {
            Log.d(TAG, "No state found, using default values");
            timeLeftInMillis = focusTime;
            isTimerRunning = false;
            isWorkTime = true;
            completedTomatoCount = 0;
            currentPhaseTotalTime = focusTime;
            isMusicPlaying = false;
        }

        Log.d(TAG, "Final state: timeLeft=" + timeLeftInMillis +
                ", running=" + isTimerRunning +
                ", workTime=" + isWorkTime +
                ", completed=" + completedTomatoCount);
    }

    private PomodoroState extractStateFromIntent() {
        Intent intent = getIntent();

        if (!intent.hasExtra("timeLeftInMillis")) {
            Log.d(TAG, "Intent does not contain state data");
            return null;
        }

        try {
            PomodoroState state = new PomodoroState();
            state.setTimeLeftInMillis(intent.getLongExtra("timeLeftInMillis", 0));
            state.setTimerRunning(intent.getBooleanExtra("isTimerRunning", false));
            state.setWorkTime(intent.getBooleanExtra("isWorkTime", true));
            state.setCompletedTomatoCount(intent.getIntExtra("completedTomatoCount", 0));
            state.setCurrentPhaseTotalTime(intent.getLongExtra("currentPhaseTotalTime", 0));
            state.setMusicPlaying(intent.getBooleanExtra("isMusicPlaying", false));
            state.setTimestamp(System.currentTimeMillis());

            Log.d(TAG, "Extracted state from intent: timeLeft=" + state.getTimeLeftInMillis() +
                    ", running=" + state.isTimerRunning());

            return state;
        } catch (Exception e) {
            Log.e(TAG, "Error extracting state from intent", e);
            return null;
        }
    }

    private PomodoroState chooseNewestState(PomodoroState state1, PomodoroState state2) {
        if (state1 == null) return state2;
        if (state2 == null) return state1;

        long timestamp1 = state1.getTimestamp();
        long timestamp2 = state2.getTimestamp();

        Log.d(TAG, "Comparing states: storage=" + timestamp1 + ", intent=" + timestamp2);

        if (timestamp1 >= timestamp2) {
            Log.d(TAG, "Using storage state (newer or equal)");
            return state1;
        } else {
            Log.d(TAG, "Using intent state (newer)");
            return state2;
        }
    }

    private void setupUI() {
        setTitle(pomodoroName);
        updateStatusText();
        updateCountText();
        updateTimerText(timeLeftInMillis);
        updateProgressBar();
    }

    private void setupButtonListeners() {
        startButton.setOnClickListener(v -> startTimer());
        pauseButton.setOnClickListener(v -> {
            pauseTimer();
            saveState();
            Log.d(TAG, "State saved on pause button click");
        });
        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void bindMusicService() {
        if (isMusicBound) {
            return;
        }

        try {
            Intent musicIntent = new Intent(this, MusicService.class);
            bindService(musicIntent, musicConnection, BIND_AUTO_CREATE);
            startService(musicIntent);
            Log.d(TAG, "Music service binding initiated");
        } catch (Exception e) {
            Log.e(TAG, "Error binding music service: " + e.getMessage());
        }
    }

    private void unbindMusicService() {
        if (isMusicBound) {
            unbindService(musicConnection);
            isMusicBound = false;
            musicService = null;
            Log.d(TAG, "Music service unbound");
        }
    }

    private void startTimer() {
        mediaPlayer1.start();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText(timeLeftInMillis);
                updateProgressBar();
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                updateButtons();
                toggleWorkBreak();
                if (isWorkTime) {
                    completedTomatoCount++;
                    saveState();
                }
            }
        }.start();

        isTimerRunning = true;
        updateButtons();
        playRandomMusic();
    }

    private void pauseTimer() {
        mediaPlayer2.start();//如果你看到这里，你可能会猜到一些谐音梗
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        updateButtons();

        pauseMusic();
    }

    private void resetTimer() {
        mediaPlayer3.start();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isTimerRunning = false;
        isWorkTime = true;
        completedTomatoCount = 0;
        currentPhaseTotalTime = focusTime;
        timeLeftInMillis = currentPhaseTotalTime;
        updateTimerText(timeLeftInMillis);
        updateButtons();
        updateProgressBar();

        saveState();
        stopMusic();
    }

    private void updateTimerText(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) ((millisUntilFinished / 1000) % 60);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    private void updateStatusText() {
        statusTextView.setText(isWorkTime ? "工作时间" : "休息时间");
    }

    private void updateCountText() {
        TextView countTextView = findViewById(R.id.countTextView);
        if (countTextView != null) {
            countTextView.setText(completedTomatoCount + "/" + totalTomatoCount);
        }
    }

    private void updateButtons() {
        if (isTimerRunning) {
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        } else {
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
        }

        resetButton.setEnabled(true);
    }

    private void updateProgressBar() {
        if (progressBar != null) {
            int progress = 0;
            if (currentPhaseTotalTime > 0) {
                progress = (int) (100 - ((timeLeftInMillis * 100) / currentPhaseTotalTime));
            } else {
                Log.e(TAG, "Invalid currentPhaseTotalTime in updateProgressBar: " + currentPhaseTotalTime);
            }

            progress = Math.max(0, Math.min(100, progress));
            progressBar.setProgress(progress);
            progressBar.setSpeedMultiplier(isWorkTime ? 1.5f : 1.0f);
        }
    }

    private void toggleWorkBreak() {
        mediaPlayer4.start();
        isWorkTime = !isWorkTime;//通过反复取反实现工作时间与休息时间的切换，这我在单片机的状态设置中见过

        if (isWorkTime) {
            // 切换到工作时间，使用focusTime
            currentPhaseTotalTime = focusTime;
        } else {
            // 切换到休息时间
            // 每完成4个番茄后，使用长休息(10分钟)，否则使用短休息(5分钟)
            currentPhaseTotalTime = (focusTime == 25 * 60 * 1000) ? SHORT_BREAK : LONG_BREAK;
        }

        if (currentPhaseTotalTime <= 0) {
            currentPhaseTotalTime = isWorkTime ? focusTime : SHORT_BREAK;
            Log.w(TAG, "Invalid currentPhaseTotalTime after toggle, using default: " + currentPhaseTotalTime);
        }

        timeLeftInMillis = currentPhaseTotalTime;
        updateStatusText();
        updateTimerText(timeLeftInMillis);
        updateProgressBar();

        if (progressBar != null) {
            progressBar.setProgressColor(isWorkTime ? Color.parseColor("#FF5722") : Color.parseColor("#4CAF50"));
        }

        saveState();
        playRandomMusic();

        if (isWorkTime) {
            Intent broadcastIntent = new Intent(ACTION_POMODORO_COMPLETED);
            broadcastIntent.putExtra("completedTomatoCount", completedTomatoCount);
            broadcastIntent.putExtra("totalTomatoCount", totalTomatoCount);
            sendBroadcast(broadcastIntent);
            Log.d(TAG, "Pomodoro completed broadcast sent: " + completedTomatoCount + "/" + totalTomatoCount);
        }
    }

    /**
     * 随机播放音乐
     */
    private void playRandomMusic() {
        if (!isMusicBound) {
            Log.w(TAG, "Music service not bound, trying to bind");
            bindMusicService();

            if (isMusicPlaying) {
                new Handler().postDelayed(this::playRandomMusic, 500);
            }
            return;
        }

        if (musicService != null) {
            Log.d(TAG, "Playing random music from network service");
            musicService.playRandomMusic();
            isMusicPlaying = true;
        } else {
            Log.e(TAG, "Music service not available");
        }
    }

    /**
     * 暂停音乐
     */
    private void pauseMusic() {
        if (isMusicBound && musicService != null && isMusicPlaying) {
            musicService.pauseMusic();
            isMusicPlaying = false;
        }
    }

    /**
     * 停止音乐
     */
    private void stopMusic() {
        if (isMusicBound && musicService != null && isMusicPlaying) {
            musicService.stopMusic();
            isMusicPlaying = false;
        }
    }

    /**
     * 保存当前番茄钟状态
     */
    private void saveState() {
        PomodoroState state = new PomodoroState();
        state.setTimeLeftInMillis(timeLeftInMillis);
        state.setTimerRunning(isTimerRunning);
        state.setWorkTime(isWorkTime);
        state.setCompletedTomatoCount(completedTomatoCount);
        state.setCurrentPhaseTotalTime(currentPhaseTotalTime);
        state.setMusicPlaying(isMusicPlaying);
        state.setTimestamp(System.currentTimeMillis());

        boolean success = stateStorage.saveState(state);
        Log.d(TAG, "State saved: timeLeft=" + timeLeftInMillis +
                ", running=" + isTimerRunning +
                ", phaseTotal=" + currentPhaseTotalTime +
                ", success=" + success);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
        Log.d(TAG, "State saved on pause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindMusicService();
        saveState();
        Log.d(TAG, "State saved on destroy");
    }
}