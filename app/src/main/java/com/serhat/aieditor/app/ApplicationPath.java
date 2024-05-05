package com.serhat.aieditor.app;

import android.os.Build;
import android.os.Environment;

import java.io.File;

public class ApplicationPath {

    public static String savePath() {
        return getQuantumPath("AI Editor Saved", Environment.DIRECTORY_PICTURES);
    }

    public static String upscalePath() {
        return getQuantumPath("AI Editor Upscaled", Environment.DIRECTORY_PICTURES);
    }


    public static String getQuantumPath(String folderName, String dir) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            File result = new File(Environment.getExternalStoragePublicDirectory(dir),
                    folderName);
            if (result.isDirectory() || result.mkdirs()) {
                return result.getPath();
            }
            else {
                return (Environment.getExternalStoragePublicDirectory(dir).getPath());
            }
        } else {
            if (App.getInstance().isStoragePermissionGranted()) {
                File result = new File(Environment.getExternalStoragePublicDirectory(dir),
                        folderName);
                if (result.isDirectory() || result.mkdirs()) {
                    return result.getPath();
                } else {
                    return (Environment.getExternalStoragePublicDirectory(dir).getPath());
                }
            } else {
                return App.getInstance().getExternalFilesDir(folderName).getPath();
            }
        }
    }
}
