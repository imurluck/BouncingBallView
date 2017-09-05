package view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import zzx.bouncingballview.R;

/**
 * Created by yourgod on 2017/9/3.
 */

public class BouncingBallView extends View {

    private String TAG = "BouncingBallView";

    public static final double PI = 3.14159265358979323846;

    private int mWidth = 300;//view的宽
    private int mHeight = 300;//view的高
    private int largeRadius;//(mWidth / 2 - ballRadius)

    private float wabbyBallX;//摇动中的小球x坐标
    private float wabbyBallY;//摇动中的小球y坐标
    private float wabbyBallStartAngle;//摇动的小球开始摇动的角度
    private float wabbyBallAngle;//摇动中小球的角度

    private float runningBallX;//转动小球的x坐标
    private float runningBallY;//转动小球的y坐标
    private float runningBallStartAngle;//转动小球开始转动的角度
    private float runningBallAngle;//转动中小球的角度

    private float perAngle;//将圆均分的角度
    private float restBallStartAngle = 60;//剩下的五个小球的开始分布的角度
    private float phaseAngle;//当两个小球碰撞时他们之间相差的角度

    private Paint mPaint;

    private STATUS currentStatus = STATUS.FIRSTCYCLE;

    private BouncingBallConfig config;//保存小球配置的类

    private enum STATUS {//运动状态的集合
        FIRSTCYCLE,
        RESTCYCLE
    }
    public BouncingBallView(Context context) {
        this(context, null);
    }

    public BouncingBallView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BouncingBallView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        BouncingBallConfig config = new BouncingBallConfig();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.BouncingBallView, defStyle, 0);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.BouncingBallView_ballColor:
                    config.ballColor = a.getColor(attr, Color.BLUE);
                    break;
                case R.styleable.BouncingBallView_ballCount:
                    config.ballCount = a.getInteger(attr, 6);
                    break;
                case R.styleable.BouncingBallView_ballRadius:
                    config.ballRadius = a.getDimensionPixelSize(attr, 10);
                    break;
                case R.styleable.BouncingBallView_cycleTime:
                    config.cycleTime = a.getInteger(attr, 1000);
                    break;
                default:
                    break;
            }
        }
        init(config);
    }

    public void init(BouncingBallConfig config) {
        this.config = config;
        runningBallAngle = 0;
        wabbyBallAngle = 0;
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.EXACTLY) {
            mWidth = widthSpecSize - getPaddingRight() - getPaddingLeft();
        }
        if (heightSpecMode == MeasureSpec.EXACTLY) {
            mHeight = heightSpecSize - getPaddingBottom() - getPaddingTop();
        }
        setMeasuredDimension(mWidth, mHeight);
        largeRadius = mWidth / 2 - config.ballRadius;
        Log.e(TAG, "alrgeRadius = " + largeRadius);
        perAngle = 360 / config.ballCount;
        Log.e(TAG, "perAngle = " + perAngle);
        Log.e(TAG, "config.ballRadius = " + config.ballRadius);
        double a = ((double) config.ballRadius) / (((double)largeRadius));
        phaseAngle = (float) (2 * Math.asin(a) * 180 / PI);
        Log.e(TAG, "mWidth = " + mWidth);
        Log.e(TAG, "mHeight = " + mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(config.ballColor);
        mPaint.setAntiAlias(true);
        drawRestBall(canvas);//画剩下为运动得小球
        if (currentStatus == STATUS.FIRSTCYCLE) {
            wabbyBallAngle = 0;
            wabbyBallStartAngle = 0;
        }
        wabbyBallX = (float) Math.sin(wabbyBallAngle / 180 * PI) * largeRadius
                            + mWidth / 2;//计算摇摆中小球得横坐标
        wabbyBallY = (float) Math.cos(wabbyBallAngle / 180 * PI) * largeRadius
                            + mHeight / 2;//纵坐标
        canvas.drawCircle(wabbyBallX, wabbyBallY, config.ballRadius, mPaint);

        runningBallX = (float) Math.sin(runningBallAngle / 180 * PI) * largeRadius
                            + mWidth / 2;//计算转动中小球得横坐标
        runningBallY = (float) Math.cos(runningBallAngle / 180 * PI) * largeRadius
                            + mHeight / 2;//纵坐标
        canvas.drawCircle(runningBallX, runningBallY, config.ballRadius, mPaint);
    }

    public void drawRestBall(Canvas canvas) {
        for (float i = restBallStartAngle; i < perAngle * (config.ballCount - 1) + restBallStartAngle;
                i += perAngle) {
            //Log.e(TAG, "i = " + i);
            double x = Math.sin(i * 1.0f / 180 * PI) * largeRadius + mWidth / 2;
            double y = Math.cos(i * 1.0f / 180 * PI) * largeRadius + mHeight / 2;
            //Log.e(TAG, "Math.sin = " + Math.sin(i * 1.0f) * largeRadius);
            //Log.e(TAG, "Math.cos = " + Math.cos(i * 1.0f) * largeRadius);
            //Log.e(TAG, "x = " + x);
            //Log.e(TAG, "y = " + y);
            canvas.drawCircle((float) x, (float) y, config.ballRadius, mPaint);
        }
    }

    public void letUsAnimate() {
        //摇摆小球的动画
        final ValueAnimator wabbyBallAnimator = ValueAnimator.ofFloat(0, phaseAngle);
        wabbyBallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = wabbyBallAnimator.getAnimatedFraction();
                wabbyBallAngle = wabbyBallStartAngle +
                                (float) (Math.sin(fraction * 1.5 * PI) * phaseAngle);
                //Log.e(TAG, "wabbyBallAngle = " + wabbyBallAngle);
                //invalidate();

            }
        });
        wabbyBallAnimator.setDuration(config.cycleTime);
        //转动小球的动画
        final ValueAnimator runningBallAnimator = ValueAnimator.ofFloat(0, perAngle - phaseAngle);
        runningBallAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = runningBallAnimator.getAnimatedFraction();
                runningBallAngle = runningBallStartAngle - fraction * (perAngle - phaseAngle);
                Log.e(TAG, "fraction = " + fraction);
                //Log.e(TAG, "runningBallAngle = " + runningBallAngle);
                invalidate();
            }
        });
        runningBallAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.e(TAG, "Thread = " + Thread.currentThread());
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                Log.e(TAG, "onAnimationRepeat: ");
                runningBallStartAngle -= perAngle;//转动小球开始转动角度减去perAngle
                //这个在效果图中很容易看出来
                restBallStartAngle -= perAngle;//剩下的小球也减去perAngle
                if (currentStatus == STATUS.FIRSTCYCLE) {
                    wabbyBallStartAngle = 0 - (perAngle - phaseAngle);//如果是第一种状态刚结束时，
                    //此时摇摆小球开始摇摆位置应该在转动小球刚结束第一阶段转动的位置
                } else {
                    wabbyBallStartAngle -= perAngle;//第二阶段则每次减去perAngle
                }
                invalidate();
                currentStatus = STATUS.RESTCYCLE;//第一次重复代表第一个阶段结束
                wabbyBallAnimator.start();
                //Log.e(TAG, "restBallStartAngle = " + restBallStartAngle);
                //Log.e(TAG, "runningBallStartAngle = " + runningBallStartAngle);
            }
        });
        runningBallAnimator.setRepeatCount(ValueAnimator.INFINITE);
        runningBallAnimator.setDuration(config.cycleTime).start();
    }

}
