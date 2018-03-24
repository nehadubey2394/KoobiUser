/*
 * Copyright (c) 2017.  Joe
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package views.refreshview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;

import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.mualab.org.user.R;

import java.util.ArrayList;


/**
 * Created by Joe on 2016/4/3.
 * Email lovejjfg@gmail.com
 */
@SuppressWarnings("unused")
public class TouchCircleView extends View {

    private static String TAG = "HeaderRefresh";
    float firstRange;//draw arc
    float secRange;//draw arrow
    float thirdRange;//dra second arc no use
    private RectF outRectF;
    private RectF innerRectf;
    private RectF secondRectf;
    private Paint innerPaint;
    private Paint paint;
    private Paint mCurrentPaint;
    private Paint mixPaint;


    private Path path;
    private Path mArrow;
    private Path mHook;
    private Path mError;

    private ObjectAnimator mObjectAnimatorSweep;
    private ObjectAnimator mObjectAnimatorAngle;
    private ValueAnimator fractionAnimator;
    private ValueAnimator translateAnimator;

    private static final Interpolator ANGLE_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator SWEEP_INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private static final int ANGLE_ANIMATOR_DURATION = 1000;
    private static final int SWEEP_ANIMATOR_DURATION = 800;
    private static final int FRACTION_DURATION = 300;
    private static final int DELAY_TIME = 2000;
    private static final int START_ANGLE = 270;
    private static final int RESULT_TIME = 300;
    private static final int ALPHA_FULL = 255;
    private static final int MIN_SWEEP_ANGLE = 30;


    private float mCurrentGlobalAngleOffset;
    private float mCurrentGlobalAngle;
    private float mCurrentSweepAngle;

    public static final int STATE_DRAW_IDLE = 0;
    public static final int STATE_DRAW_ARC = 1;
    public static final int STATE_DRAW_PATH = 2;
    public static final int STATE_DRAW_OUT_PATH = 3;
    public static final int STATE_DRAW_CIRCLE = 4;
    public static final int STATE_DRAW_ARROW = 5;
    public static final int STATE_DRAW_PROGRESS = 6;
    public static final int STATE_DRAW_ERROR = 7;
    public static final int STATE_DRAW_SUCCESS = 8;
    public static final int STATE_DRAW_BACK = 9;
    private static final int STATE_TRANSLATE_PATH = 10;

    private CirclePoint p0;
    private CirclePoint p1;
    private CirclePoint p2;
    private CirclePoint p3;
    private CirclePoint p4;
    private CirclePoint p5;
    private CirclePoint p6;
    private CirclePoint p7;
    private CirclePoint p8;
    private CirclePoint p9;
    private CirclePoint p10;
    private CirclePoint p11;

    private float currentOffset;
    private int defaultOffset;
    private final Runnable finishAction = new Runnable() {
        @Override
        public void run() {
            finish();
        }
    };
    private Runnable startLoadingAction = new Runnable() {
        @Override
        public void run() {
            updateRectF();
            startLoading();
        }
    };
    private float pathMax;
    private Paint secPaint;
    private int mCurrentRadius;
    private RectF mCurrentRectf = new RectF();
    private boolean isBack;
    private float changeDy;
    private boolean abortReset;

    public int getCurrentState() {
        return currentState;
    }

    public void setCurrentState(int currentState) {
        this.currentState = currentState;
        invalidate();
    }

    private int currentState;
    private int centerY;
    private int centerX;
    private float mBorderWidth = 4;
    private float mRingCenterRadius;
    private boolean mModeAppearing;
    private float mArrowScale = 1.0f;
    private float fraction;
    private Paint mHookPaint;

    public boolean ismRunning() {
        return mRunning;
    }

    private boolean mRunning;
    private boolean isDrawTriangle;

    //设置默认半径
    @SuppressWarnings("unused")
    public void setOutCirRadius(int outCirRadius) {
        this.outCirRadius = outCirRadius;
        //Log.i("默认半径：", outCirRadius + "");

    }

