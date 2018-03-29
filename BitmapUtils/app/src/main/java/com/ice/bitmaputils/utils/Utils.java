package com.ice.bitmaputils.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by minwalker on 2018/3/26.
 */

public class Utils {

    private static final String TAG = "Utils";

    /**
     * get the result whether a apk has install
     * @param mPackageManager PackageManager
     * @param name packageName of the apk
     * @param name versionName of the apk
     * @return ture if has installed, otherwise false
     * **/
    public static boolean isAppInstalled(PackageManager mPackageManager, String name, String version) {
        try {
            if (mPackageManager != null) {
                PackageInfo info = mPackageManager.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
                Log.d(TAG, "isAppInstall version: "+version);
                if(info!=null && !TextUtils.isEmpty(version)) {
                    boolean same_version = version.equals(info.versionName);
                    Log.d(TAG, "isAppInstall same_version: "+same_version);
                    return same_version;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

}
