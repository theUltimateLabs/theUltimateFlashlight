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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class FlashlightReceiver extends BroadcastReceiver {

	public final static String TAG = "WatchReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG,"Starting flashlight service");
		context.startService(new Intent(context, FlashlightService.class));	
	}

	

}
