package com.ehi.videostudydemo.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author: 37745 <a href="ziju.wang@1hai.cn">Contact me.</a>
 * @date: 2021/2/2 15:19
 * @desc:
 */
public class FileUtils {

    @TargetApi(Build.VERSION_CODES.Q)
    public static boolean isFileExistAboveQ(Context mContext, String fileUri) {
        boolean isFind = true;
        try {
            ParcelFileDescriptor pfd = mContext.getContentResolver().openFileDescriptor(Uri.parse(fileUri), "r");
        } catch (FileNotFoundException e) {
            isFind = false;
        }
        return isFind;
    }

    public static boolean isFileExistBelowQ(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isFileExist(Context mContext, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return isFileExistAboveQ(mContext, filePath);
        }
        return isFileExistBelowQ(filePath);
    }
}
