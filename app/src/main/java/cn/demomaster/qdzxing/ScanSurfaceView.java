package cn.demomaster.qdzxing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
public class ScanSurfaceView extends SurfaceView implements ScanHelper.OnScanResultListener{
    public static String TAG="CGQ";
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

    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //检测环境光
    private AmbientLightManager ambientLightManager;
    public void init() {
        cameraManager = ScanHelper.getInstance().getCameraManager(getContext());
        getHolder().addCallback(callback);
        ambientLightManager = new AmbientLightManager(getContext());
        ambientLightManager.start(ScanHelper.getInstance().getCameraManager(getContext()));
        ScanHelper.getInstance().setOnScanResultListener(this);
    }

    public void start() {
        cameraManager = ScanHelper.getInstance().getCameraManager(getContext());
        getHolder().addCallback(callback);
        if(ambientLightManager==null) {
            ambientLightManager = new AmbientLightManager(getContext());
        }
        ambientLightManager.start(ScanHelper.getInstance().getCameraManager(getContext()));
        ScanHelper.getInstance().setOnScanResultListener(this);
        initCamera(getHolder());
        ScanHelper.getInstance().restartDelay(0);
    }

    public void stop(){
        ambientLightManager.stop();
        getHolder().removeCallback(callback);
        if (ScanHelper.getInstance().getHandler() != null) {
            ScanHelper.getInstance().getHandler().quitSynchronously();
            ScanHelper.getInstance().setHandler(null);
        }
        ScanHelper.getInstance().getCameraManager(getContext()).closeDriver();
    }

    SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder == null) {
                Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
            }
            initCamera(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private CameraManager cameraManager;
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (ScanHelper.getInstance().getHandler() == null) {
                ScanHelper.getInstance().setHandler(new CaptureActivityHandler((Context) getContext(), decodeFormats, decodeHints, characterSet, cameraManager));
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    @Override
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        Log.i(TAG,"rawResult="+rawResult.toString());

        Vibrator vibrator = (Vibrator)getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        long[] patter = {50, 50};
        vibrator.vibrate(patter, -1);

        boolean fromLiveScan = barcode != null;
        if (fromLiveScan) {
            // Then not from history, so beep/vibrate and we have an image to draw on
            //TODO beepManager.playBeepSoundAndVibrate();
            drawResultPoints(barcode, scaleFactor, rawResult);
        }
    }

    /**
     * Superimpose a line for 1D or dots for 2D to highlight the key features of the barcode.
     *
     * @param barcode     A bitmap of the captured image.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param rawResult   The decoded results which contains the points to draw.
     */
    private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
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
    }

    /**
     * 添加疑似点接收
     * @param resultPointCallback
     */
    public void addMakerView(ResultPointCallback resultPointCallback){
        ScanHelper.getInstance().addResultPointCallback(resultPointCallback);
    }

}
