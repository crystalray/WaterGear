package com.ray.water;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public class WaterView extends View implements SensorEventListener {
    private static final int NO_COLOR = -1;
    private static Bitmap sCoverBitmap;
    private static Bitmap sNormalWaterBitmap;
    private static Bitmap sWaterFaroutBitmap;
    private static Bitmap sErrorWaterBitmap;
    private static Bitmap sErrorFaroutBitmap;
    private Canvas mAidCanvas;
    private Canvas mAidCanvas2;
    private Bitmap mAidCanvasBitmap;
    private Bitmap mAidCanvasBitmap2;
    private boolean mAnimationOn = true;
    private Paint mClearPaint;
    private int mCurrentFaroutColor;
    private Bitmap mCurrentWaterBitmap;
    private Bitmap mCurrentFaroutBitmap;
    private float mDensity;
    private Rect mDestFaroutRect;
    private Rect mDestWaterRect;
    private Paint mDstInPaint;
    private int mFaroutWaterOffset1;
    private int mFaroutWaterOffset2;
    private static WaterView mWaterView;

    private static Handler mHandler = new Handler() {
        public void handleMessage(Message paramAnonymousMessage) {
            mWaterView.invalidate();
        }
    };
    private int mHeight;
    private float mPreXValue = 0.0F;
    private SensorManager mSensorManager = (SensorManager) getContext().getSystemService("sensor");
    private int mShakingVal = 0;
    private Paint mSrcOverPaint;
    private int mWaterLength;
    private int mWaterOffset;
    private int mWaveDistance;
    private int mWidth;
    private int mCurrentHeight;
    private int mTargetHeight;
    private boolean mInited = false;
//    private boolean mIsError = false;

    public WaterView(Context paramContext) {
        this(paramContext, null);
    }

    public WaterView(Context paramContext, AttributeSet paramAttributeSet) {
        this(paramContext, paramAttributeSet, 0);
    }

    public WaterView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
    }

    private void caculate() {
        int waveHeight = (int) ((10 + mShakingVal / 10) * mDensity);
        mWaterOffset = (waveHeight + mWaterOffset);
        if (mWaterOffset > mWaterLength) {
            mWaterOffset = 0;
        }
        mFaroutWaterOffset1 = (waveHeight + mFaroutWaterOffset1);
        if (mFaroutWaterOffset1 > mWaterLength) {
            mFaroutWaterOffset1 = 0;
        }
        mFaroutWaterOffset2 = (waveHeight + mFaroutWaterOffset2);
        if (mFaroutWaterOffset2 > mWaterLength) {
            mFaroutWaterOffset2 = 0;
        }
        if (mShakingVal > 0) {
            mShakingVal--;
        }
        if (mCurrentHeight > mTargetHeight) {
            caculateDestRects();
            mCurrentHeight--;
        }
        if (mCurrentHeight == 0) {
            toggleAnimationOn(false);
            mBackgroundProgressCallback.onFinishProgress();
        }
    }

    private void caculateDestRects() {
        mDestWaterRect = new Rect(0, mCurrentHeight, mWidth, mCurrentHeight + mHeight);
        int destFaroutHeight2 = (int) (mCurrentHeight - 1.0 * mDensity);
        mDestFaroutRect = new Rect(0, destFaroutHeight2, mWidth, destFaroutHeight2 + mHeight);
        return;
    }

    private void createAidCanvas() {
        mAidCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mAidCanvas = new Canvas(mAidCanvasBitmap);
        mAidCanvasBitmap2 = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mAidCanvas2 = new Canvas(mAidCanvasBitmap2);
    }

    private void decodeBitmaps() {
        if (sNormalWaterBitmap == null) {
            sNormalWaterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_normal);
        }
        if (sWaterFaroutBitmap == null) {
            sWaterFaroutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_farout);
        }
        if (sErrorWaterBitmap == null) {
            sErrorWaterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_error);
        }
        if (sErrorFaroutBitmap == null) {
            sErrorFaroutBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_farout_error);
        }
        if (sCoverBitmap == null) {
            sCoverBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.water_cover);
        }
        mCurrentWaterBitmap = sNormalWaterBitmap;
        mCurrentFaroutBitmap = sWaterFaroutBitmap;
        return;
    }

    private void drawWater(Canvas canvas) {
        caculate();
        mAidCanvas2.drawPaint(mClearPaint);
        drawWaterLayer(mCurrentFaroutBitmap, new Rect(mFaroutWaterOffset1, 0, mFaroutWaterOffset1 + mWidth, mHeight
                - mShakingVal), mDestFaroutRect, mCurrentFaroutColor);
        drawWaterLayer(mCurrentFaroutBitmap, new Rect(mFaroutWaterOffset2, 0, mFaroutWaterOffset2 + mWidth, mHeight
                - mShakingVal), mDestFaroutRect, mCurrentFaroutColor);
        drawWaterLayer(mCurrentWaterBitmap, new Rect(mWaterOffset, 0, mWaterOffset + mWidth, mHeight - mShakingVal),
                mDestWaterRect, NO_COLOR);
        canvas.drawBitmap(mAidCanvasBitmap2, 0.0F, 0.0F, null);
    }

    private void drawWaterLayer(Bitmap bitmap, Rect rect1, Rect rect2, int color) {
        mAidCanvas.drawPaint(mClearPaint);
        mAidCanvas.drawBitmap(bitmap, rect1, rect2, null);
        //		mAidCanvas.drawBitmap(sCoverBitmap, 0.0F, 0.0F, mDstInPaint);
        if (color != NO_COLOR) {
            mAidCanvas.drawColor(color, PorterDuff.Mode.SRC_IN);
        }
        mAidCanvas2.drawBitmap(mAidCanvasBitmap, 0.0F, 0.0F, null);
    }

    private void init() {
        decodeBitmaps();
        initVariables();
        caculateDestRects();
        createAidCanvas();
        initPaints();
        mInited = true;
    }

    private void initPaints() {
        mCurrentFaroutColor = NO_COLOR;
        mDstInPaint = new Paint();
        mDstInPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mSrcOverPaint = new Paint();
        mSrcOverPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        mClearPaint = new Paint();
        mClearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private void initVariables() {
        mWaterView = this;
        mWidth = sCoverBitmap.getWidth();
        mHeight = sCoverBitmap.getHeight();
        mWaterLength = (sWaterFaroutBitmap.getWidth() - mWidth);
        mDensity = getResources().getDisplayMetrics().density;
        mWaveDistance = ((int) (60.0F * mDensity));
        mWaterOffset = ((int) (System.currentTimeMillis() % mWaterLength));
        mFaroutWaterOffset1 = (mWaterOffset - 2 * mWaveDistance);
        mFaroutWaterOffset2 = (mWaterOffset - mWaveDistance);
        mCurrentHeight = mHeight;
        mTargetHeight = mHeight;
    }

    private void releaseBitmaps() {
        if (sNormalWaterBitmap != null) {
            sNormalWaterBitmap.recycle();
            sNormalWaterBitmap = null;
        }
        if (sWaterFaroutBitmap != null) {
            sWaterFaroutBitmap.recycle();
            sWaterFaroutBitmap = null;
        }
        if (sErrorWaterBitmap != null) {
            sErrorWaterBitmap.recycle();
            sErrorWaterBitmap = null;
        }
        if (sErrorFaroutBitmap != null) {
            sErrorFaroutBitmap.recycle();
            sErrorFaroutBitmap = null;
        }
        if (sCoverBitmap != null) {
            sCoverBitmap.recycle();
            sCoverBitmap = null;
        }
        if (mAidCanvasBitmap != null) {
            mAidCanvasBitmap.recycle();
            mAidCanvasBitmap = null;
        }
        if (mAidCanvasBitmap2 != null) {
            mAidCanvasBitmap2.recycle();
            mAidCanvasBitmap2 = null;
        }
        System.gc();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseBitmaps();
        mSensorManager.unregisterListener(this);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawWater(canvas);
        if (mAnimationOn) {
            mHandler.sendEmptyMessageDelayed(0, 10L);
        }
    }

    public void setProgress(final float percent) {
        if (mInited) {
            mTargetHeight = (int) (mHeight * (1f - percent / 100));
        } else {
            this.postDelayed(new Runnable() {

                @Override
                public void run() {
                    setProgress(percent);

                }
            }, 100);
        }
    }
    
    public void setError() {
        if (mInited) {
            if (mCurrentHeight > (int) (mHeight * 0.6)) {
                mTargetHeight = (int) (mHeight * 0.6);
                mCurrentHeight = mTargetHeight+1;
            } else {
                mTargetHeight = mCurrentHeight;
            }
            mCurrentWaterBitmap = sErrorWaterBitmap;
            mCurrentFaroutBitmap = sErrorFaroutBitmap;
        } else {
            this.postDelayed(new Runnable() {

                @Override
                public void run() {
                    setError();
                }
            }, 100);
        }
    }

    public void toggleAnimationOn(boolean animationOn) {
        if (mAnimationOn != animationOn) {
            mAnimationOn = animationOn;
            if (!mAnimationOn && !mHandler.hasMessages(0)) {
                return;
            }
            mShakingVal = 90;
            mHandler.sendEmptyMessage(0);
        }
        mHandler.removeMessages(0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float acceleometerChange;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            acceleometerChange = Math.abs(mPreXValue - sensorEvent.values[0]);
            if (acceleometerChange > 3.0F && acceleometerChange <= 6.0F) {
                mShakingVal = (30 * (int) acceleometerChange);
            } else if (acceleometerChange > 6f) {
                mShakingVal = 180;
            }
        }
        mPreXValue = sensorEvent.values[0];
        return;
    }

    private BackgroundProgressCallback mBackgroundProgressCallback = new EmptyBackgroundProgressCallback();

    public static interface BackgroundProgressCallback {
        void onFinishProgress();
    }

    private class EmptyBackgroundProgressCallback implements BackgroundProgressCallback {
        @Override
        public void onFinishProgress() {
        }
    }

    public void setProgressCallback(BackgroundProgressCallback callback) {
        mBackgroundProgressCallback = callback;
    }
}
