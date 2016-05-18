package epu.d7cnpm.ewc.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import epu.d7cnpm.ewc.R;
import epu.d7cnpm.ewc.core.TessOcr;
import epu.d7cnpm.ewc.dialog.ProgressDialogFragment;

public class ElectricFragment extends Fragment {

    public static final String TAG = ElectricFragment.class.getSimpleName();

    protected static final String PHOTO_TAKEN = "photo_taken";

    private Handler handler = new Handler();
    private Button button;
    private ImageView imageView;
    private EditText editText;

    private String path;
    private Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_electric, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        path = TessOcr.PATH;
        createSubViews(view);
    }

    private void createSubViews(View view) {
        imageView = (ImageView) view.findViewById(R.id.imageView);
        editText = (EditText) view.findViewById(R.id.editText);
        button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraActivity();
            }
        });
    }

    private void startCameraActivity() {
        File file = new File(path);
        Uri outputFileUri = Uri.fromFile(file);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            getBitmapFromCamera();
//            preProcessingBitmap();
//            detectText();
            detect();
            imageView.setImageBitmap(this.bitmap);
            handleBitmapToText();
        } else {
        }
    }

    protected void getBitmapFromCamera() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        this.bitmap = BitmapFactory.decodeFile(path, options);
        try {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotate = 0;
            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }
            if (rotate != 0) {
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                Matrix mtx = new Matrix();
                mtx.preRotate(rotate);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void preProcessingBitmap() {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mat, mat, 100.0, 255.0, Imgproc.THRESH_BINARY);
        Utils.matToBitmap(mat, bitmap);
    }

    private void detectText() {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Utils.matToBitmap(mat, bitmap);
    }

    private void detect() {
        Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);
        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_GRADIENT, morphKernel);
        Imgproc.threshold(mat, mat, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Utils.matToBitmap(mat, bitmap);
    }

    private String text = "";

    private void handleBitmapToText() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TessOcr.getInstance().setImage(bitmap);
                text = TessOcr.getInstance().getUTF8Text();
                Log.i(TAG, "ocr utf8 text: " + text);
                if (TessOcr.LANGUAGE.equalsIgnoreCase("eng")) {
                    text = text.replaceAll("[^a-zA-Z0-9]+", " ");
                    text = text.replace("  ", " ");
                }
                text = text.trim();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (text.length() != 0) {
                            editText.setText(text);
                        }
                    }
                });
            }
        }).start();
    }
}
