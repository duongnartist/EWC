package epu.d7cnpm.ewc.core;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import epu.d7cnpm.ewc.activity.SplashActivity;

/**
 * Created by duong on 5/11/16.
 */
public class TessOcr extends TessBaseAPI {

    private static TessOcr instance = null;

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/EWC/";
    public static final String PATH = TessOcr.DATA_PATH + "/ocr.jpg";
    public static final String TAG = SplashActivity.class.getSimpleName();
    public static final String ROOT = "tessdata/";
    public static final String EXTENSION = ".traineddata";
    public static final String LANGUAGE = "eng";
    private Context context;

    private TessOcr() {
        super();
    }

    public static TessOcr getInstance() {
        if (instance == null) {
            instance = new TessOcr();
        }
        return instance;
    }

    public void initialize(Context context) {
        this.context = context;
        copyTrainedData();
    }

    private void copyTrainedData() {
        String[] paths = new String[]{DATA_PATH, DATA_PATH + ROOT};
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    Log.e(TAG, "Creation of directory " + path + " on sdcard failed");
                    return;
                } else {
                    Log.i(TAG, "Created directory " + path + " on sdcard");
                }
            }
        }
        if (!(new File(DATA_PATH + ROOT + LANGUAGE + EXTENSION)).exists()) {
            try {
                AssetManager assetManager = context.getAssets();
                InputStream inputStream = assetManager.open(ROOT + LANGUAGE + EXTENSION);
                OutputStream outputStream = new FileOutputStream(DATA_PATH + ROOT + LANGUAGE + EXTENSION);
                byte[] buffer = new byte[1024];
                int length;
                Log.i(TAG, "Copying " + LANGUAGE + " traineddata");
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();
                Log.i(TAG, "Copied " + LANGUAGE + " traineddata");
            } catch (IOException e) {
                Log.e(TAG, "Was unable to copy " + LANGUAGE + " traineddata " + e.toString());
            }
        }
        this.setDebug(true);
        this.init(DATA_PATH, LANGUAGE);
        this.setVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        Log.i(TAG, "Initialize " + LANGUAGE + " traineddata");
    }
}
