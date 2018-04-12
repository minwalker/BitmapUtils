package com.ice.bitmaputils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.TextUtils;
import android.util.Log;

import com.ice.bitmaputils.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by minwalker on 2018/2/7.
 */

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    public static final int TYPE_JPEG = 0;

    public static final int TYPE_PNG = 1;

    public static final int TYPE_WEBP = 2;

    /**
     * method for decode Bitmap into File(in type JPEG,PNG or WEBP)
     * @param bitmap deocde orgin bitmap
     * @param path save image file path, no need .jpeg, .png or .webp, the method will add auto
     * @return result in boolean, true is success, false is fail
     * **/
    public static boolean decodeBitmapToFile(Bitmap bitmap, String path, int type) {
        if(bitmap == null || bitmap.isRecycled()) {
            Log.e(TAG,"decodeBitmapToFile fail bitmap is recycled or null");
            return false;
        }

        if(TextUtils.isEmpty(path)) {
            Log.e(TAG,"decodeBitmapToFile fail save path is empty");
            return false;
        }

        Bitmap.CompressFormat format = getDecodeType(type);
        path = formatPath(path,format);

        if(!FileUtils.checkDirExist(path)){
            Log.e(TAG,"decodeBitmapToFile fail save dir is no exit");
            return false;
        }

        File file = new File(path);
        if(file!=null && (!file.isFile() || !file.exists())) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG,"decodeBitmapToFile fail file is no exit or no a file");
                e.printStackTrace();
                return false;
            }
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            boolean result = bitmap.compress(format,100,fos);
            Log.d(TAG,"decodeBitmapToFile result: "+result);
            return result;
        } catch (Exception e) {
            Log.e(TAG,"decodeBitmapToFile fail for exception");
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * @param type support TYPE_PNG, TYPE_JPEG, TYPE_WEBP
     *             but TYPE_WEBP only support version above android 4.0
     * @return decode type for file
     * **/
    private static Bitmap.CompressFormat getDecodeType(int type) {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        switch (type) {
            case TYPE_PNG:
                format = Bitmap.CompressFormat.PNG;
                break;
            case TYPE_WEBP:
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    format = Bitmap.CompressFormat.WEBP;
                } else {
                    Log.e(TAG,"getDecodeType fail for SDK version is low than 4.0");
                }
                break;
            default:
                Log.e(TAG,"getDecodeType fail for not in type");
                break;
        }

        return format;
    }


    /**
     * method auto set the file type by format
     * @param path save file path
     * @param format image save type
     * @return save file path with type
     * **/
    private static String formatPath(String path, Bitmap.CompressFormat format) {
        if(TextUtils.isEmpty(path)) {
            return path;
        }

        switch (format){
            case PNG:
                path = path+".png";
                break;
            case JPEG:
                path = path+".jpg";
                break;
            case WEBP:
                path = path+".webp";
                break;
            default:
                path = path+".jpg";
                break;
        }

        return path;
    }

    /***
     * create scale bitmap for file, useless more memory
     * @param path file path to decode
     * @param width target bitmap width
     * @param height target bitmap height
     * @param options target bitmap decode option, ARGB8888, ARGB4444, RGB565...
     * @return result scale bitmap or null
     * */
    public static Bitmap createScaleBitmapFromFile(String path, int width, int height, BitmapFactory.Options options, boolean scale) {
        if(TextUtils.isEmpty(path)) {
            Log.e(TAG,"createScaleBitmapFromFile fail for path is empty");
            return null;
        }

        if(width<=0 || height<=0) {
            Log.e(TAG,"createScaleBitmapFromFile fail for target width or height is less than 0");
            return null;
        }

        if(options == null) {
            options = new BitmapFactory.Options();
        }

        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);

            int maxSize = Math.max(options.outWidth, options.outHeight);
            int maxTargetSize = Math.max(width, height);
            if(maxSize <= maxTargetSize) {
                options.inSampleSize = 1;
            } else {
                options.inSampleSize = maxSize / maxTargetSize;
            }

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(path, options);
            if(scale) {
                bitmap = createScaleBitmap(bitmap, width, height);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /***
     * create scale bitmap for Resource, useless more memory
     * @param context context of activity or fragment
     * @param resid id of drawable to decode
     * @param width target bitmap width
     * @param height target bitmap height
     * @param options target bitmap decode option, ARGB8888, ARGB4444, RGB565...
     * @return result scale bitmap or null
     * */
    public static Bitmap createScaleBitmapFromRes(Context context, int resid, int width, int height, BitmapFactory.Options options) {
        if(resid <= 0) {
            Log.e(TAG,"createScaleBitmapFromRes fail for resource id is invalid");
            return null;
        }

        if(width<=0 || height<=0) {
            Log.e(TAG,"createScaleBitmapFromRes fail for target width or height is less than 0");
            return null;
        }

        if(options == null) {
            options = new BitmapFactory.Options();
        }

        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),resid,options);

            int maxSize = Math.max(options.outWidth, options.outHeight);
            int maxTargetSize = Math.max(width, height);
            if(maxSize <= maxTargetSize) {
                options.inSampleSize = 1;
            } else {
                options.inSampleSize = maxSize / maxTargetSize;
            }

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeResource(context.getResources(),resid,options);
            bitmap = createScaleBitmap(bitmap,width,height);
            Log.d(TAG,"createScaleBitmapFroRes result width: "+bitmap.getWidth()+" : "+bitmap.getHeight());
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /***
     * create scale bitmap for Uri, useless more memory
     * @param context context of activity or fragment
     * @param uri Uri of bitmap to decode
     * @param width target bitmap width
     * @param height target bitmap height
     * @param options target bitmap decode option, ARGB8888, ARGB4444, RGB565...
     * @return result scale bitmap or null
     * */
    public static Bitmap createScaleBitmapFroUri(Context context, Uri uri, int width, int height, BitmapFactory.Options options) {
        if(uri==null || TextUtils.isEmpty(uri.toString())) {
            Log.e(TAG,"createScaleBitmapFromUri fail for uri is invalid");
            return null;
        }

        if(width<=0 || height<=0) {
            Log.e(TAG,"createScaleBitmapFromUri fail for target width or height is less than 0");
            return null;
        }

        if(options == null) {
            options = new BitmapFactory.Options();
        }

        options.inJustDecodeBounds = true;
        InputStream inStream = null;
        try {
            inStream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inStream,null,options);
            inStream.close();
            inStream = null;

            int maxSize = Math.max(options.outWidth, options.outHeight);
            int maxTargetSize = Math.max(width, height);
            if(maxSize <= maxTargetSize) {
                options.inSampleSize = 1;
            } else {
                options.inSampleSize = maxSize / maxTargetSize;
            }

            options.inJustDecodeBounds = false;
            inStream = context.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(inStream,null,options);
            bitmap = createScaleBitmap(bitmap,width,height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /***
     * create scale bitmap for bytes, useless more memory
     * @param bytes bytes of bitmap to decode
     * @param width target bitmap width
     * @param height target bitmap height
     * @param options target bitmap decode option, ARGB8888, ARGB4444, RGB565...
     * @return result scale bitmap or null
     * */
    public static Bitmap createScaleFromBytes(byte[] bytes, int width, int height, BitmapFactory.Options options, boolean scale) {
        if(bytes==null || bytes.length<=0) {
            Log.e(TAG,"createScaleFromBytes fail for bytes is invalid");
            return null;
        }

        if(width<=0 || height<=0) {
            Log.e(TAG,"createScaleFromBytes fail for target width or height is less than 0");
            return null;
        }

        if(options == null) {
            options = new BitmapFactory.Options();
        }

        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);

            int maxSize = Math.max(options.outWidth, options.outHeight);
            int maxTargetSize = Math.max(width, height);
            if(maxSize <= maxTargetSize) {
                options.inSampleSize = 1;
            } else {
                options.inSampleSize = maxSize / maxTargetSize;
            }

            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
            if(scale) {
                bitmap = createScaleBitmap(bitmap, width, height);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /***
     * create Scale Bitmap with target width and target height
     * @param bitmap origin bimap
     * @param width target bitmap width
     * @param height target bitmap height
     * @return result scale bitmap or origin bitmap(if width or height is same)
     * */
    public static Bitmap createScaleBitmap(Bitmap bitmap, int width, int height){
        if(bitmap==null || bitmap.isRecycled()) {
            Log.e(TAG,"createScaleBitmap fail for bitmap is empty or recycle");
            return null;
        }

        if(width<=0 || height<=0) {
            Log.e(TAG,"createScaleBitmap fail for target width or height is less than 0");
            return null;
        }

        int origin_w = bitmap.getWidth();
        int origin_h = bitmap.getHeight();
        if(origin_h==height && origin_w==width) {
            Log.d(TAG,"createScaleBitmap no need scale return origin bitmap");
            return bitmap;
        }

        Bitmap result = null;
        try {
            result = Bitmap.createScaledBitmap(bitmap, width, height, true);
            if (result != bitmap) {
                bitmap.recycle();
                bitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *method for using Gaussian Blur to decode a bitmap
     * @param context activity or fragment
     * @param bitmap origin bitmap
     * @param radius Gaussian Blur radio
     * @return a bitmap after Gauusian Blur
     * **/
    public static Bitmap blurBitmap(Context context, Bitmap bitmap, float radius) {
        RenderScript renderScript = RenderScript.create(context);
        Allocation input = Allocation.createFromBitmap(renderScript, bitmap);
        Allocation output = Allocation.createTyped(renderScript,input.getType());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript,input.getElement());
        blurScript.setRadius(radius);
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(bitmap);
        input.destroy();
        output.destroy();
        blurScript.destroy();
        renderScript.destroy();
        return bitmap;
    }

    /**
     * a class using for get corner bitmap
     * **/
    public static class CornerBitmapUtils {
        public static final int CORNER_NONE = 0x00;
        public static final int CORNER_LEFT_TOP = 0x01;
        public static final int CORNER_RIGHT_TOP = 0x01 << 1;
        public static final int CORNER_LEFT_BOTTOM = 0x01 << 2;
        public static final int CORNER_RIGHT_BOTTOM = 0x01 << 3;

        public static final int CORNER_ALL = CORNER_LEFT_TOP|CORNER_LEFT_BOTTOM|CORNER_RIGHT_TOP|CORNER_RIGHT_BOTTOM;
        public static final int CORNER_TOP = CORNER_LEFT_TOP|CORNER_RIGHT_TOP;
        public static final int CORNER_BOTTOM = CORNER_LEFT_BOTTOM|CORNER_RIGHT_BOTTOM;
        public static final int CORNER_LEFT = CORNER_LEFT_TOP|CORNER_LEFT_BOTTOM;
        public static final int CORNER_RIGHT = CORNER_RIGHT_TOP|CORNER_RIGHT_BOTTOM;

        /**
         * get a bitmap with corner
         * @param bitmap origin bitmap
         * @param radius radius or corner
         * @param type corner type with CORNER_LEFT_TOP,CORNER_LEFT_BOTTOM,CORNER_RIGHT_TOP...
         * @return origin bitmap or corner bitmap
         * **/
        public static Bitmap getCornerBitmap(Bitmap bitmap, int radius, int type) {
            if(type == CORNER_NONE) {
                Log.d(TAG, "getCornerBitmap corner type is none return origin");
                return bitmap;
            }

            if(bitmap == null || bitmap.isRecycled()) {
                Log.e(TAG, "getCornerBitmap fail for bitmap is null or recycle");
                return null;
            }
            try {
                final int origin_w = bitmap.getWidth();
                final int origin_h = bitmap.getHeight();

                Bitmap result = Bitmap.createBitmap(origin_w,origin_h,bitmap.getConfig());
                Canvas canvas = new Canvas(result);
                canvas.drawColor(Color.TRANSPARENT);

                final Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.BLUE);

                final RectF rectF = new RectF(0,0,origin_w,origin_h);
                canvas.drawRoundRect(rectF, radius, radius, paint);

                if(type != CORNER_ALL) {
                    clipRect(canvas, paint, radius, origin_w, origin_h, type);
                }

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                final Rect rect = new Rect(0,0,origin_w,origin_h);
                canvas.drawBitmap(bitmap,rect,rect,paint);
                if(result!=bitmap) {
                    bitmap.recycle();
                    bitmap = null;
                }
                return result;
            } catch (Exception e) {
                Log.e(TAG, "getCornerBitmap fail for exception");
                e.printStackTrace();
                return bitmap;
            }
        }

        /**
         * method for clip no need corner
         * @param canvas origin bitmap canvas
         * @param paint paint for draw rect
         * @param radius corner's radius
         * @param width origin bitmap's width
         * @param height origin bitmap's height
         * @param type corner's type
         * **/
        private static void clipRect(final Canvas canvas, final Paint paint, int radius, int width, int height, int type) {
            final Rect rect = new Rect(0,0,0,0);
           int no_type = type ^ CORNER_ALL;
           if((no_type & CORNER_LEFT_TOP) != 0) {
                rect.set(0,0,radius,radius);
                canvas.drawRect(rect,paint);
           }

           if((no_type & CORNER_RIGHT_TOP) != 0) {
               rect.set(width - radius,0, width, radius);
               canvas.drawRect(rect,paint);
           }

           if((no_type & CORNER_LEFT_BOTTOM) != 0) {
               rect.set(0,height - radius, radius, height);
               canvas.drawRect(rect,paint);
           }

           if((no_type & CORNER_RIGHT_BOTTOM) != 0) {
               rect.set(width - radius,height - radius, width, height);
               canvas.drawRect(rect,paint);
           }
        }

        /**
         * method for get a oval bitmap
         * @param bitmap origin bitmap
         * @return oval bitmap
         * **/
        public static Bitmap getOvalBitmap(Bitmap bitmap){
            if(bitmap == null || bitmap.isRecycled()) {
                Log.e(TAG, "getOvalBitmap fail for bitmap is null or recycle");
                return null;
            }

            final int origin_w = bitmap.getWidth();
            final int origin_h = bitmap.getHeight();

            try {
                Bitmap result = Bitmap.createBitmap(origin_w, origin_h, bitmap.getConfig());
                Canvas canvas = new Canvas(result);

                canvas.drawColor(Color.TRANSPARENT);

                final Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.BLUE);

                final Rect rect = new Rect(0, 0, origin_w, origin_h);
                final RectF rectF = new RectF(0, 0, origin_w, origin_h);
                canvas.drawOval(rectF, paint);

                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bitmap, rect, rect, paint);
                if (result != bitmap) {
                    bitmap.recycle();
                    bitmap = null;
                }
                return result;
            } catch (Exception e) {
                Log.e(TAG, "getOvalBitmap fail for exception");
                e.printStackTrace();
            }

            return bitmap;
        }

        /**
         * method for get a reflected bitmap of half
         * @param bitmap origin bitmap
         * @param gap gap between origin bitmap and reflected bitmap
         * @return bitmap after reflected
         * **/
        public static Bitmap getReflectedBitmap(Context context,Bitmap bitmap, int gap) {

            if(bitmap == null || bitmap.isRecycled()) {
                Log.e(TAG, "getReflectedBitmap fail for bitmap is null or recycled");
                return null;
            }

            final int origin_w = bitmap.getWidth();
            final int origin_h = bitmap.getHeight();

            try {
                final Matrix matrix = new Matrix();
                matrix.preScale(1, -1);

                Bitmap refectBitmap =
                        Bitmap.createBitmap(bitmap, 0, origin_h / 2, origin_w, origin_h / 2, matrix, false);
                Bitmap result = Bitmap.createBitmap(origin_w, origin_h + gap + origin_h / 2, bitmap.getConfig());

                final Canvas canvas = new Canvas(result);
                final Paint paint = new Paint();
                paint.setAntiAlias(true);

                canvas.drawBitmap(bitmap, 0, 0, paint);
                canvas.drawBitmap(refectBitmap, 0, origin_h + gap, paint);

                final LinearGradient shader = new LinearGradient(0, origin_h, 0, refectBitmap.getHeight(),
                        context.getColor(R.color.begin_color), context.getColor(R.color.end_color), Shader.TileMode.MIRROR);
                paint.setShader(shader);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                canvas.drawRect(0, origin_h + gap, origin_w, result.getHeight(), paint);

                if (refectBitmap != result && !refectBitmap.isRecycled()) {
                    refectBitmap.recycle();
                    refectBitmap = null;
                }

                if (bitmap != result && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

    }

}
