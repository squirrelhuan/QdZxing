package cn.demomaster.qdzxing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.encode.EncodeActivity;

import cn.demomaster.qdzxing.sample.MyCaptureActivity;
import cn.demomaster.qdzxinglibrary.CodeCreator;
import cn.demomaster.qdzxinglibrary.ScanHelper;
import cn.demomaster.qdzxinglibrary.ScanMakerView;
import cn.demomaster.qdzxinglibrary.ScanSurfaceView;

public class MainActivity extends AppCompatActivity {
    //遮盖层视图
    ScanMakerView smv;
    //相机图片渲染层
    ScanSurfaceView ssfv ;
    ImageView iv_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_code = findViewById(R.id.iv_code);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
                requestPermissions( permissions, 1024);
            }else {

            }
        }
        /*Intent intent = new Intent();
        intent.setAction(Intents.Encode.ACTION);
        QRCodeEncoder qrCodeEncoder = null;
        try {
            qrCodeEncoder = new QRCodeEncoder(this, intent, 100, false);
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            iv_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }*/
       // startActivity(new Intent(MainActivity.this,EncodeActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                startActivity(new Intent(MainActivity.this,EncodeActivity.class));
            }
        }
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

    public void generateQrcode(View view) {
       EditText et_code_str =  findViewById(R.id.et_code_str);
        Bitmap bitmap = CodeCreator.createQRCode(et_code_str.getText().toString(), 500, 500, null);
        iv_code.setImageBitmap(bitmap);
    }
}
