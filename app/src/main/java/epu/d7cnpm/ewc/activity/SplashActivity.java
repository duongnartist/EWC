package epu.d7cnpm.ewc.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

import epu.d7cnpm.ewc.R;
import epu.d7cnpm.ewc.core.TessOcr;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = SplashActivity.class.getSimpleName();

    static {
        if(OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV loaded");
        } else {
            Log.i(TAG, "OpenCV not loaded");
        }
    }

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Thread(new Runnable() {
            @Override
            public void run() {
                TessOcr.getInstance().initialize(SplashActivity.this);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }).start();
    }

}
