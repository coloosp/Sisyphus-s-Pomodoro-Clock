package com.example.tomatoclock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class EggActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egg);

        // 查找并设置番茄图片按钮点击事件
        ImageButton tomatoButton = findViewById(R.id.tomatoImageButton);

    }
    public void onTomatoClick(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/coloosp?tab=repositories"));
        startActivity(intent);
    }
}
