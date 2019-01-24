/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.customfancontroller;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Custom view renders a multi-position "dial". Each click advances to the
 * next dial position. Initially set to 4 selections (0-3):
 * 0 = Off, 1 = Low, 2 = Medium, 3 = High.
 */

public class DialView extends View {

    private static int SELECTION_COUNT = 12;  //4;  // Total number of selections.

    double mRadian = (2 * Math.PI) / SELECTION_COUNT;  // 1/SELECTION_COUNT

    private float mWidth;                    // Custom view width.
    private float mHeight;                   // Custom view height.
    private Paint mTextPaint;                // For text in the view.
    private Paint mDialPaint;                // For dial circle in the view.

    private Paint mAnimPaint;

    private float mRadius;                   // Radius of the dial.
    private int mActiveSelection;            // The active selection.

    private int mLastSelection;              // The last selection.

    private double animAngle;
    float startAngle;
    float sweepAngle;

    // String buffer for dial labels and float for ComputeXY result.
    private final StringBuffer mTempLabel = new StringBuffer(8);
    private final float[] mTempResult = new float[2];

    /**
     * Standard constructor.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     */
    public DialView(Context context) {
        super(context);
        init();
    }

    /**
     * This constructor is called when a view is built from an XML file,
     * supplying attributes that were specified in the XML file.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public DialView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * This constructor is called to supply the default style.
     *
     * @param context      The Context the view is running in, through which
     *                     it can access the current theme, resources, etc.
     * @param attrs        The attributes of the XML tag inflating the view.
     * @param defStyleAttr The default style attributes.
     */
    public DialView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Helper method to initialize instance variables. Called by constructors.
     */
    private void init() {
        // Paint styles used for rendering are created here. This
        // is a performance optimization, since onDraw() is called
        // for every screen refresh.
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(40f);
        mDialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDialPaint.setColor(Color.GRAY);

        mAnimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mAnimPaint.setColor(Color.RED);
        mAnimPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mAnimPaint.setTextAlign(Paint.Align.CENTER);
        mAnimPaint.setTextSize(40f);

        // Initialize current selection (where the dial's "indicator" is
        // pointing).

        mLastSelection = mActiveSelection = 0;

        Double baseAngle = 9 * mRadian; // Angles are in radians.
        Double oldMarkAngle = baseAngle + (mLastSelection * (2 * (Math.PI / SELECTION_COUNT)));
        Double newMarkAngle = baseAngle + (mActiveSelection * (2 * (Math.PI / SELECTION_COUNT)));

        //float startAngle = (mLastSelection * (360 / SELECTION_COUNT));
        //float endAngle = (mActiveSelection * (360 / SELECTION_COUNT));

        animAngle = newMarkAngle;
        startAngle = (9 * (360 / SELECTION_COUNT));
        sweepAngle = 0;

        // Set up onClick listener for this view.
        // Rotates between each of the different selection
        // states on each click.
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Rotate selection forward to the next valid choice.
                mActiveSelection = (mActiveSelection + 1) % SELECTION_COUNT;

                animateArc(1000);

                // Set dial background color to green if selection is >= 1.
                if (mActiveSelection >= 1) {
                    mDialPaint.setColor(Color.GREEN);
                } else {
                    mDialPaint.setColor(Color.GRAY);
                }
                // Redraw the view.
                invalidate();


            }
        });
    }

    //TEST_ML===<
    public void animateArc(long duration){

        final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.setDuration(duration);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                Double baseAngle = 9 * mRadian; // Angles are in radians.
                Double oldMarkAngle = baseAngle + (mLastSelection * (2 * (Math.PI / SELECTION_COUNT)));
                Double newMarkAngle = baseAngle + (mActiveSelection * (2 * (Math.PI / SELECTION_COUNT)));

                Float val = (Float)animation.getAnimatedValue();
                float oneSectionRadius = (float)(2 * (Math.PI / SELECTION_COUNT));

                //Log.w("+++","+++ onAnimationUpdate()., animation.getAnimatedValue(): "+val+", oneSectionRadius:"+oneSectionRadius+", animAngle:"+animAngle);

                if (val >= 1) {
                    mLastSelection = mActiveSelection;
                    animAngle = newMarkAngle;
                    startAngle = startAngle + (360 / SELECTION_COUNT);
                    sweepAngle = 0;
                } else {

                    animAngle = oldMarkAngle + oneSectionRadius * val;
                    sweepAngle = ((360 / SELECTION_COUNT) * val);

                    //Log.d("+++","+++ onAnimationUpdate(),  animAngle:"+animAngle+", oldMarkAngle:"+oldMarkAngle+", newMarkAngle:"+newMarkAngle);

                }
                invalidate();
            }
        });

        valueAnimator.start();
    }
    //==========>

    /**
     * This is called during layout when the size of this view has changed. If
     * the view was just added to the view hierarchy, it is called with the old
     * values of 0. The code determines the drawing bounds for the custom view.
     *
     * @param w    Current width of this view.
     * @param h    Current height of this view.
     * @param oldw Old width of this view.
     * @param oldh Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Calculate the radius from the width and height.
        mWidth = w;
        mHeight = h;
        mRadius = (float) (Math.min(mWidth, mHeight) / 2 * 0.8);
    }

    /**
     * Render view content: an outer grey circle to serve as the "dial",
     * and a smaller black circle to server as the indicator.
     * The position of the indicator is based on mActiveSelection.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the dial.
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mDialPaint);

        // Draw the text labels.
        final float labelRadius = mRadius + 20;
        drawCircle(canvas, mWidth / 2, mHeight / 2, labelRadius);

        int textDelta = 12;
        StringBuffer label = mTempLabel;
        for (int i = 0; i < SELECTION_COUNT; i++) {
            float[] xyData = computeXYForPosition(i, labelRadius);
            float x = xyData[0];
            float y = xyData[1];
            label.setLength(0);
            label.append(i);
            canvas.drawText(label, 0, label.length(), x, y+textDelta, mTextPaint);
            drawCircle(canvas, x, y, 20);
        }

        // Draw the indicator mark
        final float markerRadius = mRadius - 35;
        drawCircle(canvas, mWidth / 2, mHeight / 2, markerRadius);


//        float[] xyData = computeXYForPosition(mActiveSelection, markerRadius);
//        float x = xyData[0];
//        float y = xyData[1];
//        canvas.drawCircle(x, y, 20, mTextPaint);

        //TEST_ML===<

        float[]  xyData = computeXYForAngle(animAngle, markerRadius);
        float x = xyData[0];
        float y = xyData[1];
        canvas.drawCircle(x, y, 20, mAnimPaint);

        //==========>
        RectF rectF = new RectF();
        float l1 = mWidth / 2 - (labelRadius);
        float t1 = mHeight / 2 - (labelRadius);
        //rectF.set(strokeWidth, strokeWidth,getWidth() - strokeWidth  ,getWidth() - strokeWidth);
        rectF.set(l1, t1,mWidth / 2 + (labelRadius)  ,mHeight / 2 + (labelRadius));

        drawArc(canvas, startAngle, sweepAngle, rectF, Color.GRAY);

        float l = mWidth / 2 - (mRadius - 35);
        float t = mHeight / 2 - (mRadius - 35);

        RectF rectF2 = new RectF();
        //rectF2.set(l, t, mWidth/2 + (mRadius - 35)  ,mWidth/2 + (mRadius - 35));
        rectF2.set(l, t, mWidth/2 + (mRadius - 35)  ,mHeight/2 + (mRadius - 35));
        float[] lines3 = {rectF2.left, rectF2.top, rectF2.right, rectF2.top,
                rectF2.right, rectF2.top, rectF2.right, rectF2.bottom,
                rectF2.right, rectF2.bottom, rectF2.left, rectF2.bottom,
                rectF2.left, rectF2.bottom, rectF2.left, rectF2.top
        };
        canvas.drawLines(lines3, mTextPaint);

        drawArc(canvas, startAngle, sweepAngle, rectF2, Color.BLUE);
        //canvas.drawArc(rectF, 0, currentAngle, false, paint);

        float[] lines = {rectF.left, rectF.top, rectF.right, rectF.top,
                         rectF.right, rectF.top, rectF.right, rectF.bottom,
                         rectF.right, rectF.bottom, rectF.left, rectF.bottom,
                         rectF.left, rectF.bottom, rectF.left, rectF.top
                        };
        canvas.drawLines(lines, mTextPaint);


        rectF.set(0,0, getWidth(), getWidth());
        float[] lines2 = {rectF.left, rectF.top, rectF.right, rectF.top,
                rectF.right, rectF.top, rectF.right, rectF.bottom,
                rectF.right, rectF.bottom, rectF.left, rectF.bottom,
                rectF.left, rectF.bottom, rectF.left, rectF.top
        };
        canvas.drawLines(lines2, mTextPaint);

//        ///
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        canvas.drawLine(0, 0, 100, 100, paint);
//        canvas.drawLine(100, 100, 200, 20, paint);
//        ///
    }

    //TEST_ML===<
    private int strokeWidth = 15;
    private void drawArc(Canvas canvas, float startAngle, float sweepAngle, RectF rectF, int color) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        canvas.drawArc(rectF, startAngle, sweepAngle,false,paint);
    }

    private void drawCircle(Canvas canvas, float cx, float cy, float radius) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(1);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(cx, cy, radius, paint);
    }
    //==========>

    /**
     * Compute the X/Y-coordinates for a label or indicator,
     * given the position number and radius
     * where the label should be drawn.
     *
     * @param pos Zero based position index
     * @param radius Radius where label/indicator is to be drawn.
     * @return 2-element array. Element 0 is X-coordinate, element 1 is Y-coordinate.
     */
    private float[] computeXYForPosition_old(final int pos, final float radius) {
        float[] result = mTempResult;
        Double startAngle = Math.PI * (9 / 8d);   // Angles are in radians.
        Double angle = startAngle + (pos * (Math.PI / 4));
        result[0] = (float) (radius * Math.cos(angle)) + (mWidth / 2);
        result[1] = (float) (radius * Math.sin(angle)) + (mHeight / 2);
        return result;
    }

    private float[] computeXYForPosition(final int pos, final float radius) {
        float[] result = mTempResult;
        Double startAngle = 9 * mRadian; // Angles are in radians.
        Double angle = startAngle + (pos * (2 * (Math.PI / SELECTION_COUNT)));
        result[0] = (float) (radius * Math.cos(angle)) + (mWidth / 2);
        result[1] = (float) (radius * Math.sin(angle)) + (mHeight / 2);
        return result;
    }

    ///
    private float[] computeXYForAngle(final Double angle, final float radius) {
        float[] result = mTempResult;
        result[0] = (float) (radius * Math.cos(angle)) + (mWidth / 2);
        result[1] = (float) (radius * Math.sin(angle)) + (mHeight / 2);
        return result;
    }
    ///
}
