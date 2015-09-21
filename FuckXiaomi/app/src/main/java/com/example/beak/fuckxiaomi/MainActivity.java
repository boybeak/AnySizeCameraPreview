package com.example.beak.fuckxiaomi;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.beak.cameralib.AutoCameraView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ResizeFragment.OnSizeNeedChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SAVING_ROOT = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() +
            File.separator + "anySizePreviewSavingBitmap";

    private AutoCameraView mAutoView = null;
    private Button mMainPreviewSizeChangeBtn = null, mMainSaveBtn;

    private ResizeFragment mResizeFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final int width = getResources().getDisplayMetrics().widthPixels;
        final int height = getResources().getDisplayMetrics().heightPixels;

        final int size = Math.min(width, height);

        mAutoView = (AutoCameraView)findViewById(R.id.main_camera_preview);
        mAutoView.setLayoutParams(new RelativeLayout.LayoutParams(size, size));

        try {
            mAutoView.startPreview(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mMainPreviewSizeChangeBtn = (Button)findViewById(R.id.main_change_size);
        mMainSaveBtn = (Button)findViewById(R.id.main_take_pic);

        mMainPreviewSizeChangeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment();
            }
        });
        mMainSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoView.takePicture(new AutoCameraView.OnPictureTakenCallback() {
                    @Override
                    public void onPictureTaken(byte[] oriData, Bitmap croppedBmp) {
                        Toast.makeText(MainActivity.this, "now saving 3 bitmaps start ..", Toast.LENGTH_SHORT).show();
                        Bitmap oriBmp = BitmapFactory.decodeByteArray(oriData, 0, oriData.length);
                        new BitmapSaveTask(MainActivity.this, oriBmp).execute(SAVING_ROOT + File.separator + "bmp_by_oriData.png");

                        new BitmapSaveTask(MainActivity.this, croppedBmp).execute(SAVING_ROOT + File.separator + "bmp_by_croppedBmp.png");

                        new BitmapSaveTask(MainActivity.this, mAutoView.getBitmap()).execute(SAVING_ROOT + File.separator + "bmp_by_getBitmap.png");
                    }
                });
            }
        });
    }

    private void showFragment () {
        if (mResizeFragment == null) {
            mResizeFragment = new ResizeFragment();
            mResizeFragment.setOnSizeNeedChangeListener(this);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (mResizeFragment.isAdded()) {
            ft.show(mResizeFragment);
        } else {
            ft.add(R.id.main_fragment_container, mResizeFragment);
        }
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onNeedChange(int width, int height) {
        mAutoView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
    }
}
