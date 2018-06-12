package com.chinamobile.update;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by liangzhongtai on 2018/6/4.
 */

public class UpdateUtil {
    private volatile static UpdateUtil uniqueInstance;
    public UpdateApp.UpdateListener listener;
    public static String FILEPROVIDER  = ".provider";

    private UpdateUtil() {
    }

    //采用Double CheckLock(DCL)实现单例
    public static UpdateUtil getInstance() {
        if (uniqueInstance == null) {
            synchronized (UpdateUtil.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new UpdateUtil();
                }
            }
        }
        return uniqueInstance;
    }

    public static int getResId(Context context, String variableName, String resFileName){
        int resId = context.getResources().getIdentifier(variableName, resFileName, context.getPackageName());
        return resId;
    }

    /**
     * 获取App应用图标 bitmap
     * @param context
     */
    public static Bitmap getAppIconBitmap(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap bm = bd.getBitmap();
        return bm;
    }

    /**
     * 网络是否可用
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
