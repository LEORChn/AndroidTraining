package com.LEORChn.CrossHair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.MessageQueue;
import android.os.Looper;
import android.view.View.OnClickListener;
import android.view.View;
import java.io.File;
import android.widget.CheckBox;
import android.net.Uri;
import android.provider.Settings;
import android.os.Build;

public class MainActivity
extends Activity
implements
	OnClickListener,
	MessageQueue.IdleHandler
{
	// 实例内全局变量
	String[] configs_path;
	
	// 启动
    @Override protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		Looper.myQueue().addIdleHandler(this);
	}

	@Override public boolean queueIdle(){
		startOverlay();
		configs_path = new String[]{
			getFilesDir().getPath() + "/shift_statusbar",
			getFilesDir().getPath() + "/shift_virtualbutton"
		};
		setContentView(R.layout.main);
		View[] v = new View[]{
			findViewById(R.id.overlay_setting),
			findViewById(R.id.notification_setting),
			findViewById(R.id.shift_sb),
			findViewById(R.id.shift_vb),
			findViewById(R.id.exit)
		};
		for(int i=0; i<v.length; i++){
			v[i].setOnClickListener(this);
		}
		int checkboxBase = 2;
		for(int i=checkboxBase; i<checkboxBase+2; i++)
			((CheckBox)v[i]).setChecked(new File(configs_path[i-checkboxBase]).exists());
			
		return false;
	}
	
	private void startOverlay(){
		startService(new Intent(this, Overlay.class));
	}

	@Override public void onClick(View v){
		int cb = 1;
		switch(v.getId()){
			case R.id.exit:
				System.exit(0);
			case R.id.overlay_setting:
				showSystemFloatWindowSetting();
				break;
			case R.id.notification_setting:
				showSystemNotificationSetting();
				break;
			case R.id.shift_sb:
				cb = 0;
			case R.id.shift_vb:
				File f = new File(configs_path[cb]);
				CheckBox c = (CheckBox)v;
				try{
					if(c.isChecked()){
						f.createNewFile();
					}else{
						f.delete();
					}
				}catch(Throwable e){}
		}
	}

	void showSystemFloatWindowSetting(){
		try{
			Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			Uri uri = Uri.fromParts("package", getPackageName(), null);
			i.setData(uri);
			startActivity(i);
		}catch(Exception e){
			Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", getPackageName(), null);
			i.setData(uri);
			startActivity(i);
		}
	}
	void showSystemNotificationSetting(){
		try{
			Intent i = new Intent();
			if(Build.VERSION.SDK_INT >= 26){
				i.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
				i.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
			}else if(Build.VERSION.SDK_INT >= 20){
				i.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
				i.putExtra("app_package", getPackageName());
				i.putExtra("app_uid", getApplicationInfo().uid);
			}
			startActivity(i);
		}catch(Exception e){
			Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", getPackageName(), null);
			i.setData(uri);
			startActivity(i);
		}
	}
	
	// 28
	@Override public void onPointerCaptureChanged(boolean p1){
		// TODO: Implement this method
	}
}
