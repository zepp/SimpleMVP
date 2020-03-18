package com.testapp.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.testapp.R;

public class CircleProgress extends View {
    private static final int pointFactor = 2;
    private static final double ZERO_OFFSET = -Math.PI / 2;
    private final Paint circlePaint = new Paint();
    private final Paint progressPaint = new Paint();
    private int lineWidth;
    private float radius;
    private int progress = 0;
    private int units;

    public CircleProgress(@NonNull Context context) {
        super(context);
        initPaints(null);
    }

    public CircleProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaints(attrs);
    }

    public CircleProgress(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaints(attrs);
    }

    private void initPaints(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.CircleProgress, 0, 0);

        try {
            if (attrs == null) {
                circlePaint.setColor(Color.GRAY);
                progressPaint.setColor(Color.BLACK);
                lineWidth = 4;
                units = 100;
            } else {
                circlePaint.setColor(a.getInt(R.styleable.CircleProgress_color, Color.GRAY));
                progressPaint.setColor(a.getInt(R.styleable.CircleProgress_progressColor, Color.BLACK));
                lineWidth = a.getDimensionPixelSize(R.styleable.CircleProgress_width, 8);
                units = a.getInt(R.styleable.CircleProgress_units, 0);
                progress = a.getInt(R.styleable.CircleProgress_progress, 0);
            }
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(lineWidth);
            circlePaint.setAntiAlias(true);

            progressPaint.setStyle(Paint.Style.FILL);
            progressPaint.setAntiAlias(true);
        } finally {
            a.recycle();
        }
    }

    public void setProgress(int progress) {
        this.progress = progress % units;
        invalidate();
    }

    public void setUnits(int units) {
        this.units = units;
        invalidate();
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        radius = calculateRadius();
        invalidate();
    }

    private float calculateRadius() {
        return (float) Math.min(getWidth() - getPaddingStart() - getPaddingEnd(),
                getHeight() - getPaddingTop() - getPaddingBottom()) / 2 - lineWidth * pointFactor;
    }

    private double getProgressAngle() {
        return ZERO_OFFSET + (2 * Math.PI / units) * progress;
    }

    private Pair<Float, Float> getProgressCoords(double angle) {
        return new Pair<>((float) (Math.cos(angle) * radius + getWidth() / 2),
                (float) (Math.sin(angle) * radius + (float) getHeight() / 2));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        radius = calculateRadius();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Pair<Float, Float> coords = getProgressCoords(getProgressAngle());
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, circlePaint);
        canvas.drawCircle(coords.first, coords.second, lineWidth * pointFactor, progressPaint);
    }
}
