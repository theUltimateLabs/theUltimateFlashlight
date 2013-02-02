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

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

//public class SettingsActivity extends Activity {
public class AboutActivity extends Activity {

	final private String TAG = "AboutActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_layout);

		/*
		 * TextView aboutText = (TextView)findViewById(R.id.about_text);
		 * aboutText.setMovementMethod(LinkMovementMethod.getInstance());
		 * aboutText.setText(Html.fromHtml(
		 * "<p>This app is for anyone who's had trouble turning their phone's flashlight on late a night.</p>"
		 * +
		 * "<p>This is a very simple app. It turns on your phone's LED whenever the lockscreen is shown. The LED will turn back off if you unlock the screen or turn the screen back off.</p>"
		 * +
		 * "<p>This app is open source GPL, go to <a href=\"http://theultimatelabs.com\">theultimatelabs.com</a> for more info.</p>"
		 * ));
		 */

		String about = "<h2><a href=\"http://theultimatelabs.com\">theUltimateLabs.com</a></h2>"
				+ "<h2>About</h2>"
				+ "<p>This app is for anyone who's had trouble turning on their phone's LED late at night."
				+ "The goal is to make your phone's LED quickly available when you need it, without using a GUI." +
				"It's a flashlight, lantern and lamp." +
				"It works so well it's replaced my nightstand lamp.</p>" +
				"<p>This app is free and open source under the GPL. I write apps as a hobby, but I would like to do more. " +
				"Please consider <a href=\"http://blog.theultimatelabs.com/p/donate.html\">donating</a> if you like this app " +
				"and want to see more open source apps. Thanks. -<a href=\"mailto:rob@theultimatelabs.com\">Rob</a>"
				+ "<h2>Links</h2>"
				+ "<p><a href=\"http://theultimatelabs.com\">Homepage</a></p>"
				+ "<p><a href=\"https://play.google.com/store/apps/developer?id=theUltimateLabs\">Other Apps</a></p>"
				+ "<p><a href=\"market://details?id=com.theultimatelabs.scale\">Rate This App</a></p>"
				+ "<p><a href=\"https://github.com/theUltimateLabs/theUtimateFlashlight\">Source Code</a></p>"
				+ "<p><a href=\"http://www.gnu.org/licenses/gpl-3.0.txt\">License</a></p>"
				+ "<p><a href=\"http://blog.theultimatelabs.com/p/donate.html\">Donate</a></p>"
				+ "<p><a href=\"mailto:rob@theultimatelabs.com\">Email Me</a></p>"
				+ "<h2>Usage</h2>"
				+ "There's a few different ways to use this app."
				+ "<ul>"
				+ "<li>Delay On: LED turns on after a specified delay after the phone is awakened</li>"
				+ "<li>Quick On: LED turns on if the screen is turned off then on quickly</li>"
				+ "<li>Lantern Mode: LED turns on when phone is placed face down and lockscreen is on</li>"
				+ "<li>Lamp Mode: Same as lantern mode, except it never turns off when plugged in</li> +"
				+ "</ul>"
				+ "The LED will turn off after a period to prevent battery loss. This timeout starts at 10 minutes when being used as a lantern and 1 minute otherwise. "
				+ "The LED will flicker before turning off. Every time the phone is moved the timers reset and more time is added. "
				+ "Specifically, 1 minute is added to the lantern timeout and 5 seconds is added to the normal timeout. "
				+ "i.e. if the phone is moved 5 times, the lantern timeout will now be 15 minutes. So shake your phone if you need more time. "
				+ "<h2>Support</h2>"
				+ "Please comment on the blog or email me at <a href=\"mailto:rob@theultimatelabs.com\">rob@theultimatelabs.com</a>. Comments on on Google Play may go unnoticed.";

		WebView webView = (WebView) findViewById(R.id.webView1);
		webView.loadData(about, "text/html", null);

	}
}
