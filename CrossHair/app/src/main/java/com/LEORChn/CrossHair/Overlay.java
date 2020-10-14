package com.LEORChn.CrossHair;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.app.PendingIntent;
import android.widget.Toast;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import java.io.File;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.net.Uri;

public class Overlay
extends Service
implements OnClickListener
{

	@Override public IBinder onBind(Intent p1){
		// TODO: Implement this method
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		//((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(
		startForeground(
			1,
			new NotificationProxy(this)
				.setSmallIcon(R.mipmap.ic_foreground)
				.setContentTitle("准心 悬浮窗进程保活")
				.setContentText("点击此处退出")
				.setSound(null)
				.setOngoing(true)
				.setChannel(
					NotificationProxy.createChannel(this, "overlay", "悬浮窗进程保活", null)
				)
				.setOnClickListener(this)
				.build()
		);
		showFloatWindow();
		return Service.START_STICKY; // 允许被系统重启，但启动参数无关紧要
	}
	public void onClick(View v){
		System.exit(0);
	};
	
	// 显示悬浮窗
	WindowManager.LayoutParams lp;
	private void showFloatWindow(){
		int winflag = lp.FLAG_NOT_FOCUSABLE
					| lp.FLAG_NOT_TOUCHABLE,
			wintype = lp.TYPE_SYSTEM_ALERT,
			w = lp.WRAP_CONTENT,
			h = lp.WRAP_CONTENT;
		lp = new WindowManager.LayoutParams(
			w,
			h,
			wintype,
			winflag,
			android.graphics.PixelFormat.TRANSLUCENT
		);
		lp.gravity = Gravity.CENTER;
		View v = LayoutInflater.from(this).inflate(R.layout.crosshair, null);
		String[] configs_path = new String[]{
			getFilesDir().getPath() + "/shift_statusbar",
			getFilesDir().getPath() + "/shift_virtualbutton"
		};
		
		int statusBarHeight = 0;
		int virtualButtonHeight = 0;
		int microShift = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
		// 是否计算偏移
		if(new File(configs_path[0]).exists()){
			//获取status_bar_height资源的ID
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			if (resourceId > 0) { // 根据资源ID获取响应的尺寸值
				statusBarHeight = getResources().getDimensionPixelSize(resourceId);
			}
		}
		if(new File(configs_path[1]).exists()){
			int resourceId = getResources().getIdentifier("navigation_bar_height","dimen", "android");
			if (resourceId > 0) {
				virtualButtonHeight = getResources().getDimensionPixelSize(resourceId);
			}
		}
		v.setPadding(virtualButtonHeight, 0, microShift, statusBarHeight);
		
		// 放置到界面
		((WindowManager)getSystemService(WINDOW_SERVICE)).addView(v, lp);
	}
}
class NotificationProxy
extends BroadcastReceiver
{
	Context c;
	Notification.Builder nb;
	int settedIcon;
	String onClickID;
	OnClickListener l;
	public NotificationProxy(Context context){
		nb = new Notification.Builder(c = context);
	}
	public NotificationProxy setContentTitle(String s){
		nb.setContentTitle(s);
		return this;
	}
	public NotificationProxy setContentText(String s){
		nb.setContentText(s);
		return this;
	}
	public NotificationProxy setOngoing(boolean b){
		nb.setOngoing(b);
		return this;
	}
	public NotificationProxy setSmallIcon(int id){
		nb.setSmallIcon(settedIcon = id);
		return this;
	}
	public NotificationProxy setSound(Uri sound){
		nb.setSound(sound);
		return this;
	}
	public static NotificationChannel createChannel(Context c, String id, String name){
		return createChannel(c, id, name, NotificationManager.IMPORTANCE_DEFAULT, null);
	}
	public static NotificationChannel createChannel(Context c, String id, String name, Uri sound){
		return createChannel(c, id, name, NotificationManager.IMPORTANCE_MAX, sound);
	}
	public static NotificationChannel createChannel(Context c, String id, String name, int importance, Uri sound){
		if(Build.VERSION.SDK_INT < 26) return null;
		// 8.0 以下不会执行到此
		NotificationManager nm = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel nc = nm.getNotificationChannel(id);
		if(nc == null){
			nc = new NotificationChannel(id, name, importance);
			nc.setSound(null, null);
			nm.createNotificationChannel(nc);
			System.out.println("此前通知频道为空，已创建频道"+id+name);
		}
		return nc;
	}
	public NotificationProxy setChannel(Object channel){
		if(Build.VERSION.SDK_INT >= 26)
			nb.setChannelId(((NotificationChannel)channel).getId());
		return this;
	}
	public NotificationProxy setOnClickListener(OnClickListener listener){
		l = listener;
		return this;
	}
	public Notification build(){
		if(!(l instanceof OnClickListener)){
			return nb.build(); // 没有设置点击后行为，直接短路
		}
		try{
			onClickID = Long.toHexString(System.currentTimeMillis());
			Thread.sleep(1); // 确保隔离每个通知的点击事件
		}catch(Exception e){}
		IntentFilter inf = new IntentFilter(onClickID);
		c.registerReceiver(this, inf);
		nb.setContentIntent(PendingIntent.getBroadcast(c, 0, new Intent(onClickID), 0));
		return nb.build();
	}
	
	// ==============================================================
	
	@Override public void onReceive(Context p1, Intent i){
		if(!i.getAction().equals(onClickID)) return;
		l.onClick(null);
	}
}

