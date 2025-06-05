package com.example.tomatoclock;

import java.io.Serializable;

public class PomodoroState implements Serializable {
    private long timeLeftInMillis;
    private boolean isTimerRunning;
    private boolean isWorkTime;
    private int completedTomatoCount;
    private long currentPhaseTotalTime;
    private boolean isMusicPlaying;
    private long timestamp; // 新增时间戳字段

    // 构造函数
    public PomodoroState() {
        this.timestamp = System.currentTimeMillis(); // 默认使用当前时间
    }

    // getter和setter方法
    public long getTimeLeftInMillis() {
        return timeLeftInMillis;
    }

    public void setTimeLeftInMillis(long timeLeftInMillis) {
        this.timeLeftInMillis = timeLeftInMillis;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public void setTimerRunning(boolean timerRunning) {
        isTimerRunning = timerRunning;
    }

    public boolean isWorkTime() {
        return isWorkTime;
    }

    public void setWorkTime(boolean workTime) {
        isWorkTime = workTime;
    }

    public int getCompletedTomatoCount() {
        return completedTomatoCount;
    }

    public void setCompletedTomatoCount(int completedTomatoCount) {
        this.completedTomatoCount = completedTomatoCount;
    }

    public long getCurrentPhaseTotalTime() {
        return currentPhaseTotalTime;
    }

    public void setCurrentPhaseTotalTime(long currentPhaseTotalTime) {
        this.currentPhaseTotalTime = currentPhaseTotalTime;
    }

    public boolean isMusicPlaying() {
        return isMusicPlaying;
    }

    public void setMusicPlaying(boolean musicPlaying) {
        isMusicPlaying = musicPlaying;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
