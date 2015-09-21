package com.beak.cameralib;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.List;

/**
 * Created by Beak on 2015/9/17.
 */
public class AutoCameraView extends TextureView implements TextureView.SurfaceTextureListener {

    private static final String TAG = AutoCameraView.class.getSimpleName();

    private int mCameraId = 0;
    private Camera mCamera = null;

    private int mOrientation = 0;

    private boolean hasCamera = false;

    private boolean previewWhenAvailable = false, isPreviewing = false;

    public AutoCameraView(Context context) {
        this(context, null);
    }

    public AutoCameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoCameraView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initThis(context);
    }

    private void initThis (Context context) {
        hasCamera = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        if (hasCamera) {
            super.setSurfaceTextureListener(this);
        }
    }

    public boolean isCameraIdValid (int cameraId) {
        return cameraId < Camera.getNumberOfCameras();
    }

    public Camera getCamera () {
        return mCamera;
    }

    private void fixOrientation () {
        if (mCamera == null) {
            return;
        }
        if (getContext() instanceof Activity) {
            Activity activity = (Activity)getContext();
            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(mCameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);
            mOrientation = result;
        }
    }

    private boolean shouldRotate () {
        return mOrientation % 90 == 0 && mOrientation % 180 != 0;
    }

    public void startPreview (int cameraId) throws IOException {
        if (!hasCamera) {
            throw new IllegalStateException("no camera found on this device");
        }
        if (!isCameraIdValid(cameraId)) {
            throw new IllegalArgumentException("the cameraId-" + cameraId + " is Invalid, camera count is " + Camera.getNumberOfCameras());
        }

        if (!isAvailable()) {
            previewWhenAvailable = true;
            isPreviewing = false;
            return;
        }
        if (mCamera == null) {
            mCamera = Camera.open(cameraId);
        }
        Camera.Size size = findBestPreviewSize();
        if (size != null) {
            Log.v(TAG, "best size width=" + size.width + " height=" + size.height);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(size.width, size.height);
            mCamera.setParameters(parameters);
        }
        fixOrientation();
        fixPreviewFrame();
        mCamera.setPreviewTexture(getSurfaceTexture());
        mCamera.startPreview();

        isPreviewing = true;
        mCameraId = cameraId;
    }

    public void stopPreview () {
        if (mCamera == null) {
            return;
        }
        mCamera.stopPreview();

        isPreviewing = false;
        previewWhenAvailable = false;
    }

    public void release () {
        if (mCamera == null) {
            return;
        }
        mCamera.release();
        mCamera = null;

        isPreviewing = false;
        previewWhenAvailable = false;
    }

    private Camera.Size findBestPreviewSize () {
        if (mCamera == null) {
            return null;
        }
        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
        if (sizeList == null || sizeList.isEmpty()) {
            return null;
        }
        final int viewWid = getWidth();
        final int viewHei = getHeight();
        final int viewArea = viewWid * viewHei;
        final int length = sizeList.size();

        Log.v(TAG, "findBestPreviewSize viewWid=" + viewWid + " viewHei=" + viewHei);

        Camera.Size resultSize = null;
        int deltaArea = 0;
        for (int i = 0; i < length; i++) {
            Camera.Size size = sizeList.get(i);
            final int area = size.width * size.height;
            final int delta = Math.abs(area - viewArea);
            if (deltaArea == 0 || delta < deltaArea) {
                deltaArea = delta;
                resultSize = size;
            }
        }
        return resultSize;
    }

    private void fixPreviewFrame () {
        if (hasCamera && mCamera != null) {

            final int viewWid = getWidth();
            final int viewHei = getHeight();

            if (viewWid == 0 || viewHei == 0) {
                return;
            }

            Matrix matrix = getMatrix();
            Camera.Size preSize = mCamera.getParameters().getPreviewSize();
            final int previewWid = shouldRotate() ? preSize.height : preSize.width;
            final int previewHei = shouldRotate() ? preSize.width : preSize.height;
            float scaleWid = (float) previewWid / viewWid;
            float scaleHei = (float)previewHei / viewHei;

            if (scaleWid < 1) {
                final float secondScale = 1f / scaleWid;
                scaleWid = 1;
                scaleHei *= secondScale;
            }
            if (scaleHei < 1) {
                final float secondScale = 1f / scaleWid;
                scaleHei = 1;
                scaleWid *= secondScale;
            }

            Log.v(TAG, "fixPreviewFrame previewWid=" + previewWid + " previewHei=" + previewHei);
            Log.v(TAG, "fixPreviewFrame viewWid=" + viewWid + " viewHei=" + viewHei);
            Log.v(TAG, "fixPreviewFrame scaleWid " + scaleWid + " scaleHei=" + scaleHei);


            matrix.setScale(scaleWid, scaleHei, viewWid / 2, viewHei / 2);
            super.setTransform(matrix);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (previewWhenAvailable && !isPreviewing) {
            try {
                startPreview(mCameraId);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        /*if (mCamera != null) {
            mCamera.stopPreview();
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size size = findBestPreviewSize();
            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
            }
            mCamera.setParameters(parameters);
        }*/
        Log.v(TAG, "onSurfaceTextureSizeChanged width=" + width + " height=" + height + " viewWid=" + getWidth() + " viewHei=" + getHeight());
        stopPreview();
        try {
            startPreview(mCameraId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopPreview();
        release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    public void takePicture (final OnPictureTakenCallback callback) {
        if (mCamera != null && isPreviewing) {
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    mCamera.startPreview();
                    final Camera.Size picSize = camera.getParameters().getPictureSize();

                    final int viewWid = getWidth();
                    final int viewHei = getHeight();

                    final int picWid = /*shouldRotate() ? picSize.height : */picSize.width;
                    final int picHei = /*shouldRotate() ? picSize.width : */picSize.height;

                    float scale = Math.min((float) picWid / viewWid, (float) picHei / viewHei);

                    final int destWid = (int)((shouldRotate() ? viewHei : viewWid) * scale);
                    final int destHei = (int)((shouldRotate() ? viewWid : viewHei) * scale);

                    if (callback != null) {
                        callback.onPictureTaken(data, ThumbnailUtils.extractThumbnail(BitmapFactory.decodeByteArray(data, 0, data.length), destWid, destHei, ThumbnailUtils.OPTIONS_RECYCLE_INPUT));
                    }
                    /*final Camera.Size preSize = camera.getParameters().getPreviewSize();
                    if (callback != null) {
                        callback.onPictureTaken(data, camera);
                    }
                    Log.v(TAG, "takePicture data.length=" + data.length + " pic.width=" + picSize.width + " pic.height=" + picSize.height + " real=" + (picSize.width * picSize.height));
                    Log.v(TAG, "takePicture data.length=" + data.length + " pre.width=" + preSize.width + " pre.height=" + preSize.height + " pre.real=" + (preSize.width * preSize.height));*/
                    //BitmapFactory.decodeByteArray()
                }
            });
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        /*super.setSurfaceTexture(surfaceTexture);*/
    }

    @Override
    public void setTransform(Matrix transform) {
        /*super.setTransform(transform);*/
    }

    public interface OnPictureTakenCallback {
        public void onPictureTaken (byte[] oriData, Bitmap croppedBmp);
    }
}
