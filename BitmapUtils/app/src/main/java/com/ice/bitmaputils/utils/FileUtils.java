package com.ice.bitmaputils.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.ice.bitmaputils.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by minwalker on 2018/3/10.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    /**
     * method for check dir exist or no, if no try to create it
     * @param path origin file path
     * @return whether has this dir
     * **/
    public static boolean checkDirExist(String path){
        if(TextUtils.isEmpty(path)) {
            return false;
        }

        try {
            String dir = path.substring(0, path.lastIndexOf('/') + 1);
            Log.d(TAG, "checkDirExist dir path: " + dir);
            File file = new File(dir);
            if(!file.exists()){
                return file.mkdirs();
            } else {
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "checkDirExist fail for exception");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * method to save the apk with origin source bytes
     * @param path file path to save apk
     * @param sources bytes source of the save apk
     * @return result whether the apk has saved
     * **/
    public static boolean saveApk(String path, byte[] sources) {
        if(!checkDirExist(path)) {
            Log.e(TAG, "save apk found dir is no exit");
            return false;
        }

        File file = new File(path);
        if(file!=null && (!file.isFile() || !file.exists())) {
            try {
                boolean result = file.createNewFile();
                Log.d(TAG,"save apk file create new result: "+result);
            } catch (IOException e) {
                Log.e(TAG,"save apk file create fail for exception");
                e.printStackTrace();
                return false;
            }
        } else if(file!=null && file.exists()) {
            Log.d(TAG,"save apk file already exit ");
            return true;
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(sources);
            return true;
        } catch (Exception e) {
            Log.d(TAG,"save apk fail for exception");
            e.printStackTrace();
        } finally {
            try {
                if(bos!=null) {
                    bos.close();
                }

                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public static boolean isApkExist(Context context, String app_url){
        StringBuilder pathBuild = new StringBuilder();
        pathBuild.append(context.getResources().getString(R.string.download_default_path));
        pathBuild.append(app_url.substring(app_url.lastIndexOf('/')+1,app_url.length()));
        String apkPath = pathBuild.toString();
        File file = new File(apkPath);
        return file.exists();
    }

    public static String ParseMd5(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(data.getBytes());
        StringBuffer buf = new StringBuffer();
        byte[] bits = md.digest();
        for(int i= 0;i<bits.length;i++) {
            int a = bits[i];
            if(a<0) a+=256;
            if(a<16) buf.append("0");
            buf.append(Integer.toHexString(a));
        }
        return buf.toString();
    }

}
