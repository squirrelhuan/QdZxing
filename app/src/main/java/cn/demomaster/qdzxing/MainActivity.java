package cn.demomaster.qdzxing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.client.android.CaptureActivity;

import cn.demomaster.qdzxing.sample.MyCaptureActivity;

public class MainActivity extends AppCompatActivity {

    ScanSurfaceView ssfv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ssfv = findViewById(R.id.ssfv);
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
