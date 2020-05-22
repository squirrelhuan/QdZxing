package cn.demomaster.qdzxinglibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.AmbientLightManager;
import com.google.zxing.client.android.CaptureActivityHandler;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * 背景层预览相机图层
 */
public class ScanSurfaceView extends SurfaceView implements ScanHelper.OnScanResultListener {
    public static String TAG = "CGQ";

    public ScanSurfaceView(Context context) {
        super(context);
        init();
    }

    public ScanSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //检测环境光
    private AmbientLightManager ambientLightManager;
    boolean adjustedSize;

    public void setAdjustedSize(boolean hasAdjusted) {
        this.adjustedSize = hasAdjusted;
    }

    public void init() {
        cameraManager = ScanHelper.getInstance().getCameraManager(getContext());
        ambientLightManager = new AmbientLightManager(getContext());
        ambientLightManager.start(cameraManager);
        ScanHelper.getInstance().setOnScanResultListener(getContext(),mOnScanResultListener);
        //ScanHelper.getInstance().setResultPointCallback(getContext(), getResultPointCallback());

        if (callback == null) {
            callback = new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (holder == null) {
                        Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
                    }
                    initCamera(holder);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    if(!adjustedSize&&ScanHelper.getInstance().getCameraManager(getContext())!=null&&ScanHelper.getInstance().getCameraManager(getContext()).getConfigManager()!=null){
                        Point point = ScanHelper.getInstance().getCameraManager(getContext()).getConfigManager().getBestPreviewSize();
                        if(point!=null){
                            adjustedSize = true;
                            ViewGroup.LayoutParams layoutParams = getLayoutParams();

                            //长宽比
                            float wh1 = (float) point.x/point.y;
                            float wh2 = (float) getHeight()/getWidth();
                            Log.e("CGQ","预览宽："+point.y+"，预览长："+point.x+",getWidth()="+getWidth()+"，getHeight()="+getHeight()+",wh1="+wh1+",wh2="+wh2);
                            if(wh1!=wh2){
                                if(wh1<wh2){//横向宽
                                   float d = ((float)point.y/point.x);
                                   // layoutParams.height = (int) (((float)point.x/point.y)*width);
                                    layoutParams.width = (int) (d*height);
                                    setLayoutParams(layoutParams);
                                    Log.e("CGQ","设置宽"+layoutParams.width +",getWidth()="+getWidth());
                                }else if(wh1>wh2){//纵向长
                                    float d = ((float)point.x/point.y);
                                    layoutParams.height = (int) (d*width);
                                    setLayoutParams(layoutParams);
                                    Log.e("CGQ","设置高"+layoutParams.height +",getHeight()="+getHeight());
                                }
                            }else {
                                Log.e("CGQ","长宽比例正常");
                            }
                        }
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            };
        }
        getHolder().addCallback(callback);
    }

    public void start() {
        cameraManager = ScanHelper.getInstance().getCameraManager(getContext());
        getHolder().addCallback(callback);
        if (ambientLightManager == null) {
            ambientLightManager = new AmbientLightManager(getContext());
        }
        ambientLightManager.start(cameraManager);
        ScanHelper.getInstance().setOnScanResultListener(getContext(),this);
        //ScanHelper.getInstance().addResultPointCallback(getContext(), getResultPointCallback());
        initCamera(getHolder());

        ScanHelper.getInstance().getHandler().resetQuitSynchronously();
        ScanHelper.getInstance().restartDelay(100);
    }

    public void stop() {
        ambientLightManager.stop();
        getHolder().removeCallback(callback);
        if (ScanHelper.getInstance().getHandler() != null) {
            ScanHelper.getInstance().getHandler().quitSynchronously();
            ScanHelper.getInstance().setHandler(null);
        }
        ScanHelper.getInstance().getCameraManager(getContext()).closeDriver();
        ScanHelper.getInstance().unregisterListener(getContext());
    }

    SurfaceHolder.Callback callback;

    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private CameraManager cameraManager;

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.e(TAG, "initCamera() while already open -- late SurfaceView callback?");
            cameraManager.setHolder(surfaceHolder);
            // throw new Exception("initCamera() while already open -- late SurfaceView callback?");
            // return;
        } else {
            try {
                cameraManager.openDriver(surfaceHolder);
            } catch (IOException ioe) {
                Log.w(TAG, ioe);
            } catch (RuntimeException e) {
                // Barcode Scanner has seen crashes in the wild of this variety:
                // java.?lang.?RuntimeException: Fail to connect to camera service
                Log.w(TAG, "Unexpected error initializing camera", e);
            }
        }
        // Creating the handler starts the preview, which can also throw a RuntimeException.
        if (cameraManager != null && cameraManager.isOpen() && ScanHelper.getInstance().getHandler() == null) {
            ScanHelper.getInstance().setHandler(new CaptureActivityHandler(getContext(), decodeFormats, decodeHints, characterSet, cameraManager));

        }
    }

    ScanHelper.OnScanResultListener mOnScanResultListener;
    public void setOnScanResultListener(ScanHelper.OnScanResultListener onScanResultListener) {
        this.mOnScanResultListener = onScanResultListener;
    }

    @Override
    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
        if (mOnScanResultListener != null) {
            mOnScanResultListener.handleDecode(obj,barcode,scaleFactor);
        }
    }

    @Override
    public void foundPossiblePoint(ResultPoint resultPoint) {
        if (mOnScanResultListener != null) {
            mOnScanResultListener.foundPossiblePoint(resultPoint);
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
   /* private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
            Canvas canvas = new Canvas(barcode);
            Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.result_points));
            if (points.length == 2) {
                paint.setStrokeWidth(4.0f);
            } else if (points.length == 4 &&
                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
                // Hacky special case -- draw two lines, for the barcode and metadata
            } else {
                paint.setStrokeWidth(10.0f);
                for (ResultPoint point : points) {
                    if (point != null) {
                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
                    }
                }
            }
        }
    }*/

}
