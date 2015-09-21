package com.example.beak.fuckxiaomi;

import android.app.DownloadManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapSaveTask extends AsyncTask<String, Integer, String> {

    private Bitmap mBmp = null;
    private Context mContext = null;
    private DownloadManager mDownloadManager = null;

    public BitmapSaveTask (Context context, Bitmap bmp) {
        mContext = context;
        mBmp = bmp;
        mDownloadManager = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    @Override
    protected String doInBackground(String... params) {
        if (mBmp != null) {
            File file = new File(params[0]);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                mBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mBmp.recycle();

            }
        }
        return params[0];
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        File file = new File(s);
        mDownloadManager.addCompletedDownload(file.getName(), file.getAbsolutePath(), true, "image/*", file.getAbsolutePath(), file.length(), true);
    }
}