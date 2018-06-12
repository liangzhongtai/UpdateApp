package com.chinamobile.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.RemoteViews;



import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateService extends Service {
	public final static String INTENT_URL        = "url";
	public final static String INTENT_FILE_DIR   = "file_dir";
	public final static String INTENT_SD_APPNAME = "appName";
	public final static String INTENT_NOTI_TITLE = "titleId";

	private static String savePath = "/@";

	//标题
	private static String notiTitle;
	//存储的文件夹
	private static String fileDir;
	//下载的文件名
	private static String sdApkName;

	//文件存储
	private File updateDir = null;
	private File updateFile = null;
	//下载状态
	private final static int DOWNLOAD_COMPLETE  = 0;
	private final static int DOWNLOAD_FAIL      = 1;
	private final static int DOWNLOAD_BREAK_OFF = 2;
	//通知栏
	private NotificationManager notificationManager = null;
	private Notification updateNotification = null;
	private NotificationCompat.Builder builder;
	//通知栏跳转Intent
	//private Intent updateIntent = null;
	private PendingIntent updatePendingIntent = null;
	private String downloadURL;

	int downloadCount = 0;
	int currentSize = 0;
	long totalSize = 0;
	int updateTotalSize = 0;

	//在onStartCommand()方法中准备相关的下载工作：
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//获取传值
		downloadURL = intent.getStringExtra(INTENT_URL);
		fileDir     = intent.getStringExtra(INTENT_FILE_DIR);
		sdApkName   = intent.getStringExtra(INTENT_SD_APPNAME);
		notiTitle   = intent.getStringExtra(INTENT_NOTI_TITLE);
		notiTitle = notiTitle == null?sdApkName:notiTitle;
		savePath = savePath.replace("@",fileDir);
		//创建文件
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			updateDir  = new File(Environment.getExternalStorageDirectory(),savePath);
			updateFile = new File(updateDir.getPath(), sdApkName);
		}

		this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel("2",
					"Channel2", NotificationManager.IMPORTANCE_DEFAULT);
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
			channel.setShowBadge(true);
			notificationManager.createNotificationChannel(channel);
		}
		builder = new NotificationCompat.Builder(this,"2");
		builder.setSmallIcon(android.R.drawable.sym_def_app_icon);
		builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), android.R.drawable.sym_def_app_icon));
		builder.setTicker("开始下载");
		builder.setContentTitle(notiTitle);
		builder.setContentText( "0%");
		updateNotification = builder.build();
		updateNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		//发出通知
		notificationManager.notify(1, updateNotification);
		Log.d(UpdateApp.TAG,"发出通知");
		//开启一个新的线程下载，如果使用Service同步下载，会导致ANR问题，Service本身也会阻塞
		new Thread(new updateRunnable()).start();// 这个是下载的重点，是下载的过程

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	private Handler updateHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_COMPLETE:
				//点击安装PendingIntent
				if(UpdateUtil.getInstance().listener!=null)
				UpdateUtil.getInstance().listener.sendUpdateResult(UpdateApp.UPDATE_DEFAULT,UpdateApp.DOWNLOAD_SUCCESS,"下载完成");
				Intent installIntent = new Intent(Intent.ACTION_VIEW);
				installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Uri uri;
				if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
					//判读版本是否在7.0以上 //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致 参数3 共享的文件
					uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + UpdateUtil.FILEPROVIDER, updateFile);
					//添加这一句表示对目标应用临时授权该Uri所代表的文件
					installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				}else{
					uri = Uri.fromFile(updateFile);
				}

				installIntent.setDataAndType(uri,
						"application/vnd.android.package-archive");
				updatePendingIntent = PendingIntent.getActivity(
						UpdateService.this, 0, installIntent, 0);
				builder.setContentTitle(notiTitle);
				builder.setContentText("下载完成,点击安装。");
				builder.setContentIntent(updatePendingIntent);
				updateNotification = builder.build();
				updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				updateNotification.defaults = Notification.DEFAULT_SOUND;// 铃声提醒
				notificationManager.notify(1, updateNotification);
				Log.d(UpdateApp.TAG,"安装开始");
				//停止服务
				//stopService(new Intent(getApplicationContext(),UpdateService.class));
				break;
			case DOWNLOAD_FAIL:
				//下载失败
				if(UpdateUtil.getInstance().listener!=null)
				UpdateUtil.getInstance().listener.sendUpdateResult(UpdateApp.UPDATE_DEFAULT,UpdateApp.DOWLOAD_FAILE,"下载失败");
				builder.setContentTitle(notiTitle);
				builder.setContentText("下载失败。");
				builder.setContentIntent(updatePendingIntent);
				updateNotification = builder.build();
				updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				notificationManager.notify(1, updateNotification);
				Log.d(UpdateApp.TAG,"下载失败");
				break;
			case DOWNLOAD_BREAK_OFF:
				if(UpdateUtil.getInstance().listener!=null)
				UpdateUtil.getInstance().listener.sendUpdateResult(UpdateApp.UPDATE_DEFAULT,UpdateApp.DOWLOAD_BREAK_OFF,"下载中断");
				builder.setContentTitle(notiTitle);
				builder.setContentText("下载中断。");
				builder.setContentIntent(updatePendingIntent);
				updateNotification = builder.build();
				updateNotification.flags |= Notification.FLAG_AUTO_CANCEL;
				notificationManager.notify(1, updateNotification);
				break;
			default:
				//stopService(new Intent(getApplicationContext(),UpdateService.class));
				break;
			}
		}
	};

	public long downloadUpdateFile(String downloadUrl, File saveFile) throws Exception {

		HttpURLConnection httpConnection = null;
		InputStream is = null;
		FileOutputStream fos = null;

		try {
			URL url = new URL(downloadUrl);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
			if (currentSize > 0) {
				httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
			}
			httpConnection.setConnectTimeout(10000);
			httpConnection.setReadTimeout(20000);
			httpConnection.setRequestProperty("Accept-Encoding", "identity");
			updateTotalSize = httpConnection.getContentLength();
			Log.d(UpdateApp.TAG, "请求完成--->="+httpConnection.getResponseCode());
			if (httpConnection.getResponseCode() == 404) {
				if(UpdateUtil.getInstance().listener!=null)
				UpdateUtil.getInstance().listener.sendUpdateResult(UpdateApp.UPDATE_DEFAULT,UpdateApp.DOWLOAD_FAILE,"下载链接404错误");
			}else {
				//Log.d(UpdateApp.TAG, "updateTotalSize--->" + updateTotalSize);
				if (updateFile.length() == updateTotalSize) {
					totalSize = updateTotalSize;
				} else {
					is = httpConnection.getInputStream();
					fos = new FileOutputStream(saveFile, false);
					byte buffer[] = new byte[4096];
					int readsize;
					while ((readsize = is.read(buffer)) > 0) {
						fos.write(buffer, 0, readsize);
						totalSize += readsize;
						//为了防止频繁的通知导致应用吃紧，百分比增加5才通知一次
						if ((downloadCount == 0) || (int) (totalSize * 100 / updateTotalSize) - 5 > downloadCount) {
							downloadCount += 5;
							/***
							 * 在这里我们用自定的view来显示Notification
							 */
							int progress = (int) totalSize * 100 / updateTotalSize;
							if (progress > 100) progress = 100;
							updateNotification.flags |= Notification.FLAG_NO_CLEAR;
							builder.setTicker("正在下载");
							builder.setContentTitle(notiTitle);
							builder.setContentText(progress + "%");
							updateNotification = builder.build();
						/*updateNotification.contentView = new RemoteViews(
								getPackageName(), UpdateUtil.getResId(getApplicationContext(), "notification_update", "layout"));
						//updateNotification.contentView.setBitmap(UpdateUtil.getResId(getApplicationContext(),"iv_logo","id"),"setImageBitmap",UpdateUtil.getAppIconBitmap(this));
						updateNotification.contentView.setTextViewText(UpdateUtil.getResId(getApplicationContext(), "tv_title", "id"), notiTitle);
						updateNotification.contentView.setTextViewText(
								UpdateUtil.getResId(getApplicationContext(), "tv_persent", "id"), progress + "%");
						updateNotification.contentView.setProgressBar(
								UpdateUtil.getResId(getApplicationContext(), "pb_loading", "id"), 100, downloadCount,
								false);*/
							notificationManager.notify(1, updateNotification);
						}
					}
				}
			}
		} catch (Exception e){
			Message message = new Message();
			message.what = DOWNLOAD_BREAK_OFF;
			updateHandler.sendMessage(message);
		}finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
			if (is != null) {
				is.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
		return totalSize;
	}

	class updateRunnable implements Runnable {
		Message message = updateHandler.obtainMessage();

		public void run() {
			message.what = DOWNLOAD_COMPLETE;
			try {
				if (!updateDir.exists()) {
					updateDir.mkdirs();
				}
				if (!updateFile.exists()) {
					updateFile.createNewFile();
				}
				//Log.d(UpdateApp.TAG,"下载进行中");
				long downloadSize = downloadUpdateFile(downloadURL, updateFile);
				if (downloadSize > 0) {
					//下载成功
					updateHandler.sendMessage(message);
					clearAll();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				//Log.d(UpdateApp.TAG,"下载失败!");
				message.what = DOWNLOAD_FAIL;
				//下载失败
				updateHandler.sendMessage(message);
			}
		}
	}

	public void clearAll() {
		downloadCount = 0;
		currentSize = 0;
		totalSize = 0;
		updateTotalSize = 0;
	}
}
