package com.example.tomatoclock;

import java.io.Serializable;
import java.util.UUID;

public class PomodoroConfig implements Serializable {
    private String pomodoroId;
    private String name;
    private long focusTime;
    private int tomatoCount;
    private long createdTime;

    public PomodoroConfig() {
        // 生成唯一ID
        this.pomodoroId = UUID.randomUUID().toString();
        this.createdTime = System.currentTimeMillis();
    }

    public PomodoroConfig(String name, long focusTime, int tomatoCount) {
        this();
        this.name = name;
        this.focusTime = focusTime;
        this.tomatoCount = tomatoCount;
    }

    public String getPomodoroId() {
        return pomodoroId;
    }


    public String getName() {
        return name;
    }


    public long getFocusTime() {
        return focusTime;
    }


    public int getTomatoCount() {
        return tomatoCount;
    }




    @Override
    public String toString() {
        // 显示配置名称和专注时间
        return name + " (" + (focusTime / 60000) + "分钟)";
    }
}
