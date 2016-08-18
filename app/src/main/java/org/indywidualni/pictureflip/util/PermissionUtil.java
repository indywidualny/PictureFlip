package org.indywidualni.pictureflip.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import org.indywidualni.pictureflip.Constant;

public abstract class PermissionUtil {

    private static boolean hasPermission(Context context, String permission) {
        int hasPermission = ContextCompat.checkSelfPermission(context, permission);
        return (hasPermission == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasStoragePermission(Context context) {
        return hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestStoragePermission(Activity activity) {
        String[] permissions = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
        ActivityCompat.requestPermissions(activity, permissions, Constant.PERMISSION_REQUEST_STORAGE);
    }

}
