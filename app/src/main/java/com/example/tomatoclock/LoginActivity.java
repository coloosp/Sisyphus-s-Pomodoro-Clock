package com.example.tomatoclock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.media.MediaPlayer;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private SharedPreferences prefs;

    // 用户数据存储相关常量
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MediaPlayer.create(this, R.raw.close2);
        try {
            setContentView(R.layout.activity_login);

            // 初始化视图
            etUsername = findViewById(R.id.et_username);
            etPassword = findViewById(R.id.et_password);
            btnLogin = findViewById(R.id.btn_login);
            btnRegister = findViewById(R.id.btn_register);

            // 获取SharedPreferences实例
            prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

            // 设置登录按钮点击事件
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String username = etUsername.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        // 验证输入
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                            Toast.makeText(LoginActivity.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 检查用户是否存在
                        if (checkUserExists(username)) {
                            // 检查密码是否正确
                            if (checkPassword(username, password)) {
                                // 登录成功
                                saveLoginState(username, password);
                                Intent intent = new Intent();
                                intent.putExtra("username", username);
                                setResult(RESULT_OK, intent);
                                finish();
                            } else {
                                // 密码错误
                                Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // 用户不存在
                            Toast.makeText(LoginActivity.this, "用户不存在，请先注册", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "登录按钮点击异常: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "登录过程发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // 设置注册按钮点击事件
            btnRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mediaPlayer.start();
                    try {
                        String username = etUsername.getText().toString().trim();
                        String password = etPassword.getText().toString().trim();

                        // 验证输入
                        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                            Toast.makeText(LoginActivity.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 检查用户是否已存在
                        if (checkUserExists(username)) {
                            Toast.makeText(LoginActivity.this, "用户名已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 注册新用户
                        registerUser(username, password);
                        Toast.makeText(LoginActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "注册按钮点击异常: " + e.getMessage(), e);
                        Toast.makeText(LoginActivity.this, "注册过程发生错误: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "onCreate异常: " + e.getMessage(), e);
            Toast.makeText(this, "登录界面初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish(); // 关闭当前Activity
        }
    }

    // 检查用户是否存在
    private boolean checkUserExists(String username) {
        String savedUsername = prefs.getString(KEY_USERNAME, "");
        return !TextUtils.isEmpty(savedUsername) && savedUsername.equals(username);
    }

    // 检查密码是否正确
    private boolean checkPassword(String username, String password) {
        String savedPassword = prefs.getString(KEY_PASSWORD, "");
        return savedPassword.equals(password);
    }

    // 注册新用户
    private void registerUser(String username, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    // 保存登录状态
    private void saveLoginState(String username, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
}