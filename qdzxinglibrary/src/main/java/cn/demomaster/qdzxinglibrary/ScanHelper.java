package cn.demomaster.qdzxinglibrary;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import static cn.demomaster.qdzxinglibrary.ScanSurfaceView.TAG;

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
        //onScanResultListenerMap = new HashMap<>();
        //resultPointCallbackHashMap = new HashMap<>();
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
   public Context mContext;
   /* public void generateHandler(Context context,Collection<BarcodeFormat> decodeFormats,Map<DecodeHintType, ?> decodeHints,String characterSet) {
        this.handler = new CaptureActivityHandler(context, decodeFormats, decodeHints, characterSet, cameraManager);
    }*/

    /**
     * 处理扫码结果，分发给监听器
     * @param obj
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
        mOnScanResultListener.handleDecode(obj,barcode,scaleFactor);
    }

    //扫描结果监听
    OnScanResultListener mOnScanResultListenerSub;
    final OnScanResultListener mOnScanResultListener = new OnScanResultListener() {
        @Override
        public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
            Log.i(TAG, "扫码完成：" + obj == null ? "null" : obj.toString());//震动
            Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
            long[] patter = {50, 50};
            vibrator.vibrate(patter, -1);
            if(mOnScanResultListenerSub!=null)
            mOnScanResultListenerSub.handleDecode(obj,barcode,scaleFactor);
        }

        @Override
        public void foundPossiblePoint(ResultPoint resultPoint) {
            if(mOnScanResultListenerSub!=null)
            mOnScanResultListenerSub.foundPossiblePoint(resultPoint);
        }
    };
    /**
     * 扫描到的疑似点
     */
    public ResultPointCallback resultPointCallback= resultPoint -> mOnScanResultListener.foundPossiblePoint(resultPoint);
    /**
     * 扫描结果监听器
     * @param onScanResultListener
     */
    public void setOnScanResultListener(Context context, OnScanResultListener onScanResultListener) {
        mContext = context.getApplicationContext();
        mOnScanResultListenerSub = onScanResultListener;
    }

    public void restartDelay(long l) {
        //restartPreviewAndDecode
        ScanHelper.getInstance().getHandler().sendEmptyMessageDelayed(R.id.restart_preview, l);
    }

    public interface OnScanResultListener {
        //解码结果
        void handleDecode(Result obj, Bitmap barcode, float scaleFactor);
        //扫描到的疑似点
        void foundPossiblePoint(ResultPoint resultPoint);
    }



    //Map<Context, ResultPointCallback> resultPointCallbackHashMap ;

    /**
     * 添加扫码疑似点实时回调
     * @param context
     * @param resultPointCallback
     */
 /*   public void setResultPointCallback(Context context, ResultPointCallback resultPointCallback) {
        resultPointCallbackHashMap.put(context, resultPointCallback);
    }*/

    /**
     * activity注销时移除监听
     * @param context
     */
    public void unregisterListener(Context context){
        //resultPointCallbackHashMap.remove(context);
    }


    /**
     * @return 是否有闪光灯
     */
    public boolean isSupportCameraLedFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void openFlash(Context context){
        setFlashState(context,true);
    }
    public void closeFlash(Context context){
        setFlashState(context,false);
    }

    public void setFlashState(Context context,boolean open){
        if(!isSupportCameraLedFlash(context)){
            Toast.makeText(context,"闪光灯不存在",Toast.LENGTH_LONG).show();
            return;
        }
        Camera.Parameters parameter;
        parameter = getCameraManager(context).getCamera().getParameters();
        if(open){
            //打开闪光灯
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }else {
            //关闭闪光灯
            parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        getCameraManager(context).getCamera().setParameters(parameter);
    }

    public boolean isFlashOpened(Context context){
        if(!isSupportCameraLedFlash(context)){
            return false;
        }
        Camera.Parameters parameter;
        parameter = getCameraManager(context).getCamera().getParameters();
        String state = parameter.getFlashMode();
        return state != null && !state.equals(Camera.Parameters.FLASH_MODE_OFF);
    }
}
