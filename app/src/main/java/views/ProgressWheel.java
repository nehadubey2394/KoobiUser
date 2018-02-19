package views;


import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dharmraj on 13/12/17.
 */

public class ProgressWheel extends View {
    private boolean mAnimationCancelled;
    private int mBarColor = -1442840576;
    private int mBarLength = 60;
    private Paint mBarPaint = new Paint();
    private int mBarWidth = 20;
    private RectF mCircleBounds = new RectF();
    private int mLayoutHeight = 0;
    private int mLayoutWidth = 0;
    private float mProgress;
    private ProgressWheelAnimationListener mProgressWheelAnimationListener;
    private int mRimColor = -1428300323;
    private Paint mRimPaint = new Paint();
    private int mRimWidth = 20;
    private ValueAnimator mValueAnimator;

    public interface ProgressWheelAnimationListener {
        void onAnimationEnd();

        boolean onAnimationRepeat();
    }

    public ProgressWheel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        // parseAttributes(context.obtainStyledAttributes(attributeSet, R.styleable.ProgressWheel));
    }

    protected void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.mLayoutWidth = i;
        this.mLayoutHeight = i2;
        setupBounds();
        setupPaints();
        invalidate();
    }

    private void setupBounds() {
        this.mCircleBounds = new RectF((float) (this.mBarWidth / 2), (float) (this.mBarWidth / 2), (float) (this.mLayoutWidth - (this.mBarWidth / 2)), (float) (this.mLayoutHeight - (this.mBarWidth / 2)));
    }

    private void setupPaints() {
        this.mBarPaint.setColor(this.mBarColor);
        this.mBarPaint.setAntiAlias(true);
        this.mBarPaint.setStyle(Style.STROKE);
        this.mBarPaint.setStrokeWidth((float) this.mBarWidth);
        this.mRimPaint.setColor(this.mRimColor);
        this.mRimPaint.setAntiAlias(true);
        this.mRimPaint.setStyle(Style.STROKE);
        this.mRimPaint.setStrokeWidth((float) this.mRimWidth);
    }

    @SuppressLint("ResourceType")
    private void parseAttributes(TypedArray typedArray) {
        int i = 0;
        this.mBarWidth = (int) typedArray.getDimension(1, (float) this.mBarWidth);
        this.mRimWidth = (int) typedArray.getDimension(4, (float) this.mRimWidth);
        this.mBarColor = typedArray.getColor(0, this.mBarColor);
        this.mRimColor = typedArray.getColor(3, this.mRimColor);
        if (typedArray.hasValue(2)) {
            int i2 = typedArray.getInt(2, 0);
            if (i2 >= 0) {
                i = i2 > 100 ? 100 : i2;
            }
            this.mProgress = (float) i;
        }
        typedArray.recycle();
    }

    @SuppressLint({"DrawAllocation"})
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(this.mCircleBounds, 360.0f, 360.0f, false, this.mRimPaint);
        canvas.drawArc(this.mCircleBounds, -90.0f, 360.0f * this.mProgress, false, this.mBarPaint);
    }

    public void startAnimation(ValueAnimator valueAnimator, ProgressWheelAnimationListener progressWheelAnimationListener) {
        this.mValueAnimator = valueAnimator;
        this.mProgressWheelAnimationListener = progressWheelAnimationListener;
        this.mValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (!ProgressWheel.this.mAnimationCancelled) {
                    ProgressWheel.this.setProgress(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            }
        });
        this.mValueAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                if (ProgressWheel.this.mProgressWheelAnimationListener != null) {
                    ProgressWheel.this.mProgressWheelAnimationListener.onAnimationEnd();
                    ProgressWheel.this.mAnimationCancelled = true;
                }
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
                if (ProgressWheel.this.mProgressWheelAnimationListener != null && !ProgressWheel.this.mProgressWheelAnimationListener.onAnimationRepeat()) {
                    ProgressWheel.this.mAnimationCancelled = true;
                    animator.cancel();
                }
            }
        });
        this.mAnimationCancelled = false;
        this.mValueAnimator.start();
    }

    public void setProgress(float f) {
        this.mProgress = f;
        invalidate();
    }

    public void setBarColor(int i) {
        this.mBarColor = i;
        this.mBarPaint.setColor(i);
        invalidate();
    }

}