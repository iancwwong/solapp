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

import java.text.DecimalFormat;
import java.util.ArrayList;

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

    public TextView feedbackText;
    public TextView subFeedbackText;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // set the text in the circle
        TextView target = null;

        //Format the numbers 2dp
        DecimalFormat formatter = new DecimalFormat("#.##");

        //For animating the circle
        float fraction = (((float) CurVal) / ((float) MaxVal));
        if (mode == 1) {
            //Determine the colour
            if (main.currentUVValue < 3) {
                this.setColor(Color.rgb(0,153,51)); //a shade of green
            } else if (main.currentUVValue < 8) {
                this.setColor(Color.BLUE);
            } else if (main.currentUVValue < 11) {
                this.setColor(Color.rgb(255,153,0)); //orange
            } else if (main.currentUVValue >= 11) {
                this.setColor(Color.RED);
            }

            //Display the measurement
            String centerText = formatter.format(main.currentUVValue);
            WriteText(tx1, centerText);

            //Display the corresponding feedback
            WriteText(feedbackText,getUVFeedback(main.currentUVValue).get(0));
            WriteText(subFeedbackText,getUVFeedback(main.currentUVValue).get(1));

        } else if (mode == 2) {
            //Determine the colour
            if (main.currentExposurePerc < 33) {
                this.setColor(Color.rgb(0,153,51)); //a shade of green
            } else if (main.currentExposurePerc < 80) {
                this.setColor(Color.BLUE);
            } else if (main.currentExposurePerc < 100) {
                this.setColor(Color.rgb(255,153,0)); //orange
            } else if (main.currentExposurePerc >= 100) {
                this.setColor(Color.RED);
            }

            //Display the percentage
            String centerText = formatter.format(main.currentExposurePerc) + "%";
            WriteText(tx2, centerText);

            //Display the corresponding feedback
            WriteText(feedbackText, getExposureFeedback(main.currentExposurePerc).get(0));
            WriteText(subFeedbackText, getExposureFeedback(main.currentExposurePerc).get(1));
        }

        canvas.drawOval(rectF, backgroundPaint);
        float angle = 360 * (fraction);
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint);

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

    //## FEEDBACK FUNCTIONS

    /*
        Return a set of feedback based on a given UV Value:
         - the first feedback is the class
         - the second feedback provides info on protection
         NOTE: Information can be found on the following site:
          https://en.wikipedia.org/wiki/Ultraviolet_index
    */
    public ArrayList<String> getUVFeedback(double uvValue) {
        ArrayList<String> feedback = new ArrayList<String>();

        if (main.currentUVValue < 3) {
            feedback.add("Low");
            feedback.add("You are safe! Keep your style outside with some sunglasses.");
        } else if (main.currentUVValue < 8) {
            feedback.add("Moderate");
            feedback.add("Cover up! Stay in shade near midday.");
        } else if (main.currentUVValue < 11) {
            feedback.add("High");
            feedback.add("Slip-slop-slap! Wear protective clothing, " +
                    "apply SPF 30+ sunscreen, and put on a hat! Reduce time outside around midday.");
        } else if (main.currentUVValue >= 11) {
            feedback.add("Very High");
            feedback.add("Be careful! Apply SPF 30+ sunscreen, wear a long-sleeve shirt and trousers," +
                    "and a wide hat. Avoid sun exposure throughout middday.");
        }

        return feedback;
    }

    /*
        Return a set of feedback based on an accumulated UV exposure level.
        - first feedback is the class
        - second feedback provides info on the accumulated UV exposure

        Currently takes in a specified percentage

        ToDO: Make this function calculate the the percentage by taking in
        OTHER variables such as skin tones, and age
     */
    public ArrayList<String> getExposureFeedback(double accExposure) {
        ArrayList<String> feedback = new ArrayList<String>();

        if (accExposure < 33) {
            feedback.add("Very safe!");
            feedback.add("You can afford to have more UV exposure.");
        } else if (accExposure < 80) {
            feedback.add("Great!");
            feedback.add("You've been exposed to a good amount of UV today.");
        } else if (accExposure < 100) {
            feedback.add("Be careful!");
            feedback.add("Be sure to avoid being in the sun for long periods of time.");
        } else if (accExposure >= 100) {
            feedback.add("Avoid the sun!");
            feedback.add("");
        }
        return feedback;
    }

}
