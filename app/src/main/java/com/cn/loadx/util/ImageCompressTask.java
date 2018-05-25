package com.cn.loadx.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.cn.loadx.interfaces.ImageCompressListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import id.zelory.compressor.Compressor;

/**
 * Created by Admin on 01-03-2018.
 */

public class ImageCompressTask extends AsyncTask<File,String,File> {
ImageCompressListener compressListener;
String imageCategory;
    public ImageCompressTask(String category, ImageCompressListener imageCompressListener) {
        compressListener = imageCompressListener;
        imageCategory = category;
    }

    @Override
    protected File doInBackground(File... files) {
        return getCompressedImg(files[0]);
    }

    private File getCompressedImg(File actualImage) {
        File compressedImage = null;
        try {
            // actualImage = FileUtil.from(this, data.getData());
            if(actualImage!=null) {
                compressedImage = new Compressor(LoadXApplication.getAppContext())
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);
               /* Bitmap bmp = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);*/
                Log.d(ApplicationUtil.APPTAG, getReadableFileSize(compressedImage.length()) + "  size");
            }
        } catch (Exception e) {
            compressedImage = actualImage;
            //Toast.makeText(UserProfileActivity.this, "Failed to read Image", Toast.LENGTH_SHORT).show();
            Log.d(ApplicationUtil.APPTAG,"Compress Ex "+e);
            e.printStackTrace();
        }

        return compressedImage;
    }

    @Override
    protected void onPostExecute(File s) {
        super.onPostExecute(s);
        compressListener.onImageCompressed(imageCategory,s);
    }
    private File getImgByteString(File actualImage) {
        String encodedString = null;
        File compressedImage = null;
        try {
            // actualImage = FileUtil.from(this, data.getData());
            if(actualImage!=null) {
                compressedImage = new Compressor(LoadXApplication.getAppContext())
                        .setMaxWidth(640)
                        .setMaxHeight(480)
                        .setQuality(75)
                        .setCompressFormat(Bitmap.CompressFormat.WEBP)
                        .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES).getAbsolutePath())
                        .compressToFile(actualImage);
               /* Bitmap bmp = BitmapFactory.decodeFile(compressedImage.getAbsolutePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                encodedString = Base64.encodeToString(byteArray, Base64.DEFAULT);*/
                Log.d(ApplicationUtil.APPTAG, getReadableFileSize(compressedImage.length()) + "  size");
            }
        } catch (IOException e) {
            compressedImage = actualImage;
            //Toast.makeText(UserProfileActivity.this, "Failed to read Image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return compressedImage;

    }
    public String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
