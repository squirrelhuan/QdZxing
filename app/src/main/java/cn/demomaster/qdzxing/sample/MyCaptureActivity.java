/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.demomaster.qdzxing.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.google.zxing.client.android.HelpActivity;
import com.google.zxing.client.android.Intents;
import com.google.zxing.client.android.PreferencesActivity;
import com.google.zxing.client.android.share.ShareActivity;

import cn.demomaster.qdzxing.R;
import cn.demomaster.qdzxing.ScanHelper;
import cn.demomaster.qdzxing.ScanMakerView;
import cn.demomaster.qdzxing.ScanSurfaceView;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class MyCaptureActivity extends Activity {
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
    }

    @Override
    protected void onResume() {
        super.onResume();

        scanMakerView = (ScanMakerView) findViewById(R.id.viewfinder_view);
        surfaceView= (ScanSurfaceView) findViewById(R.id.preview_view);
        surfaceView.addMakerView(scanMakerView);
    }

    @Override
    protected void onPause() {
        surfaceView.stop();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {/*
            case KeyEvent.KEYCODE_BACK:
                    restartPreviewAfterDelay(0L);
                    return true;*/
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                ScanHelper.getInstance().getCameraManager(this).setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                ScanHelper.getInstance().getCameraManager(this).setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.capture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intents.FLAG_NEW_DOC);
        switch (item.getItemId()) {
            case R.id.menu_share:
                intent.setClassName(this, ShareActivity.class.getName());
                startActivity(intent);
                break;
            case R.id.menu_settings:
                intent.setClassName(this, PreferencesActivity.class.getName());
                startActivity(intent);
                break;
            case R.id.menu_help:
                intent.setClassName(this, HelpActivity.class.getName());
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (ScanHelper.getInstance().getHandler() != null) {
            ScanHelper.getInstance().getHandler().sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

}
