package com.bluefletch.internal.feed.service;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;

import timber.log.Timber;

/**
 * Created by blakebyrnes on 6/6/14.
 */
public class ResizeImageAsyncTask extends AsyncTask<Uri, Void, String> {
    private ContentResolver mContentResolver;

    public ResizeImageAsyncTask(ContentResolver resolver) {
        mContentResolver= resolver;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        try
        {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContentResolver,  uris[0]);

            File destinationFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "image.jpg");
            Timber.d("the destination for image file is: %s", destinationFile.getAbsolutePath());
            FileOutputStream out = new FileOutputStream(destinationFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
            out.flush();
            out.close();
            return destinationFile.getAbsolutePath();
        }
        catch (Exception e) {
            Timber.e("ERROR resizing image: %s", e.getLocalizedMessage());
        }
        return null;
    }
}
