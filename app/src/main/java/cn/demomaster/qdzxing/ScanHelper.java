package cn.demomaster.qdzxing;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 扫描帮助类
 */
public class ScanHelper {
    public static ScanHelper instance;

    public static ScanHelper getInstance() {
        if (instance == null) {
            instance = new ScanHelper();
        }
        return instance;
    }

    private ScanHelper() {
    }

    private CameraManager cameraManager;
    public CameraManager getCameraManager(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context.getApplicationContext());
        }
        return cameraManager;
    }

    private CaptureActivityHandler handler;

    public CaptureActivityHandler getHandler() {
        return handler;
    }

    public void setHandler(CaptureActivityHandler handler) {
        this.handler = handler;
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
        if (onScanResultListener != null) {
            onScanResultListener.handleDecode(obj, barcode, scaleFactor);
        }
    }

    //扫描结果监听
    OnScanResultListener onScanResultListener;

    public OnScanResultListener getOnScanResultListener() {
        return onScanResultListener;
    }

    /**
     * 扫描结果监听器
     *
     * @param onScanResultListener
     */
    public void setOnScanResultListener(OnScanResultListener onScanResultListener) {
        this.onScanResultListener = onScanResultListener;
    }

    public void restartDelay(long l) {

    }

    public static interface OnScanResultListener {
        public void handleDecode(Result obj, Bitmap barcode, float scaleFactor);
    }

    /**
     * 扫描到的疑似点
     */
    private ResultPointCallback resultPointCallback = new ResultPointCallback() {
        @Override
        public void foundPossibleResultPoint(ResultPoint resultPoint) {
            for (Map.Entry entry : resultPointCallbackHashMap.entrySet()) {
                try {
                    ResultPointCallback resultPointCallback = (ResultPointCallback) entry.getValue();
                    if (resultPointCallback != null) {
                        resultPointCallback.foundPossibleResultPoint(resultPoint);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public ResultPointCallback getResultPointCallback() {
        return resultPointCallback;
    }

    Map<Integer, ResultPointCallback> resultPointCallbackHashMap = new HashMap<>();

    public void addResultPointCallback(ResultPointCallback resultPointCallback) {
        resultPointCallbackHashMap.put(resultPointCallback.hashCode(), resultPointCallback);
    }

}
