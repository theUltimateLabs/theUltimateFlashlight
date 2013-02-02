/*******************************************************************************
 * Copyright (c) 2012 rob@theultimatelabs.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rob@theultimatelabs.com - initial API and implementation
 ******************************************************************************/
package com.theultimatelabs.flashlight;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.musicg.api.ClapApi;

public class FlashlightService extends Service {

	static final String TAG = "FlashlightService";
	private SharedPreferences mSettings;
	private boolean keepOff=false;
	private long mLastOff=0;
	private Handler mHandler;
	
	public boolean isEnabled() {
		mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if (mSettings.getBoolean("global_enabled", true) && (mSettings.getBoolean("lantern_enabled", true) ||
				mSettings.getBoolean("lamp_enabled", true) || mSettings.getBoolean("quickOn_enabled", true) || 
				Integer.parseInt(mSettings.getString("on_delay", "2"))>=0)) {
			return true;
		}
		return false;
	}
	
	@Override
	public void onCreate() {

		mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if (!isEnabled()) {
			stopSelf();
		}
		
		mHandler = new Handler();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		filter.addAction(Intent.ACTION_POWER_CONNECTED);
		//filter.addAction(Intent.ACTION_USER_PRESENT);

		registerReceiver(mBroadcastReciever, filter);

	}

	/**
	 * Handler for displaying messages
	 */
	public Handler mhandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			//Log.w(TAG, "handleMessage");
			super.handleMessage(msg);

		}

	};

	@Override
	public void onDestroy() {

		Log.d(TAG, "onDestroy");
		unregisterReceiver(mBroadcastReciever);
		super.onDestroy();

	}

	public BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {

		

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;
			Log.d(TAG, "onReceive: " + action);

			if(!isEnabled()) {
				stopSelf();
			}
			
			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				if(keepOff == false) {
					startLed();
				}
			} else if (action.equals(Intent.ACTION_USER_PRESENT)) {
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				mLastOff = SystemClock.elapsedRealtime();
			} else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
				keepOff = true;
				mHandler.removeCallbacks(disableKeepOff);
				mHandler.postDelayed(disableKeepOff,1000);
			} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
				keepOff = true;
				mHandler.removeCallbacks(disableKeepOff);
				mHandler.postDelayed(disableKeepOff,1000);
			}
			else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				keepOff = true;
				mHandler.removeCallbacks(disableKeepOff);
				mHandler.postDelayed(disableKeepOff,1000);
			}
		}

	};
	
	Runnable disableKeepOff = new Runnable() {
		@Override
		public void run() {
			keepOff = false;	
		}
	};
	
	private void startLed() {
		KeyguardManager km = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		if (km.inKeyguardRestrictedInputMode() && pm.isScreenOn()) {
			Intent ledIntent = new Intent(getApplicationContext(), FlashlightActivity.class);
			ledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ledIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			ledIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			ledIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);
			ledIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			ledIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			ledIntent.putExtra("lastOff", mLastOff);
			getApplicationContext().startActivity(ledIntent);
		} else {
			Log.w(TAG, "not locked");
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Log.v(TAG, "onStartCommand");

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
