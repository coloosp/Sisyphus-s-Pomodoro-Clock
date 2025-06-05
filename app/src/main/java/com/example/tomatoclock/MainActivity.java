package com.example.tomatoclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.media.MediaPlayer;


import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button startButton;
    private Button historyButton;
    private Button loginButton;
    private TextView welcomeText;
    private TextView usernameText;
    private TextView levelText;
    private ImageButton tomatoImageButton;
    private PomodoroConfigStorage configStorage;
    private SharedPreferences prefs;
    private BroadcastReceiver pomodoroReceiver;

    // 用户数据存储相关常量
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TOMATO_COUNT = "tomato_count";
    private static final String KEY_TOMATO_LEVEL = "tomato_level";
    private static final int LEVEL_UP_THRESHOLD = 5; // 每5个番茄升一级
    private static final int MAX_LEVEL = 8; // 最高8级

    //做点好玩的
    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private MediaPlayer mediaPlayer3;
    private int ClickCount=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate called");

        try {
            setContentView(R.layout.activity_main);
            Log.d("MainActivity", "Layout inflated successfully");


            //初始化MC音效
            mediaPlayer1 = MediaPlayer.create(this, R.raw.click);
            mediaPlayer2 = MediaPlayer.create(this, R.raw.crop1);
            mediaPlayer3 = MediaPlayer.create(this, R.raw.wood_click);

            // 初始化视图组件
            startButton = findViewById(R.id.startButton);
            historyButton = findViewById(R.id.historyButton);
            loginButton = findViewById(R.id.loginButton);
            welcomeText = findViewById(R.id.welcomeText);
            usernameText = findViewById(R.id.usernameText);
            levelText = findViewById(R.id.levelText);
            tomatoImageButton = findViewById(R.id.tomatoImageButton);

            configStorage = new PomodoroConfigStorage(this);
            prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            // 检查用户是否已登录
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            // 更新UI显示
            if (isLoggedIn) {
                String username = prefs.getString(KEY_USERNAME, "用户");
                usernameText.setText("欢迎回来，" + username);
                usernameText.setVisibility(View.VISIBLE);
                loginButton.setText("退出登录");

                // 显示番茄等级
                updateTomatoCountAndLevel();
            } else {
                usernameText.setVisibility(View.GONE);
                levelText.setVisibility(View.GONE);
                tomatoImageButton.setVisibility(View.GONE);
                loginButton.setText("登录/注册");
            }

            // 检查是否有保存的配置
            try {
                List<PomodoroConfig> configs = configStorage.loadAllConfigs();
                if (configs.isEmpty()) {
                    welcomeText.setText("欢迎使用番茄钟\n请创建您的第一个番茄钟配置");
                } else {
                    welcomeText.setText("您有 " + configs.size() + " 个番茄钟配置");
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error loading configs", e);
                welcomeText.setText("欢迎使用番茄钟");
            }

            // 设置按钮点击事件
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer1.start();
                    // 检查登录状态
                    if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                        // 已登录，跳转到设置界面
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    } else {
                        // 未登录，提示登录
                        Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            historyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer1.start();
                    // 检查登录状态
                    if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                        // 已登录，跳转到历史记录界面
                        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                        startActivity(intent);
                    } else {
                        // 未登录，提示登录
                        Toast.makeText(MainActivity.this, "请先登录", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // 设置番茄图片按钮点击事件
            tomatoImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer2.start();
                    ClickCount++; // 增加点击计数

                    if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                        // 显示番茄统计信息
                        int tomatoCount = prefs.getInt(KEY_TOMATO_COUNT, 0);
                        int level = prefs.getInt(KEY_TOMATO_LEVEL, 1);
                        Toast.makeText(MainActivity.this,
                                "番茄等级: " + level + "\n已完成番茄数: " + tomatoCount,
                                Toast.LENGTH_SHORT).show();
                    }

                    // 直接使用数值5进行比较
                    if (ClickCount >= 5) {
                        ClickCount = 0; // 重置计数器
                        Intent intent = new Intent(MainActivity.this, EggActivity.class);
                        startActivity(intent);
                    }
                }
            });

            // 设置登录/退出按钮点击事件
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer3.start();
                    if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                        // 已登录，执行退出操作
                        logout();
                    } else {
                        // 未登录，跳转到登录界面
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivityForResult(intent, 100);
                    }
                }
            });

            // 注册广播接收器
            registerPomodoroReceiver();
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "应用启动失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // 注册广播接收器
    private void registerPomodoroReceiver() {
        try {
            pomodoroReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (PomodoroActivity.ACTION_POMODORO_COMPLETED.equals(intent.getAction())) {
                        // 更新番茄计数和等级
                        updateTomatoCountAndLevel();
                    }
                }
            };

            IntentFilter filter = new IntentFilter(PomodoroActivity.ACTION_POMODORO_COMPLETED);
            registerReceiver(pomodoroReceiver, filter);
            Log.d("MainActivity", "Pomodoro receiver registered");
        } catch (Exception e) {
            Log.e("MainActivity", "Error registering receiver", e);
        }
    }

    // 更新番茄计数和等级的方法
    private void updateTomatoCountAndLevel() {
        try {
            if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                // 从SharedPreferences获取番茄计数和等级
                int tomatoCount = prefs.getInt(KEY_TOMATO_COUNT, 0);
                int tomatoLevel = prefs.getInt(KEY_TOMATO_LEVEL, 1);

                // 更新番茄图片
                updateTomatoImage(tomatoLevel);

                // 确保levelText不为null再设置文本
                if (levelText != null) {
                    levelText.setText("番茄等级: " + tomatoLevel);
                    levelText.setVisibility(View.VISIBLE);
                }

                // 确保tomatoImageButton不为null再设置可见性
                if (tomatoImageButton != null) {
                    tomatoImageButton.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error updating tomato count/level", e);
        }
    }

    // 更新番茄图片
    private void updateTomatoImage(int level) {
        try {
            // 根据等级选择对应的图片资源
            int imageResource = getResources().getIdentifier(
                    "tomato" + Math.min(level, MAX_LEVEL), "drawable", getPackageName());

            if (imageResource != 0) {
                tomatoImageButton.setImageResource(imageResource);
            } else {
                Log.w("MainActivity", "Tomato image resource not found for level: " + level);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error updating tomato image", e);
        }
    }

    // 退出登录
    private void logout() {
        try {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_IS_LOGGED_IN, false);
            editor.apply();

            // 更新UI
            usernameText.setVisibility(View.GONE);
            levelText.setVisibility(View.GONE);
            tomatoImageButton.setVisibility(View.GONE);
            loginButton.setText("登录/注册");

            Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("MainActivity", "Error logging out", e);
            Toast.makeText(this, "退出登录失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == 100 && resultCode == RESULT_OK) {
                // 从登录界面返回，更新UI
                boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
                if (isLoggedIn) {
                    String username = prefs.getString(KEY_USERNAME, "用户");
                    usernameText.setText("欢迎回来，" + username);
                    usernameText.setVisibility(View.VISIBLE);
                    loginButton.setText("退出登录");

                    // 显示番茄等级
                    updateTomatoCountAndLevel();
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onActivityResult", e);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            // 注销广播接收器
            if (pomodoroReceiver != null) {
                unregisterReceiver(pomodoroReceiver);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onDestroy", e);
        }
    }
}