package com.example.smartexpensetracker.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple pie chart view (no external libs).
 * Call setData(map) to give category -> amount values, then invalidate().
 */
public class PieChartView extends View {

    private List<String> labels = new ArrayList<>();
    private List<Float> values = new ArrayList<>();
    private Paint slicePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF pieRect = new RectF();
    private String centerText = "Spending";
    private int[] defaultColors;

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint.setTextSize(dpToPx(12));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFF333333);

        // generate a default color palette (HSV hues)
        defaultColors = new int[12];
        for (int i = 0; i < defaultColors.length; i++) {
            float hue = (i * 360f / defaultColors.length);
            defaultColors[i] = android.graphics.Color.HSVToColor(new float[]{hue, 0.65f, 0.9f});
        }
    }

    /**
     * Provide data as a map category -> amount (amounts should be >= 0)
     */
    public void setData(Map<String, Double> data) {
        labels.clear();
        values.clear();

        if (data == null || data.isEmpty()) {
            invalidate();
            return;
        }

        for (Map.Entry<String, Double> e : data.entrySet()) {
            labels.add(e.getKey());
            values.add(e.getValue().floatValue());
        }
        invalidate();
    }

    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (values == null || values.isEmpty()) {
            // draw placeholder
            textPaint.setTextSize(dpToPx(14));
            canvas.drawText("No data", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float padding = dpToPx(12);
        float left = padding;
        float top = padding;
        float right = getWidth() - padding;
        float bottom = getHeight() - padding - dpToPx(60); // leave space below for legend if needed

        pieRect.set(left, top, right, bottom);

        float total = 0f;
        for (Float v : values) total += v;

        if (total <= 0f) {
            canvas.drawText("No positive values", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float startAngle = -90f; // start at top

        for (int i = 0; i < values.size(); i++) {
            float value = values.get(i);
            float sweep = (value / total) * 360f;

            slicePaint.setColor(defaultColors[i % defaultColors.length]);
            canvas.drawArc(pieRect, startAngle, sweep, true, slicePaint);

            startAngle += sweep;
        }

        // draw center text
        textPaint.setColor(0xFF222222);
        textPaint.setTextSize(dpToPx(16));
        canvas.drawText(centerText, pieRect.centerX(), pieRect.centerY(), textPaint);

        // draw legend below (label + amount)
        float legendX = left;
        float legendY = bottom + dpToPx(20);
        textPaint.setTextSize(dpToPx(12));
        for (int i = 0; i < labels.size(); i++) {
            // small color square
            slicePaint.setColor(defaultColors[i % defaultColors.length]);
            float boxSize = dpToPx(10);
            canvas.drawRect(legendX, legendY - boxSize, legendX + boxSize, legendY, slicePaint);

            // label text
            String lab = labels.get(i);
            String amt = String.format("  %.2f", values.get(i));
            canvas.drawText(lab + amt, legendX + boxSize + dpToPx(8), legendY, textPaint);

            // move down
            legendY += dpToPx(18);
            // if legend goes out of view we simply keep going (or you can clip)
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
