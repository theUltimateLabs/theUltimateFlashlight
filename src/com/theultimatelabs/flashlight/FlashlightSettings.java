package com.theultimatelabs.flashlight;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

//public class SettingsActivity extends Activity {
public class FlashlightSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {


	final private String TAG = "AboutActivity";
	private SharedPreferences mSettings;

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
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		if (isEnabled()) {
		   startService(new Intent(getApplicationContext(), FlashlightService.class));
       }
		
		
		//setContentView(R.layout.settings_layout);
		
		
		/*TextView aboutText = (TextView)findViewById(R.id.about_text);
		aboutText.setMovementMethod(LinkMovementMethod.getInstance());
		aboutText.setText(Html.fromHtml(
				 "<p>This app is for anyone who's had trouble turning their phone's flashlight on late a night.</p>" +
				 "<p>This is a very simple app. It turns on your phone's LED whenever the lockscreen is shown. The LED will turn back off if you unlock the screen or turn the screen back off.</p>" +
				 "<p>This app is open source GPL, go to <a href=\"http://theultimatelabs.com\">theultimatelabs.com</a> for more info.</p>"
				 ));
		
		final SharedPreferences settings = getSharedPreferences("PREFS", 0);
		
		Spinner delaySpinner = (Spinner) findViewById(R.id.delay_spinner);
	
		delaySpinner.setSelection(settings.getInt("delay",2));
		delaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				settings.edit().putInt("delay", arg2).commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		CheckBox enableCheckBox = (CheckBox) findViewById(R.id.enabledCheckBox);
		enableCheckBox.setChecked(settings.getBoolean("enabled",true));
		if(settings.getBoolean("enabled", true)){
			startService(new Intent(getApplicationContext(), FlashlightService.class));
		}
		enableCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				settings.edit().putBoolean("enabled", isChecked).commit();
				if(isChecked) {
				
				}
				else {
					//TODO: stop service
				}
			}
		});*/
		
	}
	
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if(isEnabled()) {
		   startService(new Intent(getApplicationContext(), FlashlightService.class));
	   }
    }

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG,"onStop");
		finish();
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

}
