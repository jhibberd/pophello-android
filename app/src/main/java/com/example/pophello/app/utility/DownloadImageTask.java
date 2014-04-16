package com.example.pophello.app.utility;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

// TODO: maybe improve with http://stackoverflow.com/questions/6023557/how-can-i-specify-a-placeholder-image
/**
 * Asynchronously load an image from a URL.
 * http://stackoverflow.com/questions/5776851/load-image-from-url
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "DownloadImageTask";
    private final ImageView mImageView;

    public DownloadImageTask(ImageView mImageView) {
        this.mImageView = mImageView;
    }

    protected Bitmap doInBackground(String... urls) {
        String url = urls[0];
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        if (result != null) {
            result = ImageHelper.getRoundedCornerBitmap(result, mImageView.getWidth() / 2);
            mImageView.setImageBitmap(result);
        }
    }
}