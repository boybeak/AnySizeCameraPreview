package com.example.beak.fuckxiaomi;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.beak.cameralib.AutoCameraView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements ResizeFragment.OnSizeNeedChangeListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private AutoCameraView mAutoView = null;
    private Button mMainPreviewSizeChange = null;

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

        mMainPreviewSizeChange = (Button)findViewById(R.id.main_change_size);

        mMainPreviewSizeChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment();
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
