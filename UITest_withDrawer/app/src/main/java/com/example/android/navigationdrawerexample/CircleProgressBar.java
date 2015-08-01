package com.example.android.navigationdrawerexample;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

public class CircleProgressBar extends View {

    private double CurVal;
    private double MinVal = 0;
    private double MaxVal = 10000;

    public MainActivity main;

    /**
     * ProgressBar's line thickness
     */
    private float FrontStrokeWidth = 4;
    private float BackStrokeWidth = 4;
    /**
     * Start the progress at 12 o'clock
     */
    private int startAngle = -90;

    private int FrontColor = Color.BLACK;
    private int BackColor = Color.WHITE;

    private RectF rectF;
    private Paint backgroundPaint;
    private Paint foregroundPaint;

    public float getStrokeWidth() {
        return FrontStrokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.FrontStrokeWidth = strokeWidth;
        backgroundPaint.setStrokeWidth(strokeWidth);
        foregroundPaint.setStrokeWidth(strokeWidth);
        invalidate();
        requestLayout();//Because it should recalculate its bounds
    }

    public double getCurVal() {
        return CurVal;
    }

    public void setCurVal(float progress) {
        this.CurVal = progress;
        invalidate();
    }

    public double getMin() {
        return MinVal;
    }

    public void setMin(double min) {
        this.MinVal = min;
        invalidate();
    }

    public double getMax() {
        return MaxVal;
    }

    public void setMax(double max) {
        this.MaxVal = max;
        invalidate();
    }

    public int getColor() {
        return FrontColor;
    }

    public void setColor(int color) {
        //this.color = color;
        backgroundPaint.setColor(adjustAlpha(color, 0.3f));
        foregroundPaint.setColor(color);

        invalidate();
        requestLayout();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rectF = new RectF();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressBar,
                0, 0);
        //Reading values from the XML layout

        FrontColor = Color.argb(0xff, 0x00, 0x80, 0xff); // blue

        int _BackColor = Color.argb(0xff, 0x00, 0x00, 0x00);
        int _alpha = Math.round(Color.alpha(_BackColor) * 0.05f);
        BackColor  = Color.argb(_alpha, 0x00, 0x00, 0x00); // light grey

        FrontStrokeWidth = 30;
        BackStrokeWidth = 30;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(BackColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(BackStrokeWidth);

        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(FrontColor);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(FrontStrokeWidth);
        foregroundPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    public TextView tx1 = null; // the first one ..
    public TextView tx2 = null; // the second one ..
    public int mode = 0;

    public TextView tvTopLeft;
    public TextView tvTopRight;
    public TextView tvBotLeft;
    public TextView tvBotRight;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Determine percentage of the circle to draw
        float percent = (((float) CurVal) / ((float) MaxVal));
        if (percent >= 1) {
            this.setColor(Color.RED);
        } else {
            this.setColor(Color.BLUE);
        }

        canvas.drawOval(rectF, backgroundPaint);
        float angle = 360 * (percent);
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint);

        // set the text in the circle
        TextView target = null;
        if (mode == 1) {
            WriteText(tx1, String.valueOf(main.currentUVValue));
        } else if (mode == 2) {
            //String centerText = String.valueOf(main.currentExposureLevel) + "\n/" + String.valueOf(MaxVal);
            String centerText = String.valueOf(main.currentExposurePerc) + "%";
            WriteText(tx2, centerText);
        }

        WriteText(tvTopLeft, "Mode: " + String.valueOf(mode));
        WriteText(tvTopRight, "Cur: " + String.valueOf(getCurVal()));
        WriteText(tvBotLeft, "Min: " + String.valueOf(getMin()));
        WriteText(tvBotRight, "Max: " + String.valueOf(getMax()));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        rectF.set(0 + FrontStrokeWidth / 2, 0 + FrontStrokeWidth / 2, min - FrontStrokeWidth / 2, min - FrontStrokeWidth / 2);
    }

    /**
     * Lighten the given color by the factor
     *
     * @param color  The color to lighten
     * @param factor 0 to 4
     * @return A brighter color
     */
    public int lightenColor(int color, float factor) {
        float r = Color.red(color) * factor;
        float g = Color.green(color) * factor;
        float b = Color.blue(color) * factor;
        int ir = Math.min(255, (int) r);
        int ig = Math.min(255, (int) g);
        int ib = Math.min(255, (int) b);
        int ia = Color.alpha(color);
        return (Color.argb(ia, ir, ig, ib));
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    ObjectAnimator objectAnimator = null;

    public void AnimateProgressTo(double CurVal, int Duration) {

        objectAnimator = ObjectAnimator.ofFloat(this, "CurVal", (float) CurVal);
        objectAnimator.setDuration(Duration);
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();

    }

    public void AnimateProgressTo(double CurVal) {
        AnimateProgressTo(CurVal, 1500);
    }

    public void EndAnimation() {
        if (objectAnimator != null) {
            objectAnimator.end();
        }
    }

    public void WriteText(TextView tv, String text) {
        if (tv != null) {
            tv.setText(text);
        }
    }
}
