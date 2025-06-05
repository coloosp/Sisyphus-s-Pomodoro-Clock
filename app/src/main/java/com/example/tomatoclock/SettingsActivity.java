package com.example.tomatoclock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgFocusTime;
    private EditText etTomatoCount;
    private EditText etPomodoroName;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        rgFocusTime = findViewById(R.id.rg_focus_time);
        etTomatoCount = findViewById(R.id.et_tomato_count);
        etPomodoroName = findViewById(R.id.et_pomodoro_name);
        mediaPlayer = MediaPlayer.create(this, R.raw.close3);

        // 从历史记录加载配置
        loadConfigFromHistory();
    }

    // 从历史记录加载配置
    private void loadConfigFromHistory() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("fromHistory", false)) {
            // 从历史记录加载配置
            long focusTime = intent.getLongExtra("focusTime", 25 * 60 * 1000);
            int tomatoCount = intent.getIntExtra("tomatoCount", 4);
            String pomodoroName = intent.getStringExtra("pomodoroName");

            // 设置UI
            if (focusTime == 50 * 60 * 1000) {
                rgFocusTime.check(R.id.rb_50);
            } else {
                rgFocusTime.check(R.id.rb_25);
            }

            etTomatoCount.setText(String.valueOf(tomatoCount));
            etPomodoroName.setText(pomodoroName);
        }
    }

    public void saveSettings(View view) {
        mediaPlayer.start();
        int selectedId = rgFocusTime.getCheckedRadioButtonId();
        long focusTime = 25 * 60 * 1000; // 默认25分钟，转换为毫秒
        if (selectedId == R.id.rb_50) {
            focusTime = 50 * 60 * 1000; // 50分钟，转换为毫秒
        }

        String tomatoCountStr = etTomatoCount.getText().toString();
        int tomatoCount = 4; // 默认值
        if (!tomatoCountStr.isEmpty()) {
            try {
                tomatoCount = Integer.parseInt(tomatoCountStr);
                // 确保番茄数量在合理范围内
                if (tomatoCount <= 0) {
                    tomatoCount = 4;
                }
            } catch (NumberFormatException e) {
                tomatoCount = 4; // 如果解析失败，使用默认值
            }
        }

        // 获取番茄钟名称（如果为空，使用默认名称）
        String pomodoroName = etPomodoroName.getText().toString().trim();
        if (pomodoroName.isEmpty()) {
            pomodoroName = "番茄钟_" + System.currentTimeMillis();
        }

        // 创建配置对象
        PomodoroConfig config = new PomodoroConfig(pomodoroName, focusTime, tomatoCount);

        // 保存配置到历史记录
        PomodoroConfigStorage storage = new PomodoroConfigStorage(this);
        storage.saveConfig(config);

        // 创建一个 Intent 用于启动 PomodoroActivity
        Intent intent = new Intent(SettingsActivity.this, PomodoroActivity.class);
        // 将专注时间和番茄个数放入 Intent 中
        intent.putExtra("focusTime", focusTime);
        intent.putExtra("tomatoCount", tomatoCount);
        intent.putExtra("pomodoroName", pomodoroName);
        // 启动新的 Activity
        startActivity(intent);
        // 关闭当前设置页面
        finish();
    }

}