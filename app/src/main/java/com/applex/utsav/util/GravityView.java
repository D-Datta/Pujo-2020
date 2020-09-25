package com.applex.utsav.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Display;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

public class GravityView implements SensorEventListener {

    @SuppressLint("StaticFieldLeak")
    private static GravityView gravityView;
    private static SensorManager mSensorManager;
    private static Sensor mSensor;
    private final Context mContext;
    private float smoothedValue;
    private ImageView image_view;
    private int mImageWidth;
    private int mMaxScroll;


    public static synchronized GravityView getInstance(Context context) {
        if (gravityView == null) {
            gravityView = new GravityView(context.getApplicationContext());
        }
        return gravityView;
    }

    private GravityView(Context context) {
        this.mContext = context;
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

    }

    @SuppressLint("ClickableViewAccessibility")
    public GravityView setImage(ImageView image, Bitmap bitmap) {
        image_view = image;
        Bitmap bmp = resizeBitmap(getDeviceHeight(mContext), bitmap);
        image_view.setLayoutParams(new HorizontalScrollView.LayoutParams(bmp.getWidth(), bmp.getHeight()));
        image_view.setImageBitmap(bmp);
        mMaxScroll = bmp.getWidth();
        if (image.getParent() instanceof HorizontalScrollView) {
            ((HorizontalScrollView) image.getParent()).setOnTouchListener((view, motionEvent) -> true);
        }
        return gravityView;
    }

    private Bitmap resizeBitmap(int targetH, Bitmap bitmap) {
        mImageWidth = (bitmap.getWidth() * getDeviceHeight(mContext)) / bitmap.getHeight();

        return Bitmap.createScaledBitmap(bitmap, mImageWidth, targetH, true);
    }

    public GravityView center() {
        image_view.post(() -> {
            if (mImageWidth > 0) {
                ((HorizontalScrollView) image_view.getParent()).setScrollX(mImageWidth / 4);
            } else {
                ((HorizontalScrollView) image_view.getParent()).setScrollX(((HorizontalScrollView) image_view.getParent()).getWidth() / 2);
            }
        });
        return gravityView;
    }


    public boolean deviceSupported() {
        return (null != mSensorManager && null != mSensor);
    }

    public void registerListener() {
        if (deviceSupported())
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void unRegisterListener() {
        if (deviceSupported())
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = event.values[1];
        value = value * 50;
        smoothedValue = smooth(value, smoothedValue);
        value = smoothedValue;

        if(image_view != null) {
            int scrollX = ((HorizontalScrollView) image_view.getParent()).getScrollX();
            if (scrollX + value >= mMaxScroll) value = mMaxScroll - scrollX;
            if (scrollX + value <= -mMaxScroll) value = -mMaxScroll - scrollX;
            ((HorizontalScrollView) image_view.getParent()).scrollBy((int) value, 0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) { }

    private float smooth(float input, float output) {
        float ALPHA = 0.2f;
        return (int) (output + ALPHA * (input - output));
    }

    private int getDeviceHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }
}