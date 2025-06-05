package com.example.tomatoclock;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class PomodoroStateStorage {
    private static final String TAG = "PomodoroStateStorage";
    private static final String PREF_NAME = "PomodoroStates";
    private static final String KEY_DEFAULT_STATE = "default_state";
    private static final String KEY_TIMESTAMP = "_timestamp";
    private static final String KEY_SOURCE = "_source"; // 存储来源标记

    // 存储来源常量
    private static final String SOURCE_SHARED_PREFS = "shared_prefs";
    private static final String SOURCE_FILE = "file";

    private Context context;
    private Gson gson;
    private String stateKey;
    private File stateFile;


    public PomodoroStateStorage(Context context, String stateKey) {
        this.context = context;
        this.gson = new Gson();
        // 使用pomodoroId作为状态键，确保唯一性
        this.stateKey = stateKey != null && !stateKey.isEmpty() ? stateKey : KEY_DEFAULT_STATE;
        Log.d(TAG, "Initialized with stateKey: " + this.stateKey);

        // 创建对应的文件存储位置
        this.stateFile = new File(context.getFilesDir(), "state_" + this.stateKey + ".json");
        Log.d(TAG, "State file path: " + stateFile.getAbsolutePath());
    }

    // 保存状态 - 双重保险策略
    public boolean saveState(PomodoroState state) {
        if (state == null) {
            Log.e(TAG, "Cannot save null state");
            return false;
        }

        boolean sharedPrefsSuccess = false;
        boolean fileSuccess = false;

        // 首先保存到SharedPreferences
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            String json = gson.toJson(state);
            editor.putString(stateKey, json);

            // 保存时间戳和来源
            editor.putLong(stateKey + KEY_TIMESTAMP, System.currentTimeMillis());
            editor.putString(stateKey + KEY_SOURCE, SOURCE_SHARED_PREFS);

            // 使用commit确保立即保存
            sharedPrefsSuccess = editor.commit();
            Log.d(TAG, "State saved to SharedPreferences: " + sharedPrefsSuccess);
        } catch (Exception e) {
            Log.e(TAG, "Error saving state to SharedPreferences: " + e.getMessage());
        }

        // 同时保存到文件
        try {
            String json = gson.toJson(state);
            FileOutputStream fos = new FileOutputStream(stateFile);
            fos.write(json.getBytes());
            fos.close();

            // 记录文件保存时间戳
            stateFile.setLastModified(System.currentTimeMillis());
            fileSuccess = true;
            Log.d(TAG, "State saved to file: " + fileSuccess);
        } catch (IOException e) {
            Log.e(TAG, "Error saving state to file: " + e.getMessage());
        }

        // 如果任一保存成功，返回true
        return sharedPrefsSuccess || fileSuccess;
    }

    // 加载状态 - 优先使用较新的状态
    public PomodoroState loadState() {
        PomodoroState sharedPrefsState = null;
        PomodoroState fileState = null;
        long sharedPrefsTimestamp = 0;
        long fileTimestamp = 0;

        // 尝试从SharedPreferences加载
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(stateKey, "");

            if (!json.isEmpty()) {
                Type type = new TypeToken<PomodoroState>() {}.getType();
                sharedPrefsState = gson.fromJson(json, type);
                sharedPrefsTimestamp = prefs.getLong(stateKey + KEY_TIMESTAMP, 0);
                Log.d(TAG, "Loaded state from SharedPreferences: timeLeft=" +
                        (sharedPrefsState != null ? sharedPrefsState.getTimeLeftInMillis() : "null") +
                        ", timestamp=" + sharedPrefsTimestamp);
            } else {
                Log.d(TAG, "No state found in SharedPreferences for key " + stateKey);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading state from SharedPreferences: " + e.getMessage());
        }

        // 尝试从文件加载
        try {
            if (stateFile.exists() && stateFile.length() > 0) {
                FileInputStream fis = new FileInputStream(stateFile);
                byte[] buffer = new byte[(int) stateFile.length()];
                fis.read(buffer);
                fis.close();

                String json = new String(buffer);
                Type type = new TypeToken<PomodoroState>() {}.getType();
                fileState = gson.fromJson(json, type);
                fileTimestamp = stateFile.lastModified();
                Log.d(TAG, "Loaded state from file: timeLeft=" +
                        (fileState != null ? fileState.getTimeLeftInMillis() : "null") +
                        ", timestamp=" + fileTimestamp);
            } else {
                Log.d(TAG, "No state file found or file is empty: " + stateFile.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading state from file: " + e.getMessage());
        }

        // 比较两个状态，返回较新的一个
        if (sharedPrefsState == null && fileState == null) {
            Log.d(TAG, "No state found in either storage method");
            return null;
        } else if (sharedPrefsState == null) {
            Log.d(TAG, "Using state from file (only available source)");
            return fileState;
        } else if (fileState == null) {
            Log.d(TAG, "Using state from SharedPreferences (only available source)");
            return sharedPrefsState;
        } else {
            // 两个都有，比较时间戳
            if (sharedPrefsTimestamp >= fileTimestamp) {
                Log.d(TAG, "Using state from SharedPreferences (newer or equal)");
                return sharedPrefsState;
            } else {
                Log.d(TAG, "Using state from file (newer)");
                return fileState;
            }
        }
    }

    // 清除状态 - 从两种存储中都删除
    public boolean clearState() {
        boolean sharedPrefsSuccess = false;
        boolean fileSuccess = false;

        // 清除SharedPreferences中的状态
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(stateKey);
            editor.remove(stateKey + KEY_TIMESTAMP);
            editor.remove(stateKey + KEY_SOURCE);

            // 使用commit确保立即删除
            sharedPrefsSuccess = editor.commit();
            Log.d(TAG, "State cleared from SharedPreferences: " + sharedPrefsSuccess);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing state from SharedPreferences: " + e.getMessage());
        }

        // 删除文件
        try {
            if (stateFile.exists()) {
                fileSuccess = stateFile.delete();
                Log.d(TAG, "State file deleted: " + fileSuccess);
            } else {
                fileSuccess = true; // 文件不存在，也算成功
                Log.d(TAG, "State file does not exist, no need to delete");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting state file: " + e.getMessage());
        }

        return sharedPrefsSuccess && fileSuccess;
    }

    // 检查是否存在状态 - 检查两种存储
    public boolean hasState() {
        boolean hasSharedPrefsState = false;
        boolean hasFileState = false;

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            hasSharedPrefsState = prefs.contains(stateKey);
            Log.d(TAG, "State exists in SharedPreferences: " + hasSharedPrefsState);
        } catch (Exception e) {
            Log.e(TAG, "Error checking SharedPreferences: " + e.getMessage());
        }

        try {
            hasFileState = stateFile.exists() && stateFile.length() > 0;
            Log.d(TAG, "State file exists: " + hasFileState);
        } catch (Exception e) {
            Log.e(TAG, "Error checking file: " + e.getMessage());
        }

        return hasSharedPrefsState || hasFileState;
    }

    // 获取状态的时间戳 - 取最新的时间戳
    public long getStateTimestamp() {
        long sharedPrefsTimestamp = 0;
        long fileTimestamp = 0;

        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            sharedPrefsTimestamp = prefs.getLong(stateKey + KEY_TIMESTAMP, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error getting SharedPreferences timestamp: " + e.getMessage());
        }

        try {
            if (stateFile.exists()) {
                fileTimestamp = stateFile.lastModified();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file timestamp: " + e.getMessage());
        }

        return Math.max(sharedPrefsTimestamp, fileTimestamp);
    }

    // 获取状态的来源
    public String getStateSource() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            return prefs.getString(stateKey + KEY_SOURCE, "unknown");
        } catch (Exception e) {
            Log.e(TAG, "Error getting state source: " + e.getMessage());
            return "unknown";
        }
    }
}
