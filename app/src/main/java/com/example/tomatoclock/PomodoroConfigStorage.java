package com.example.tomatoclock;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PomodoroConfigStorage {
    private static final String PREF_NAME = "PomodoroConfigs";
    private static final String KEY_CONFIGS = "configs";

    private Context context;
    private Gson gson;

    public PomodoroConfigStorage(Context context) {
        this.context = context;
        this.gson = new Gson();
    }

    // 保存单个配置
    public void saveConfig(PomodoroConfig config) {
        // 加载所有配置
        List<PomodoroConfig> configs = loadAllConfigs();

        // 检查是否已存在相同ID的配置
        boolean found = false;
        for (int i = 0; i < configs.size(); i++) {
            if (configs.get(i).getPomodoroId().equals(config.getPomodoroId())) {
                // 更新现有配置
                configs.set(i, config);
                found = true;
                break;
            }
        }

        // 如果不存在，则添加新配置
        if (!found) {
            configs.add(config);
        }

        // 保存所有配置
        saveAllConfigs(configs);
    }

    // 保存所有配置
    private void saveAllConfigs(List<PomodoroConfig> configs) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String json = gson.toJson(configs);
        editor.putString(KEY_CONFIGS, json);
        editor.apply();
    }

    // 加载所有配置
    public List<PomodoroConfig> loadAllConfigs() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CONFIGS, "");

        if (json.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<PomodoroConfig>>() {}.getType();
        return gson.fromJson(json, type);
    }

    // 清除所有配置
    public void clearAllConfigs() {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CONFIGS);
        editor.apply();
    }
}
