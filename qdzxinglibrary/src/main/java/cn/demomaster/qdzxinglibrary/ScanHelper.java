package cn.demomaster.qdzxinglibrary;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        onScanResultListenerMap = new HashMap<>();
        resultPointCallbackHashMap = new HashMap<>();
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
    
   /* private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;*/
    public void generateHandler(Context context,Collection<BarcodeFormat> decodeFormats,Map<DecodeHintType, ?> decodeHints,String characterSet) {
        this.handler = new CaptureActivityHandler(context, decodeFormats, decodeHints, characterSet, cameraManager);
    }

    /**
     * 处理扫码结果，分发给监听器
     * @param obj
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
        for(Map.Entry entry :onScanResultListenerMap.entrySet()){
            ((OnScanResultListener)entry.getValue()).handleDecode(obj, barcode, scaleFactor);
        }
    }

    //扫描结果监听
    Map<Context,OnScanResultListener> onScanResultListenerMap;

    /**
     * 扫描结果监听器
     * @param onScanResultListener
     */
    public void addOnScanResultListener(Context context, OnScanResultListener onScanResultListener) {
        onScanResultListenerMap.put(context,onScanResultListener);
    }

    public void restartDelay(long l) {
        //restartPreviewAndDecode
        ScanHelper.getInstance().getHandler().sendEmptyMessageDelayed(R.id.restart_preview, l);
    }

    public static interface OnScanResultListener {
        //解码结果
        void handleDecode(Result obj, Bitmap barcode, float scaleFactor);
        //扫描到的疑似点
        void foundPossiblePoint(ResultPoint resultPoint);
    }

    /**
     * 扫描到的疑似点
     */
    private ResultPointCallback resultPointCallback;
    public ResultPointCallback getResultPointCallback() {
        if(resultPointCallback==null){
            resultPointCallback = new ResultPointCallback() {
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
        }
        return resultPointCallback;
    }

    Map<Integer, ResultPointCallback> resultPointCallbackHashMap ;

    /**
     * 添加扫码疑似点实时回调
     * @param resultPointCallback
     */
    public void addResultPointCallback(ResultPointCallback resultPointCallback) {
        resultPointCallbackHashMap.put(resultPointCallback.hashCode(), resultPointCallback);
    }

    /**
     * activity注销时移除监听
     * @param context
     */
    public void unregisterListener(Context context){
        resultPointCallbackHashMap.remove(context);
    }
}
