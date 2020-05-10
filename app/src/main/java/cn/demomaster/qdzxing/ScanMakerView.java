/*
 * Copyright (C) 2008 ZXing authors
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

package cn.demomaster.qdzxing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.ArrayList;
import java.util.List;

import static cn.demomaster.qdzxing.ScanSurfaceView.TAG;

/**
 * 扫描控件maker层，用于处理扫描框  扫描特效光
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ScanMakerView extends View implements ResultPointCallback{

    private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
    private static final long ANIMATION_DELAY = 80L;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int MAX_RESULT_POINTS = 20;
    private static final int POINT_SIZE = 6;

    private final Paint paint;
    private final int maskColor;
    private final int laserColor;
    private final int resultPointColor;
    private int scannerAlpha;
    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    //闪烁的疑似点
    private int pointColor = Color.GREEN;
    //扫描线
    private int scanLineColor = Color.RED;

    // This constructor is used when the class is built from an XML resource.
    public ScanMakerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        laserColor = scanLineColor;
        resultPointColor = pointColor;
        scannerAlpha = 0;
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;

        Display defaultDisplay = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        screenWidth = point.x;
        screenHeight = point.y;
        Log.i(TAG, "screenWidth = " + screenWidth + ",screenHeight = " + screenHeight);
        //x = 1440,y = 2768
    }
    int screenWidth;
    int screenHeight;

    float roundRadius = 5f;
    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        if (ScanHelper.getInstance().getCameraManager(getContext()) == null) {
            return; // not ready yet, early draw before done configuring
        }
        Rect frame = ScanHelper.getInstance().getCameraManager(getContext()).getFramingRect();
        Rect previewFrame = ScanHelper.getInstance().getCameraManager(getContext()).getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float centerX = width/2;
        float centerY = height/2;
        int newwidth = frame.width()*width/screenWidth;
        int newheight = frame.height()*height/screenHeight;

        // Draw the exterior (i.e. outside the framing rect) darkened
        canvas.drawColor(maskColor);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        paint.setColor(Color.BLACK);
        RectF rectF = new RectF(centerX-newwidth/2, centerY-newheight/2, centerX+newwidth/2, centerY+newheight/2);
        frame = new Rect((int) rectF.left,(int)rectF.top,(int)rectF.right,(int)rectF.bottom);
        canvas.drawRoundRect(rectF, roundRadius, roundRadius, paint);
        paint.setXfermode(null);

        // Draw a red "laser scanner" line through the middle to show decoding is active
        paint.setColor(laserColor);
        paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
        scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
        int middle = (int) ((rectF.top+rectF.bottom)/2f);
        canvas.drawRect(rectF.left + 2, middle - 1, rectF.right - 1, middle + 2, paint);

        float scaleX = frame.width() / (float) previewFrame.width();
        float scaleY = frame.height() / (float) previewFrame.height();

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = frame.left;
        int frameTop = frame.top;
        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            paint.setAlpha(CURRENT_POINT_OPACITY);
            paint.setColor(resultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            POINT_SIZE, paint);
                }
            }
        }
        if (currentLast != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY / 2);
            paint.setColor(resultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY),
                            radius, paint);
                }
            }
        }

        // Request another update at the animation interval, but only repaint the laser line,
        // not the entire viewfinder mask.
        postInvalidateDelayed(ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE);
    }

    /**
     * 添加疑似点
     *
     * @param point
     */
    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint resultPoint) {
        addPossibleResultPoint(resultPoint);
    }
}
