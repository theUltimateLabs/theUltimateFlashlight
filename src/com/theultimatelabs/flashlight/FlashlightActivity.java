/*******************************************************************************
 * Copyright (c) 2013 rob@theultimatelabs.com.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     rob@theultimatelabs.com - initial API and implementation
 ******************************************************************************/
package com.theultimatelabs.flashlight;

import java.io.IOException;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class FlashlightActivity extends Activity implements SensorEventListener, SurfaceHolder.Callback {

	private static long LANTERN_TIMEOUT;
	private static long MOTION_TIMEOUT;
	final private String TAG = "FlashlightActivity";
	private Camera mCamera;
	private SurfaceHolder mHolder;
	private boolean mLedOn = false;
	long motionTimeout = 0;
	long lanternTime = 0;
	long mOnDelay = 0;
	long proximityTimeout = 0;
	long PROXIMITY_TIMEOUT = 10000;
	private WakeLock mLock;
	private SharedPreferences mSettings;
	private KeyguardManager mKeyguard = null;
	private PowerManager mPower = null;
	private SensorManager mSensor;
	private int mLightCount = 0;
	private float mLightSum = 0;
	private Handler mHandler;
	private float mProximity;
	private float mLight = 10;
	private boolean mIsFlat ;
	private boolean mIsFlickering;
	private boolean mLanternEnabled;
	private boolean mFlashlightEnabled;
	private boolean mLampEnabled;
	private long mLastOff;
	private boolean mQuickOnEnabled;
	private boolean mStopped=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SurfaceView preview = (SurfaceView) findViewById(R.id.PREVIEW);
		mHolder = preview.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		// Log.i(TAG,"Start Service");
		if (!getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			Toast.makeText(this, "No camera flash support", Toast.LENGTH_LONG).show();
			finish();
		}
		
		mLastOff = getIntent().getExtras().getLong("lastOff",0);
		//Log.v(TAG,String.format("lastOff = %d", SystemClock.elapsedRealtime() - mLastOff));
		
		mLanternEnabled = mSettings.getBoolean("lantern_enabled", true);
		mFlashlightEnabled = mSettings.getBoolean("flashlight_enabled", true);
		mLampEnabled = mSettings.getBoolean("lamp_enabled", true);
		mQuickOnEnabled = mSettings.getBoolean("quickOn_enabled", true);

		MOTION_TIMEOUT = 60*1000;//Integer.parseInt(mSettings.getString("motionless_timeout", "60")) * 1000;
		LANTERN_TIMEOUT = 10*60*1000;//Integer.parseInt(mSettings.getString("lantern_timeout", "600")) * 1000;
		mOnDelay = SystemClock.elapsedRealtime() + Integer.parseInt(mSettings.getString("on_delay", "2")) * 1000 - 200;

		Log.v(TAG, "LED Activity Create");

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		// filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		// filter.addAction(Intent.ACTION_POWER_CONNECTED);
		filter.addAction(Intent.ACTION_USER_PRESENT);

		registerReceiver(mBroadcastReciever, filter);

		mPower = (PowerManager) getSystemService(Context.POWER_SERVICE);

		mSensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		if (!check())
			return;
		
		// sm.registerListener(this,
		// sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		// SensorManager.SENSOR_DELAY_UI);
		mSensor.registerListener(this, mSensor.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		mSensor.registerListener(this, mSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		mSensor.registerListener(this, mSensor.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_NORMAL);

		if (mFlashlightEnabled) {
			mLock = mPower.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Flashlight");
			mLock.acquire();
		}

		mHandler = new Handler();

		mHandler.post(new Runnable() {
			public void run() {
				if (check() && !mLedOn) {
					if ((mFlashlightEnabled && (SystemClock.elapsedRealtime() >= mOnDelay) 
							|| (mQuickOnEnabled && (SystemClock.elapsedRealtime()-mLastOff) < 1000)) || 
							(mLanternEnabled && mIsFlat == true)) {
						// Log.v(TAG, String.format("light: %f", mLightSum /
						// mLightCount));
						// if (mLightSum / mLightCount < 1000) {
						// mSensor.unregisterListener(FlashlightActivity.this,
						// mSensor.getDefaultSensor(Sensor.TYPE_LIGHT));
						if(mLock==null || !mLock.isHeld()) {
							mLock = mPower.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Flashlight");
							mLock.acquire();
						}
						
						ledOn();

						return;
					}
					mHandler.postDelayed(this, 100);
				}
			}
		});

	}

	public boolean isEnabled() {
		
		mLanternEnabled = mSettings.getBoolean("lantern_enabled", true);
		mFlashlightEnabled = Integer.parseInt(mSettings.getString("on_delay", "2"))>=0;
		mLampEnabled = mSettings.getBoolean("lamp_enabled", true);
		mQuickOnEnabled = mSettings.getBoolean("quickOn_enabled", true);
		
		if (mSettings.getBoolean("global_enabled", true) && (mLanternEnabled || mFlashlightEnabled || mLampEnabled || mQuickOnEnabled)) {
			return true;
		}
		return false;
	}
	private boolean check() {
		if (mKeyguard == null)
			mKeyguard = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);

		if (mPower == null)
			mPower = (PowerManager) getSystemService(Context.POWER_SERVICE);

		

		if (!isEnabled() || !mPower.isScreenOn() || !mKeyguard.inKeyguardRestrictedInputMode()) {
			stop();
			return false;
		} else {
			return true;
		}

	}

	private void ledFlicker(final int timeoutIn) {

		if (mIsFlickering) {
			return;
		}
		mIsFlickering = true;
		final Handler handler = new Handler();
		Log.v(TAG, "Flicker");
		// TelephonyManager mTelephony = (TelephonyManager)
		// getSystemService(Context.TELEPHONY_SERVICE);
		// mTelephony.listen(this, PhoneStateListener.LISTEN_CALL_STATE);

		handler.post(new Runnable() {
			int flickerCount = 0;
			final int FLICKER_PERIOD = 100;
			final int timeout = timeoutIn;

			public void run() {
				if (mLedOn == true && mCamera != null) {
					Parameters params = mCamera.getParameters();
					if (flickerCount <= 3) {
						if (flickerCount % 2 == 0) {
							params.setFlashMode(Parameters.FLASH_MODE_OFF);
							params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
						} else {
							params.setFlashMode(Parameters.FLASH_MODE_TORCH);
							params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
						}
						mCamera.setParameters(params);
					}
					// flickerToggle = !flickerToggle;

					if (flickerCount >= timeout / FLICKER_PERIOD) {
						mIsFlickering = false;
						return;
					}
					flickerCount++;

					handler.postDelayed(this, FLICKER_PERIOD);

				} else {
					mIsFlickering = false;
				}

			}
		});

	}
	

	private void ledOn() {
		if (mLedOn == false) {
			Log.i(TAG, "TURN ON LED");

			mLedOn = true;
			
			try {
				mCamera = Camera.open();
				mCamera.setPreviewDisplay(mHolder);
			} catch (Exception e) {
				Log.e(getString(R.string.app_name), "failed to open Camera");
				e.printStackTrace();
				stop();
			}


			// Turn on LED
			Parameters params = mCamera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
			mCamera.setParameters(params);
			
			mCamera.startPreview();

			motionTimeout = SystemClock.elapsedRealtime() + MOTION_TIMEOUT;
			lanternTime = SystemClock.elapsedRealtime() + LANTERN_TIMEOUT;

			// mSensor.registerListener(this,
			// mSensor.getDefaultSensor(Sensor.TYPE_PROXIMITY),
			// SensorManager.SENSOR_DELAY_NORMAL);

		}
	}

	public BroadcastReceiver mBroadcastReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action == null)
				return;
			Log.d(TAG, "onRecevie: " + action);

			if (action.equals(Intent.ACTION_SCREEN_ON)) {
				stop();
			} else if (action.equals(Intent.ACTION_USER_PRESENT)) {
				stop();
			} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
				stop();
			} else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
				stop();
			} else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
				stop();
			} else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
				stop();
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Activity destroy");
		if(!mStopped) stop();
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		//Log.w(TAG, "surface changed");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		//Log.w(TAG, "surface created");
		mHolder = holder;
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mCamera != null) {
			mCamera.stopPreview();
		}
		mHolder = null;
	}

	public void stop() {

		Log.d(TAG, "Activity stop");
		mStopped = true;
		if (mSensor != null) {
			mSensor.unregisterListener(this);
			mSensor = null;
		}
		// mLock.release();
		if (mLedOn && mCamera != null) {
			Log.i(TAG, "TURN OFF LED");
			// Turn off LED
			Parameters params = mCamera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(params);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			// mLedOn = false;
		}

		if (mBroadcastReciever != null) {
			unregisterReceiver(mBroadcastReciever);
			mBroadcastReciever = null;
		}

		if (mLock != null && mLock.isHeld()) {
			mLock.release();
			mLock = null;
		}
		
		finish();
	}

	// TODO: MAX TIME
	// TODO: CLAP CLAP
	// TODO: SETTINGS
	@Override
	public void onSensorChanged(SensorEvent event) {

		if (!check()) {
			return;
		}

		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			// mLightSum += event.values[0];
			mLight = event.values[0];
			// mLightCount += 1;
			Log.v(TAG, String.format("light: %f", mLight));
			// Log.v(TAG, String.format("time: %d %d",
			// SystemClock.elapsedRealtime(), delayTimeout));
		} else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			//Log.v(TAG, String.format("linear: %f %f %f", x, y, z));
			double mag = Math.sqrt(x * x + y * y + z * z);
			//Log.v(TAG, String.format("linear mag: %f", mag));

			if (mag >= .66) {
				Log.i(TAG,"Motion detected, reset timers");
				if(mLedOn && LANTERN_TIMEOUT > 0) LANTERN_TIMEOUT += 60000;
				lanternTime = SystemClock.elapsedRealtime() + LANTERN_TIMEOUT;
				if(mLedOn && MOTION_TIMEOUT > 0) MOTION_TIMEOUT += 5000;
				motionTimeout = SystemClock.elapsedRealtime() + MOTION_TIMEOUT;
			}

		} else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			//Log.d(TAG, String.format("accelerometer: %f %f %f", x, y, z));
			if (z < -7.5) {
				if(mIsFlat == false) Log.i(TAG, "Device is flat");
				mIsFlat = true;
			} else if(mIsFlat == true) {
				if(mIsFlat == true) Log.i(TAG, String.format("Device is not flat"));
				mIsFlat = false;
			}

		} else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
			mProximity = event.values[0];
			if (mProximity == 0) {
				proximityTimeout = SystemClock.elapsedRealtime() + PROXIMITY_TIMEOUT;
			}
			// Log.d(TAG, String.format("proximity: %f", mProximity));
		}

		Intent batteryStatus = getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		int chargingStatus = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = chargingStatus == BatteryManager.BATTERY_STATUS_CHARGING || chargingStatus == BatteryManager.BATTERY_STATUS_FULL;

		if (!(mLampEnabled && isCharging) && mLedOn) {
			if (mIsFlat) {
				if (LANTERN_TIMEOUT > 0 && SystemClock.elapsedRealtime() > lanternTime) {
					stop();
				} else if (SystemClock.elapsedRealtime() > lanternTime - 5000) {
					ledFlicker(5000);
				}
			} else {
				if (MOTION_TIMEOUT > 0 && SystemClock.elapsedRealtime() > motionTimeout) {
					stop();
				} else if (SystemClock.elapsedRealtime() > motionTimeout - 2000) {
					ledFlicker(2000);
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

}
