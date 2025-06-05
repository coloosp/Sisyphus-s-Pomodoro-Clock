package com.example.tomatoclock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private List<PomodoroConfig> pomodoroConfigs;
    private ArrayAdapter<PomodoroConfig> adapter;
    private PomodoroConfigStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        pomodoroConfigs = new ArrayList<>();
        storage = new PomodoroConfigStorage(this);

        // 设置列表适配器
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, pomodoroConfigs);
        historyListView.setAdapter(adapter);

        // 从本地存储加载历史记录
        loadHistoryConfigs();

        // 设置列表项点击事件 - 直接跳转到番茄钟界面
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取选中的配置
                PomodoroConfig selectedConfig = pomodoroConfigs.get(position);

                // 直接跳转到番茄钟界面
                Intent intent = new Intent(HistoryActivity.this, PomodoroActivity.class);
                intent.putExtra("focusTime", selectedConfig.getFocusTime());
                intent.putExtra("tomatoCount", selectedConfig.getTomatoCount());
                intent.putExtra("pomodoroName", selectedConfig.getName());
                intent.putExtra("fromHistory", true); // 标记从历史记录启动

                // 传递pomodoroId
                intent.putExtra("pomodoroId", selectedConfig.getPomodoroId());

                // 尝试从存储中加载状态并传递
                PomodoroStateStorage stateStorage = new PomodoroStateStorage(HistoryActivity.this, selectedConfig.getPomodoroId());
                PomodoroState state = stateStorage.loadState();

                if (state != null) {
                    // 传递状态数据
                    intent.putExtra("timeLeftInMillis", state.getTimeLeftInMillis());
                    intent.putExtra("isTimerRunning", state.isTimerRunning());
                    intent.putExtra("isWorkTime", state.isWorkTime());
                    intent.putExtra("completedTomatoCount", state.getCompletedTomatoCount());
                    intent.putExtra("currentPhaseTotalTime", state.getCurrentPhaseTotalTime());
                    intent.putExtra("isMusicPlaying", state.isMusicPlaying());


                }
                startActivity(intent);
            }
        });

        // 设置列表项长按事件 - 删除配置
        historyListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                // 显示确认对话框
                AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
                builder.setTitle("删除配置");
                builder.setMessage("确定要删除这个番茄钟配置吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 删除选中的配置
                        deleteConfig(position);
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.show();

                // 返回true表示事件已处理，不会触发短按事件
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载历史记录，确保显示最新状态
        loadHistoryConfigs();
    }

    // 从本地存储加载历史记录
    private void loadHistoryConfigs() {
        // 从本地存储加载历史记录
        pomodoroConfigs.clear();
        pomodoroConfigs.addAll(storage.loadAllConfigs());

        // 如果没有历史记录，显示提示
        if (pomodoroConfigs.isEmpty()) {
            Toast.makeText(this, "暂无历史记录", Toast.LENGTH_SHORT).show();
        }

        // 通知适配器数据已更改
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // 删除选中的配置
    private void deleteConfig(int position) {
        if (position >= 0 && position < pomodoroConfigs.size()) {
            // 获取要删除的配置
            PomodoroConfig configToDelete = pomodoroConfigs.get(position);

            // 从列表中移除
            pomodoroConfigs.remove(position);

            // 重新保存所有配置（不包括已删除的）
            storage.clearAllConfigs();
            for (PomodoroConfig config : pomodoroConfigs) {
                storage.saveConfig(config);
            }

            // 清除该配置的状态
            PomodoroStateStorage stateStorage = new PomodoroStateStorage(this, configToDelete.getPomodoroId());
            stateStorage.clearState();

            // 更新列表显示
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "已删除配置: " + configToDelete.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    // 返回按钮点击事件
    public void onBackButtonClick(View view) {
        finish();
    }
}
