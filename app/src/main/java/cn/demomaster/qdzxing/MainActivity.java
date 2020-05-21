package cn.demomaster.qdzxing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

import cn.demomaster.qdzxing.sample.MyCaptureActivity;
import cn.demomaster.qdzxinglibrary.ScanHelper;
import cn.demomaster.qdzxinglibrary.ScanMakerView;
import cn.demomaster.qdzxinglibrary.ScanSurfaceView;

public class MainActivity extends AppCompatActivity {
    //遮盖层视图
    ScanMakerView smv;
    //相机图片渲染层
    ScanSurfaceView ssfv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ssfv = findViewById(R.id.ssfv);
        smv = findViewById(R.id.smv);
        smv.setMarkerHeight(500);
        smv.setMarkerWidth(500);
        //扫码回调
        ssfv.setOnScanResultListener(new ScanHelper.OnScanResultListener() {
            @Override
            public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
                Toast.makeText(MainActivity.this,obj.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void foundPossiblePoint(ResultPoint resultPoint) {
                smv.foundPossibleResultPoint(resultPoint);
            }
        });
    }

    public void goToScanActivity(View view){
        Intent intent = new Intent(this, MyCaptureActivity.class);
        startActivity(intent);
    }

    public void stopScan(View view) {
        ssfv.stop();
    }

    public void startScan(View view) {
        ssfv.start();
    }
}
