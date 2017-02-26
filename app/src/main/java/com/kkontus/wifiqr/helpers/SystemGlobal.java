package com.kkontus.wifiqr.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class SystemGlobal {

    public void hideKeyboard(Activity activity) {
        if (activity == null || activity.getCurrentFocus() == null || activity.getCurrentFocus().getWindowToken() == null)
            return;

        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void hideKeyboard(Fragment fragment) {
        if (fragment == null || fragment.getActivity() == null || fragment.getActivity().getCurrentFocus() == null || fragment.getActivity().getCurrentFocus().getWindowToken() == null)
            return;

        InputMethodManager inputManager = (InputMethodManager) fragment.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(fragment.getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean handleSavingImage(Context context, Bitmap bitmap, String filename) {
        boolean isSaved;
        // check permissions to write every time, also add saving to internal memory
        if (isExternalStorageReadable() && isExternalStorageWritable()) {
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String filenameUnique = n + filename;
            isSaved = saveImageToExternalStorage(context, bitmap, filenameUnique);
        } else {
            System.out.println("Storage not writable");
            isSaved = false;
        }

        return isSaved;
    }

    private boolean saveImageToExternalStorage(Context context, Bitmap bitmap, String filename) {
        boolean isSaved;
        String root = Environment.getExternalStorageDirectory().toString();
        File directory = new File(root + Config.SAVE_IMAGE_FOLDER);
        directory.mkdirs();

        File file = new File(directory, filename);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            isSaved = true;
        } catch (Exception e) {
            e.printStackTrace();
            isSaved = false;
        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

        return isSaved;
    }

}