    @SuppressWarnings("unused")
    public int getOutCirRadius() {
        return outCirRadius;
    }
    //外圈半径
    private int outCirRadius;
    //not used in the project
    private int secondRadius = (int) (outCirRadius * 1f);
    //
    private int innerCirRadius = outCirRadius - 30;
    private static int ARROW_WIDTH = 20 * 2;
    private static int ARROW_HEIGHT = 10 * 2;


    private long angle;
    private int paths;
    private long backpaths;

    public TouchCircleView(Context context) {
        this(context, null);
    }

    public TouchCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HeaderProgress, defStyleAttr, 0);
        mBorderWidth = a.getDimension(R.styleable.HeaderProgress_circleBorderWidth,
                2 * density);
        outCirRadius = (int) a.getDimension(R.styleable.HeaderProgress_outRadius, 18 * density);
        innerCirRadius = (int) a.getDimension(R.styleable.HeaderProgress_innerRadius, 10 * density);
        secondRadius = (int) (outCirRadius * 1.5f);
//        innerCirRadius = (int) (outCirRadius * 0.6f);
        a.recycle();
        initView();
    }


    private void initView() {
        setupAnimations();
        innerRectf = new RectF();
        secondRectf = new RectF();
        outRectF = new RectF();
        ARROW_WIDTH = (int) (mBorderWidth * 1.5f);
        ARROW_HEIGHT = (int) (mBorderWidth * 0.75f);
        initPaintPath();
    }

    /**
     * 绘制对应的圆形
     */
    private static final float CIRCLE_VALUE = 0.551915024494f;

    private void initPoints() {
        float m = mCurrentRadius * CIRCLE_VALUE;
        float centerX = mCurrentRectf.centerX();
        float centerY = mCurrentRectf.centerY();

        p0 = new CirclePoint(centerX, centerY - mCurrentRadius);
        p1 = new CirclePoint(centerX + m, centerY - mCurrentRadius);
        p2 = new CirclePoint(centerX + mCurrentRadius, centerY - m);
        p3 = new CirclePoint(centerX + mCurrentRadius, centerY);

        p4 = new CirclePoint(centerX + mCurrentRadius, centerY + m);
        p5 = new CirclePoint(centerX + m, centerY + mCurrentRadius);
        p6 = new CirclePoint(centerX, centerY + mCurrentRadius);

        p7 = new CirclePoint(centerX - m, centerY + mCurrentRadius);
        p8 = new CirclePoint(centerX - mCurrentRadius, centerY + m);
        p9 = new CirclePoint(centerX - mCurrentRadius, centerY);

        p10 = new CirclePoint(centerX - mCurrentRadius, centerY - m);
        p11 = new CirclePoint(centerX - m, centerY - mCurrentRadius);
    }

    private void resetPoints() {
        float centerX = mCurrentRectf.centerX();
        float centerY = mCurrentRectf.centerY();
        resetPoints(centerX, centerY);
    }

    private void resetPoints(float centerX, float centerY) {
        float m = mCurrentRadius * CIRCLE_VALUE;

        p0.setPoint(centerX, centerY - mCurrentRadius);
        p1.setPoint(centerX + m, centerY - mCurrentRadius);
        p2.setPoint(centerX + mCurrentRadius, centerY - m);
        p3.setPoint(centerX + mCurrentRadius, centerY);

        p4.setPoint(centerX + mCurrentRadius, centerY + m);
        p5.setPoint(centerX + m, centerY + mCurrentRadius);
        p6.setPoint(centerX, centerY + mCurrentRadius);

        p7.setPoint(centerX - m, centerY + mCurrentRadius);
        p8.setPoint(centerX - mCurrentRadius, centerY + m);
        p9.setPoint(centerX - mCurrentRadius, centerY);

        p10.setPoint(centerX - mCurrentRadius, centerY - m);
        p11.setPoint(centerX - m, centerY - mCurrentRadius);
    }

    public void handleOffset(int dy) {
        float percent;
        if (dy < 0) {
            dy = 0;
        }
        mModeAppearing = true;
        percent = dy / firstRange;
        if (dy != angle && dy >= 0 && dy <= firstRange) {
            paint.setAlpha((int) (percent * ALPHA_FULL));
            updateState(STATE_DRAW_ARC, false);
            resetAngle();
           // Log.e(TAG, "handleOffset: height::" + getHeight());
            currentOffset = getHeight() - percent * defaultOffset;
            updateRectF();
            angle = (long) (percent * 360);
          //  Log.i(TAG, "onTouchEvent: " + angle);
            invalidate();
            return;
        }
        if (dy > firstRange && dy <= secRange) {
            innerPaint.setAlpha(ALPHA_FULL);
            outRectF.set(centerX - outCirRadius, currentOffset, centerX + outCirRadius
                    , centerY + outCirRadius + currentOffset);
            updateState(STATE_DRAW_ARROW, false);
            percent = (dy - firstRange) / (secRange - firstRange);
            mCurrentSweepAngle = mCurrentGlobalAngle = percent * 200;
            invalidate();
        }


    }

    private void updateState(int state, boolean hide) {
        updateState(state, hide, false);

    }

    private void updateState(final int state, final boolean hide, boolean delay) {
        currentState = state;
        if (listeners != null) {
            for (final OnLoadingListener listener : listeners) {
                if (STATE_DRAW_BACK == state) {
                    listener.onGoBackHome();
                }
                if (delay) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listener.onProgressStateChange(state, hide);
                        }
                    }, RESULT_TIME);
                } else {
                    listener.onProgressStateChange(state, hide);
                }
            }
        }
    }

    public void resetTouch() {
        if (mRunning || translateAnimator.isRunning()) {
            abortReset = true;
            return;
        }
        abortReset = false;
        if (STATE_DRAW_ARROW == currentState || STATE_DRAW_PATH == currentState || STATE_DRAW_OUT_PATH == currentState) {
            outRectF.set(centerX - outCirRadius, currentOffset, centerX + outCirRadius
                    , centerY + outCirRadius + currentOffset);
            innerPaint.setAlpha(ALPHA_FULL);
            updateState(STATE_DRAW_PROGRESS, false);
            start();
            return;
        }
        if (STATE_DRAW_CIRCLE == currentState) {
            updateState(STATE_DRAW_BACK, true);
        }
        stop();
        currentOffset = 0;
        updateState(STATE_DRAW_IDLE, true);
        invalidate();

    }

    private void startLoading() {
        innerPaint.setAlpha(ALPHA_FULL);
        start();
    }

    private void initPaintPath() {

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        //paint.setColor(Color.RED);
        paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        paint.setStrokeCap(Paint.Cap.SQUARE);

        secPaint = new Paint(paint);
        secPaint.setColor(Color.GREEN);

        mixPaint = new Paint(paint);
        innerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerPaint.setStyle(Paint.Style.STROKE);
        innerPaint.setAntiAlias(true);
        innerPaint.setColor(Color.WHITE);
        innerPaint.setStrokeWidth(mBorderWidth);


        mHookPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHookPaint.setStyle(Paint.Style.STROKE);
        mHookPaint.setStrokeCap(Paint.Cap.ROUND);
        mHookPaint.setStrokeWidth(mBorderWidth);
        mHookPaint.setColor(Color.WHITE);

        path = new Path();
        mArrow = new Path();
        mHook = new Path();
        mError = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = outCirRadius;

        updateRectF();
        initPoints();
        mRingCenterRadius = Math.min(innerRectf.centerX() - innerRectf.left, innerRectf.centerY() - innerRectf.top) - mBorderWidth;

    }

    private void updateRectF() {
        outRectF.set(centerX - outCirRadius, currentOffset, centerX + outCirRadius
                , centerY + outCirRadius + currentOffset);
        innerRectf.set(centerX - innerCirRadius, centerY - innerCirRadius + currentOffset, centerX + innerCirRadius, centerY + innerCirRadius + currentOffset);
        secondRectf.set(centerX - secondRadius, currentOffset + secondRadius * 2, centerX + secondRadius
                , centerY + secondRadius + currentOffset + secondRadius * 2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        defaultOffset = (int) (getHeight() *0.4f);
        firstRange = (int) (getHeight() * 0.6f);
        secRange = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //动画的
        switch (currentState) {
            case STATE_DRAW_IDLE:
                canvas.drawArc(outRectF, 0, 0, false, paint);
                break;
            case STATE_DRAW_ARC:
                canvas.drawArc(outRectF, START_ANGLE, angle, true, paint);
                break;
            case STATE_DRAW_ARROW:
                isDrawTriangle = true;
                mCurrentRectf = outRectF;
                mCurrentRadius = outCirRadius;
                mCurrentPaint = paint;
                resetPoints();
                drawCirclePath(canvas);
                drawArc(canvas);
                break;
            case STATE_DRAW_PROGRESS:
                isDrawTriangle = false;
                mCurrentRectf = outRectF;
                mCurrentPaint = paint;
                mCurrentRadius = outCirRadius;
                resetPoints();
                drawCirclePath(canvas);
                drawArc(canvas);
                break;
            case STATE_DRAW_SUCCESS:
                drawHook(canvas);
                break;
            case STATE_DRAW_ERROR:
                drawError(canvas);
                break;
            case STATE_DRAW_PATH:
                mCurrentRadius = outCirRadius;
                mCurrentRectf = outRectF;
                mCurrentPaint = paint;
                resetPoints();
                drawCirclePath(canvas, paths);
                break;
            case STATE_TRANSLATE_PATH:
                drawCirclePath(canvas);
                break;
            case STATE_DRAW_OUT_PATH:
                mCurrentRadius = secondRadius;
                mCurrentRectf = secondRectf;
                mCurrentPaint = secPaint;
                resetPoints();
                drawCirclePath(canvas, backpaths);
                break;
            case STATE_DRAW_CIRCLE:
                mCurrentRadius = secondRadius;
                mCurrentRectf = secondRectf;
                mCurrentPaint = secPaint;
                resetPoints();
                drawCirclePath(canvas);
                break;
        }
    }


    private void drawCirclePath(Canvas canvas) {
        drawCirclePath(canvas, 0);
    }

    private void drawCirclePath(Canvas canvas, float distance) {
        boolean isDrawPath = distance != 0;
        if (isDrawPath) {
            if (distance > 0) {
                p0.y -= changeDy;
                p11.y = p1.y = p0.y;//竖直方向变大
                //上边 +
                p5.y += distance;
                p7.y = p6.y = p5.y;
                //左边 +
                p8.x += changeDy / 2;
                p10.x = p9.x = p8.x;

                //右边 -
                p2.x -= changeDy / 2;
                p3.x = p4.x = p2.x;
            } else {
                //下边
                p0.y += distance;
                p11.y = p1.y = p0.y;//竖直方向变大
                //上边 +
                p5.y += changeDy;
                p7.y = p6.y = p5.y;
                //左边 +
                p8.x -= changeDy / 2;
                p10.x = p9.x = p8.x;

                //右边 -
                p2.x += changeDy / 2;
                p3.x = p4.x = p2.x;
            }
        }
        path.reset();
        path.moveTo(p0.x, p0.y);
        path.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);
        path.cubicTo(p4.x, p4.y, p5.x, p5.y, p6.x, p6.y);
        path.cubicTo(p7.x, p7.y, p8.x, p8.y, p9.x, p9.y);
        path.cubicTo(p10.x, p10.y, p11.x, p11.y, p0.x, p0.y);
        canvas.drawPath(path, mCurrentPaint);
    }


    private void drawArc(Canvas canvas) {
        float startAngle = mCurrentGlobalAngle - mCurrentGlobalAngleOffset + START_ANGLE;
        float sweepAngle = mCurrentSweepAngle;
        if (mModeAppearing) {
//            paint.setColor(gradient(mColors[mCurrentColorIndex], mColors[mNextColorIndex],
//                    mCurrentSweepAngle / (360 - MIN_SWEEP_ANGLE * 2)));
            sweepAngle += MIN_SWEEP_ANGLE;
        } else {
            startAngle = startAngle + sweepAngle;
            sweepAngle = 360 - sweepAngle - MIN_SWEEP_ANGLE;
        }
        canvas.drawArc(innerRectf, startAngle, sweepAngle, false, innerPaint);
        if (isDrawTriangle) {
            drawTriangle(canvas, startAngle, sweepAngle);
        }
    }

    public void drawTriangle(Canvas c, float startAngle, float sweepAngle) {
        if (mArrow == null) {
            mArrow = new Path();
            mArrow.setFillType(Path.FillType.EVEN_ODD);
        } else {
            mArrow.reset();
        }

        // Adjust the position of the triangle so that it is inset as
        // much as the arc, but also centered on the arc.
//        float inset = (int) mStrokeInset / 2 * mArrowScale;
        float x = (float) (mRingCenterRadius * Math.cos(0) + innerRectf.centerX());
        float y = (float) (mRingCenterRadius * Math.sin(0) + innerRectf.centerY());

        // Update the path each time. This works around an issue in SKIA
        // where concatenating a rotation matrix to a scale matrix
        // ignored a starting negative rotation. This appears to have
        // been fixed as of API 21.
        mArrow.moveTo(0, 0);
        mArrow.lineTo(ARROW_WIDTH * mArrowScale, 0);
        mArrow.lineTo((ARROW_WIDTH * mArrowScale / 2), (ARROW_HEIGHT
                * mArrowScale));
        mArrow.offset(x, y);
        mArrow.close();
        // draw a triangle
        c.rotate(startAngle + sweepAngle, innerRectf.centerX(),
                innerRectf.centerY());
        c.drawPath(mArrow, innerPaint);

    }

    /**
     * 画勾
     */
    private void drawHook(Canvas canvas) {
        mHook.reset();
        mHook.moveTo(innerRectf.centerX() - innerRectf.width() * 0.25f * fraction, innerRectf.centerY());
        mHook.lineTo(innerRectf.centerX() - innerRectf.width() * 0.1f * fraction, innerRectf.centerY() + innerRectf.height() * 0.18f * fraction);
        mHook.lineTo(innerRectf.centerX() + innerRectf.width() * 0.25f * fraction, innerRectf.centerY() - innerRectf.height() * 0.20f * fraction);
        mCurrentRectf = outRectF;
        mCurrentPaint = paint;
        resetPoints();
        drawCirclePath(canvas);
        canvas.drawPath(mHook, mHookPaint);

    }

    /**
     * 画×
     */
    private void drawError(Canvas canvas) {
        mError.reset();
        mError.moveTo(innerRectf.centerX() + innerRectf.width() * 0.2f * fraction, innerRectf.centerY() - innerRectf.height() * 0.2f * fraction);
        mError.lineTo(innerRectf.centerX() - innerRectf.width() * 0.2f * fraction, innerRectf.centerY() + innerRectf.height() * 0.2f * fraction);
        mError.moveTo(innerRectf.centerX() - innerRectf.width() * 0.2f * fraction, innerRectf.centerY() - innerRectf.height() * 0.2f * fraction);
        mError.lineTo(innerRectf.centerX() + innerRectf.width() * 0.2f * fraction, innerRectf.centerY() + innerRectf.height() * 0.2f * fraction);
        mCurrentRectf = outRectF;
        mCurrentPaint = paint;
        resetPoints();
        drawCirclePath(canvas);
        canvas.drawPath(mError, mHookPaint);
    }


    private void start() {
        if (mRunning) {
            return;
        }
        mRunning = true;
        dispatchListener();
        mObjectAnimatorAngle.setFloatValues(mCurrentGlobalAngle, 360f);
        mObjectAnimatorSweep.setFloatValues(mCurrentSweepAngle, 360f - MIN_SWEEP_ANGLE * 2);
//        mCurrentState = STATE_LOADING;
        mObjectAnimatorAngle.start();
        mObjectAnimatorSweep.start();
//        postDelayed(finishAction, DELAY_TIME);
        invalidate();
    }

    private void dispatchListener() {
        if (listeners != null) {
            for (OnLoadingListener listener : listeners) {
                listener.onProgressStateChange(STATE_DRAW_PROGRESS, false);
                listener.onProgressLoading();
            }

        }
    }

    public void finish() {
        stop();
        mRunning = true;
        resetAngle();
        if (!fractionAnimator.isRunning()) {
            fractionAnimator.start();
        }
        updateState(currentState, false, false);
        invalidate();
    }

    private void resetAngle() {
        if (mCurrentSweepAngle != 0 || mCurrentGlobalAngle != 0 || mCurrentGlobalAngleOffset != 0) {
            mCurrentSweepAngle = 0;
            mCurrentGlobalAngle = 0;
            mCurrentGlobalAngleOffset = 0;
        }
    }

    private void stop() {
        if (!mRunning) {
            return;
        }
//        mRunning = false;
        mObjectAnimatorAngle.cancel();
        mObjectAnimatorSweep.cancel();
//        mRunning = fractionAnimator.isRunning();
    }

    private void toggleAppearingMode() {
        mModeAppearing = !mModeAppearing;
        if (mModeAppearing) {
//            mCurrentColorIndex = ++mCurrentColorIndex % 4;
//            mNextColorIndex = ++mNextColorIndex % 4;
            mCurrentGlobalAngleOffset = (mCurrentGlobalAngleOffset + MIN_SWEEP_ANGLE * 2) % 360;
        }
    }

    private Property<TouchCircleView, Float> mAngleProperty = new Property<TouchCircleView, Float>(Float.class, "angle") {
        @Override
        public Float get(TouchCircleView object) {
            return object.getCurrentGlobalAngle();
        }

        @Override
        public void set(TouchCircleView object, Float value) {
            object.setCurrentGlobalAngle(value);
        }
    };

    private Property<TouchCircleView, Float> mSweepProperty = new Property<TouchCircleView, Float>(Float.class, "arc") {
        @Override
        public Float get(TouchCircleView object) {
            return object.getCurrentSweepAngle();
        }

        @Override
        public void set(TouchCircleView object, Float value) {
            object.setCurrentSweepAngle(value);
        }
    };

    private Runnable idleAction = new Runnable() {
        @Override
        public void run() {
            mRunning = false;
            updateState(STATE_DRAW_IDLE, true);
            invalidate();
        }
    };

    private void setupAnimations() {

        mObjectAnimatorAngle = ObjectAnimator.ofFloat(this, mAngleProperty, mCurrentGlobalAngle, 360f);
        mObjectAnimatorAngle.setInterpolator(ANGLE_INTERPOLATOR);
        mObjectAnimatorAngle.setDuration(ANGLE_ANIMATOR_DURATION);
        mObjectAnimatorAngle.setRepeatMode(ValueAnimator.RESTART);
        mObjectAnimatorAngle.setRepeatCount(ValueAnimator.INFINITE);
        mObjectAnimatorAngle.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mObjectAnimatorAngle.setFloatValues(360f);
            }
        });

        mObjectAnimatorSweep = ObjectAnimator.ofFloat(this, mSweepProperty, mCurrentSweepAngle, 360f - MIN_SWEEP_ANGLE * 2);
        mObjectAnimatorSweep.setInterpolator(SWEEP_INTERPOLATOR);
        mObjectAnimatorSweep.setDuration(SWEEP_ANIMATOR_DURATION);
        mObjectAnimatorSweep.setRepeatMode(ValueAnimator.RESTART);
        mObjectAnimatorSweep.setRepeatCount(ValueAnimator.INFINITE);
        mObjectAnimatorSweep.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mObjectAnimatorSweep.setFloatValues(360f - MIN_SWEEP_ANGLE * 2);
                toggleAppearingMode();
            }
        });

        fractionAnimator = ValueAnimator.ofInt(0, ALPHA_FULL);
        fractionAnimator.setInterpolator(ANGLE_INTERPOLATOR);
        fractionAnimator.setDuration(FRACTION_DURATION);
        fractionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fraction = animation.getAnimatedFraction();
                mHookPaint.setAlpha((Integer) animation.getAnimatedValue());
                invalidate();
            }
        });
        fractionAnimator.addListener(new AnimatorListenerAdapter() {


            @Override
            public void onAnimationEnd(Animator animation) {
                mRunning = false;
                postDelayed(idleAction, 0);
            }
        });
        translateAnimator = ValueAnimator.ofFloat(0, 100);
        translateAnimator.setInterpolator(ANGLE_INTERPOLATOR);
        translateAnimator.setDuration(200);
        translateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            private float tranlateFraction;

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float animatedValue = (Float) animation.getAnimatedValue();
                updateState(STATE_TRANSLATE_PATH, false);
                tranlateFraction = animation.getAnimatedFraction();
                if (!isBack) {
                    mixPaint.setColor(ColorUtils.blendARGB(Color.RED, Color.GREEN, tranlateFraction));
                    mCurrentRadius = (int) (outCirRadius + tranlateFraction * (secondRadius - outCirRadius));
                } else {
                    mixPaint.setColor(ColorUtils.blendARGB(Color.GREEN, Color.RED, tranlateFraction));
                    mCurrentRadius = (int) (secondRadius - tranlateFraction * (secondRadius - outCirRadius));
                }
                mCurrentPaint = mixPaint;
                resetPoints(secondRectf.centerX(), animatedValue);
                invalidate();
            }
        });
        translateAnimator.addListener(new AnimatorListenerAdapter() {


            @Override
            public void onAnimationEnd(Animator animation) {
                updateState(isBack ? STATE_DRAW_ARROW : abortReset ? STATE_DRAW_BACK : STATE_DRAW_CIRCLE, abortReset);

                updateRectF();
                invalidate();
            }
        });
    }

    public void setCurrentGlobalAngle(float currentGlobalAngle) {
        mCurrentGlobalAngle = currentGlobalAngle;
        invalidate();
    }

    public float getCurrentGlobalAngle() {
        return mCurrentGlobalAngle;
    }

    public void setCurrentSweepAngle(float currentSweepAngle) {
        mCurrentSweepAngle = currentSweepAngle;
        invalidate();
    }

    public float getCurrentSweepAngle() {
        return mCurrentSweepAngle;
    }

    ArrayList<OnLoadingListener> listeners = new ArrayList<>();

    public void addLoadingListener(@Nullable OnLoadingListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public boolean removeLoadingListener(@NonNull OnLoadingListener listener) {
        return listeners != null && listeners.remove(listener);
    }


//    @Nullable
//    OnLoadingListener listener;

    public void setRefresh(boolean mRefresh) {
        currentOffset = getHeight() * 0.5f;
        updateState(mRefresh ? STATE_DRAW_PROGRESS : STATE_DRAW_IDLE, !mRefresh);
        if (mRefresh) {
//            currentOffset = c;
            post(startLoadingAction);
        } else {
            currentOffset = 0;
            mObjectAnimatorSweep.cancel();
            mObjectAnimatorAngle.cancel();
            updateRectF();
            invalidate();
        }

    }

    public void setRefreshError() {
        if (currentState == STATE_DRAW_ERROR) {
            return;
        }
        currentState = STATE_DRAW_ERROR;
        finish();
    }

    public void setRefreshSuccess() {
        //Log.e(TAG, "setRefreshSuccess: ");
        if (currentState == STATE_DRAW_SUCCESS) {
            return;
        }
        currentState = STATE_DRAW_SUCCESS;
        finish();
    }

    public interface OnLoadingListener {
        void onProgressStateChange(int state, boolean hide);

        void onProgressLoading();

        void onGoBackHome();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    private static class CirclePoint {
        float x;
        float y;

        public CirclePoint(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void setPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
