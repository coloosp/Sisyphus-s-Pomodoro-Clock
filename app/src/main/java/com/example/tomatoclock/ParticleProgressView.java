package com.example.tomatoclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleProgressView extends View {
    private List<Particle> particles = new ArrayList<>();
    private int progress = 0;
    private Paint particlePaint, progressPaint, bgPaint;
    private int particleColor, progressColor, backgroundColor;
    private int particleCount = 200;
    private float speedMultiplier = 1f;
    private boolean isAttached = false;

    public ParticleProgressView(Context context) {
        super(context);
        init(null);
    }

    public ParticleProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        // 初始化画笔（必须放在最前面）
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ParticleProgressView);
            try {
                particleColor = ta.getColor(R.styleable.ParticleProgressView_particleColor, Color.WHITE);
                progressColor = ta.getColor(R.styleable.ParticleProgressView_progressColor, Color.parseColor("#4CAF50"));
                backgroundColor = ta.getColor(R.styleable.ParticleProgressView_backgroundColor, Color.parseColor("#303030"));
                particleCount = ta.getInteger(R.styleable.ParticleProgressView_particleCount, 200);
            } finally {
                ta.recycle();
            }
        }

        particlePaint.setColor(particleColor);
        progressPaint.setColor(progressColor);
        bgPaint.setColor(backgroundColor);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
        initParticles();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
        particles.clear();
    }

    private void initParticles() {
        if (getWidth() <= 0 || getHeight() <= 0) {
            post(this::initParticles);
            return;
        }

        particles.clear();
        for (int i = 0; i < particleCount; i++) {
            particles.add(new Particle());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null || getWidth() <= 0 || getHeight() <= 0) return;

        int width = getWidth();
        int height = getHeight();
        int progressWidth = (int) (width * (progress / 100f));

        // 绘制背景
        canvas.drawRect(0, 0, width, height, bgPaint);

        // 绘制进度
        canvas.save();
        canvas.clipRect(0, 0, progressWidth, height);
        canvas.drawRect(0, 0, width, height, progressPaint);

        // 绘制粒子
        for (Particle p : particles) {
            if (p.x < progressWidth) {
                particlePaint.setAlpha(p.alpha);
                canvas.drawCircle(p.x, p.y, p.radius, particlePaint);
            }
            p.update();
        }
        canvas.restore();

        // 触发重绘
        if (progress > 0 && progress < 100 && isAttached) {
            postInvalidateOnAnimation();
        }
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(progress, 100));
        if (isAttached) invalidate();
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = Math.max(0.1f, Math.min(multiplier, 5f));
    }

    public void setProgressColor(int color) {
        progressPaint.setColor(color);
        if (isAttached) invalidate();
    }

    public void setParticleColor(int color) {
        particlePaint.setColor(color);
        if (isAttached) invalidate();
    }

    public void setBackgroundColor(int color) {
        bgPaint.setColor(color);
        if (isAttached) invalidate();
    }

    private class Particle {
        float x, y, radius, speed;
        int alpha;
        Random random = new Random();

        Particle() {
            reset();
        }

        void reset() {
            x = random.nextInt(getWidth());
            y = random.nextInt(getHeight());
            radius = 1 + random.nextFloat() * 3;
            speed = 0.5f + random.nextFloat() * 3 * speedMultiplier;
            alpha = 100 + random.nextInt(155);
        }

        void update() {
            x -= speed;
            if (x < 0) {
                reset();
                x = getWidth();
            }
        }
    }
}