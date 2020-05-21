package cn.demomaster.qdzxing;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.demomaster.qdzxing.sample.MyCaptureActivity;
import cn.demomaster.qdzxinglibrary.ScanMakerView;
import cn.demomaster.qdzxinglibrary.ScanSurfaceView;

public class MainActivity extends AppCompatActivity {
    //遮盖层视图
    ScanMakerView smv;
    ScanSurfaceView ssfv ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ssfv = findViewById(R.id.ssfv);
        smv = findViewById(R.id.smv);
        ssfv.addMakerView(smv);
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
