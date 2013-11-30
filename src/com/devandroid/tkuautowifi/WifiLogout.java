/*
 * Copyright (C) 2013  Sparks Lab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devandroid.tkuautowifi;

import java.io.IOException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class WifiLogout extends IntentService {
	private static final String TAG = "TKUWIFILOGIN";

	private NotificationManager mNotifMan;
	private Handler mHandler;

	public WifiLogout() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		findViews();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent,
					getString(R.string.wifi_disabled_to_logout),
					getString(R.string.wifi_disabled_to_logout),
					getString(R.string.tap_to_modify_wifi_settings), false);
			return;
		}

		if (Utils.isWifiAvailable(this) && !Utils.isConnectToSSID(this, "tku")) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.not_connected_to_tku),
					getString(R.string.not_connected_to_tku),
					getString(R.string.tap_to_modify_wifi_settings), false);
			return;
		}

		WifiClient loginClient = new WifiClient("", "", this);

		try {
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
					getString(R.string.logging_out),
					getString(R.string.app_name),
					getString(R.string.logging_out), true);

			loginClient.logout();

			mNotifMan.cancel(Constant.NOTIFICATION_ONGOING_ID);
			createToastNotification(getString(R.string.logout_successfully),
					Toast.LENGTH_SHORT);
		} catch (IOException e) {
			e.printStackTrace();

			mNotifMan.cancel(Constant.NOTIFICATION_ONGOING_ID);
			createToastNotification(getString(R.string.logout_successfully),
					Toast.LENGTH_SHORT);
		} catch (LogoutRepeatException e) {
			e.printStackTrace();

			mNotifMan.cancel(Constant.NOTIFICATION_ONGOING_ID);
			createToastNotification(getString(R.string.logout_successfully),
					Toast.LENGTH_SHORT);
		}
	}

	private void findViews() {
		mHandler = new Handler();
		mNotifMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private void createToastNotification(final String message, final int length) {
		if (message.length() > 0) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(WifiLogout.this, message, length).show();
				}
			});
		}
	}

	private void createNotification(int notification_id,
			PendingIntent contentIntent, String ticker_text,
			String content_title, String content_text, boolean ongoing) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setLargeIcon(
						BitmapFactory.decodeResource(getResources(),
								R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.notification_small_icon)
				.setTicker(ticker_text).setContentTitle(content_title)
				.setContentText(content_text).setContentIntent(contentIntent)
				.setOngoing(ongoing);

		mNotifMan.notify(notification_id, mBuilder.build());
	}
}
