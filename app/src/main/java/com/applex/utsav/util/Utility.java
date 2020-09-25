package com.applex.utsav.util;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class Utility {

//    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 400;
    private static final String TAG = "SAVE_IMAGE_BITMAP_NOT_WORKING";
    public static Long tsLong;
//    private String[] cameraPermission;
    /**
     * CHECK WHETHER INTERNET CONNECTION IS AVAILABLE OR NOT
     */

    public static void showToast(Context context, String data) {
        Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
    }

    public static Boolean saveImage(Bitmap finalBitmap, Context context) {
        File myDir = new File(context.getExternalFilesDir(null), "/Utsav");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        tsLong = System.currentTimeMillis();
        try {
            File file = new File(myDir, "IMG-" + tsLong + ".jpg");
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            MediaStore.Images.Media.insertImage(context.getContentResolver(), finalBitmap, "IMG-" + tsLong + ".jpg" , "Downloaded from Utsav");
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Boolean saveVideo(Uri uri, Context context) {
        File myDir = new File(context.getExternalFilesDir(null), "/Utsav");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        tsLong = System.currentTimeMillis();
        InputStream in;
        OutputStream out;
        try {
            Log.i("NONGRAMI", "BAR KORE DEBO");
            File file = new File(myDir, "VID-" + tsLong + ".mp4");
            file.createNewFile();
            in = context.getContentResolver().openInputStream(uri);
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            if (in != null) {
                while((len = in.read(buf))>0) {
                    out.write(buf,0,len);
                }
            }
            out.close();
            Objects.requireNonNull(in).close();
            return true;
        } catch (IOException e) {
            Log.i("SHUTIYE", "LAL KORE DEBO");
            e.printStackTrace();
            return false;
        }
    }

    public static boolean downloadVideo(String url, Context context) {
        File myDir = new File(context.getExternalFilesDir(null), "/Utsav");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        tsLong = System.currentTimeMillis();
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setDescription("download");
            request.setTitle("VID-" + tsLong + ".mp4");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(context.getExternalFilesDir(null) + "/Utsav", "VID-" + tsLong + ".mp4");

            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);
            return true;

        } catch (Exception e) {
            Log.i("SHUTIYE", "LAL KORE DEBO");
            e.printStackTrace();
            return false;
        }

    }

    public static String getTimeAgo(Long timestamp) {
        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;
        double time = 0.0;
        try {
            Double a= Double.valueOf(timestamp);
            time = Math.round(a);
        }catch (NumberFormatException e){
            System.out.println(e.getMessage());
        }

        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }
        // TODO: localize
        final long diff = (long) (now - time);
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a min ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " mins ago";
        } else if (diff < 120 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    //612, 816
    public static Bitmap decodeSampledBitmapFromFile(Bitmap bitmap, int reqWidth, int reqHeight) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Objects.requireNonNull(bitmap).compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] bytes = out.toByteArray();

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String getFittingString(String s, int length) {
        if (s.length() > length) {
            s = s.substring(0, length) + "\u2026";
        }
        return s;
    }

    public static void hideKeyboard(Context context, ConstraintLayout layout) {
        InputMethodManager imm = (InputMethodManager)context.getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
    }

    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(50);
        }
    }

    public static void requestStoragePermission(Context mContext) {
        String[] storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions((Activity) mContext, storagePermission, STORAGE_REQUEST_CODE);
    }

    public static boolean checkStoragePermission(Context mContext) {
        return ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

}
