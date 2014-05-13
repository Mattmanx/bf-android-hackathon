package com.bluefletch.internal.feed;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

/**
 * Created by grantstevens on 4/23/14.
 */
public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    Context theContext;

    public DownloadImageTask(Context context, ImageView bmImage) {
        this.theContext = context;
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(in), dpToPx(bmImage.getWidth()), dpToPx(bmImage.getHeight()), true);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {

        bmImage.setImageBitmap(result);
    }

    private int dpToPx(int dp)
    {
        float density = theContext.getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
}