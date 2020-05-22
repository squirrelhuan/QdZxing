# QdZxing
二维码扫描与生成(基于Zxing)

方便开发者以view的形式集成到页面中，方便自定义页面布局。


layout
```java
<RelativeLayout
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_marginTop="50dp"
        android:layout_gravity="center">
        <!--背景层用于显示camera视图-->
        <cn.demomaster.qdzxinglibrary.ScanSurfaceView
            android:id="@+id/ssfv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
        <!--遮罩层用于显示扫描窗口和扫描动画-->
        <cn.demomaster.qdzxinglibrary.ScanMakerView
            android:id="@+id/smv"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </RelativeLayout>
```
Activity
```
//遮盖层视图
    ScanMakerView scanMakerView;
    //背景预览图层
    ScanSurfaceView surfaceView;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //请求屏幕常亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mycapture);

        scanMakerView = (ScanMakerView) findViewById(R.id.viewfinder_view);
        surfaceView= (ScanSurfaceView) findViewById(R.id.preview_view);
        scanMakerView.setMarkerHeight(500);//设置扫码框大小
        scanMakerView.setMarkerWidth(500);//设置扫码框大小
        //扫码回调
        surfaceView.setOnScanResultListener(new ScanHelper.OnScanResultListener() {
            @Override
            public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
                Toast.makeText(MyCaptureActivity.this,obj.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void foundPossiblePoint(ResultPoint resultPoint) {
                //扫描到疑似点，疑似点闪烁处理
                scanMakerView.foundPossibleResultPoint(resultPoint);
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.start();
    }
    
    @Override
    protected void onPause() {
        surfaceView.stop();
        super.onPause();
    }

```
