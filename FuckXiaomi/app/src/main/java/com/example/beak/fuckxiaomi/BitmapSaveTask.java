package com.example.beak.fuckxiaomi;

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

    public BitmapSaveTask (Context context, Bitmap bmp) {
        mContext = context;
        mBmp = bmp;
    }

    @Override
    protected String doInBackground(String... params) {
        if (mBmp != null) {
            File file = new File(params[0]);
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
    }
}