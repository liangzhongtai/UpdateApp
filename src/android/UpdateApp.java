package com.chinamobile.update;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;



import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;



/**
 * Created by liangzhongtai on 2018/5/17.
 */

public class UpdateApp extends CordovaPlugin{
    public final static String TAG = "UpdateApp_Plugin";
    public final static int RESULTCODE_PERMISSION = 20;

    public final static int UPDATE_DEFAULT = 0;

    //网络不可用
    public final static int NET_NOT_AVAILABLE = 1;
    //app下载失败
    public final static int DOWLOAD_FAILE     = 2;
    //app下载中断
    public final static int DOWLOAD_BREAK_OFF = 3;
    //app下载成功
    public final static int DOWNLOAD_SUCCESS  = 4;

    //app的下载链接url
    public String url;
    public String fileDir;
    public String sdApkName;
    public String notiTitle;
    public CordovaInterface cordova;
    public CordovaWebView webView;
    public boolean first = true;
    private CallbackContext callbackContext;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
        this.webView = webView;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        //Log.d(TAG,"执行方法updateapp");
        if("coolMethod".equals(action)){
            url = args.getString(0);
            if(args.length()>1)
            fileDir   = args.getString(1);
            if(args.length()>2)
            sdApkName = args.getString(2);
            if(args.length()>3)
            notiTitle = args.getString(3);
            //Log.d(TAG,"url="+url);
            //Log.d(TAG,"sdApkName="+sdApkName);
            //Log.d(TAG,"notiTitle="+notiTitle);
            //权限
            try {
                if(!PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                   ||!PermissionHelper.hasPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                   ||!PermissionHelper.hasPermission(this,Manifest.permission.ACCESS_NETWORK_STATE)) {
                    PermissionHelper.requestPermissions(this,RESULTCODE_PERMISSION,new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_NETWORK_STATE
                    });
                }else{
                    startWork();
                }
            }catch (Exception e){
                //权限异常
                callbackContext.error("照相机功能异常");
                return true;
            }
            return true;
        }
        return super.execute(action, args, callbackContext);
    }

    @Override
    public Bundle onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                callbackContext.error("缺少权限,无法使用app下载功能");
                return;
            }
        }
        switch (requestCode) {
            case RESULTCODE_PERMISSION:
                startWork();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cordova.getActivity().stopService(new Intent(cordova.getActivity(),UpdateService.class));
    }


    private void startWork() {
        if(!UpdateUtil.isNetworkAvailable(cordova.getActivity())){
            sendUpdateResult(UPDATE_DEFAULT,NET_NOT_AVAILABLE,"网络不可用，请检查网络");
            return;
        }
        UpdateUtil.getInstance().listener = new UpdateListener() {
            @Override
            public void sendUpdateResult(int updateType, int status, String message) {
                UpdateApp.this.sendUpdateResult(updateType,status,message);
            }
        };

        Intent intent = new Intent(cordova.getActivity(), UpdateService.class);
        intent.putExtra(UpdateService.INTENT_URL,url);
        intent.putExtra(UpdateService.INTENT_FILE_DIR,fileDir);
        intent.putExtra(UpdateService.INTENT_SD_APPNAME,sdApkName);
        intent.putExtra(UpdateService.INTENT_NOTI_TITLE,notiTitle);
        cordova.getActivity().startService(intent);
    }

    public void sendUpdateResult(int updateType,int status,String message){
        PluginResult pluginResult;
        JSONArray array = new JSONArray();
        try {
            array.put(0, updateType);
            array.put(1, status);
            array.put(2, message);

        }catch (Exception e){
            e.printStackTrace();
        }
        if(status == NET_NOT_AVAILABLE||status == DOWLOAD_FAILE||status == DOWLOAD_BREAK_OFF) {
            pluginResult = new PluginResult(PluginResult.Status.ERROR, array);
        }else{
            pluginResult = new PluginResult(PluginResult.Status.OK, array);
        }
        callbackContext.sendPluginResult(pluginResult);
    }

    public interface UpdateListener{
        void sendUpdateResult(int updateType,int status,String message);
    }
}
