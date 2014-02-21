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

import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.devandroid.tkuautowifi.callback.LoginCallback;
import com.devandroid.tkuautowifi.notification.NotificationHelper;
import com.devandroid.tkuautowifi.ui.AppLoader;
import com.devandroid.tkuautowifi.ui.Preferences;

public class WifiLoginService extends Service {
	private String mUsername, mPassword;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void initialValues() {
		mUsername = Memory.getString(getApplicationContext(),
				Preferences.KEY_USERNAME, "");
		mPassword = Memory.getString(getApplicationContext(),
				Preferences.KEY_PASSWORD, "");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initialValues();

		if (mUsername.length() == 0 || mPassword.length() == 0) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(this, Preferences.class), 0);
			NotificationHelper.createNotification(getApplicationContext(),
					Constant.NOTIFICATION_ONGOING_ID, content_intent,
					getString(R.string.id_password_not_valid),
					getString(R.string.id_password_not_valid),
					getString(R.string.tap_to_modify_id_password), false);

			stopSelf();
			return START_STICKY;
		}

		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			NotificationHelper.createNotification(getApplicationContext(),
					Constant.NOTIFICATION_ONGOING_ID, content_intent,
					getString(R.string.wifi_disabled_to_login),
					getString(R.string.wifi_disabled_to_login),
					getString(R.string.tap_to_modify_wifi_settings), false);

			stopSelf();
			return START_STICKY;
		}

		if (!Utils.isConnectToSSID(this, Constant.SSID)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			NotificationHelper.createNotification(getApplicationContext(),
					Constant.NOTIFICATION_ONGOING_ID, content_intent,
					getString(R.string.not_connected_to_tku),
					getString(R.string.not_connected_to_tku),
					getString(R.string.tap_to_modify_wifi_settings), false);

			stopSelf();
			return START_STICKY;
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(), 0);
		NotificationHelper.createNotification(getApplicationContext(),
				Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.notify_login_ongoing_text_logging_in),
				getString(R.string.app_name),
				getString(R.string.notify_login_ongoing_text_logging_in), true);

		WifiClient.login(getApplicationContext(), mUsername, mPassword,
				new LoginCallback() {

					@Override
					public void onSuccess() {
						super.onSuccess();

						Utils.createToastNotification(getApplicationContext(),
								getString(R.string.login_successful),
								Toast.LENGTH_SHORT);

						PendingIntent contentIntent = PendingIntent.getService(
								getApplicationContext(), 0, new Intent(
										getApplicationContext(),
										WifiLogoutService.class), 0);
						NotificationHelper
								.createNotification(
										getApplicationContext(),
										Constant.NOTIFICATION_ONGOING_ID,
										contentIntent,
										getString(R.string.login_successful),
										getString(R.string.app_name),
										getString(R.string.click_here_to_logout),
										false);
						launchApp();

						stopSelf();
					}

					@Override
					public void onFailure(String message) {
						super.onFailure(message);

						PendingIntent contentIntent = PendingIntent.getService(
								getApplicationContext(), 0, new Intent(
										getApplicationContext(),
										WifiLoginService.class), 0);
						NotificationHelper.createNotification(
								getApplicationContext(),
								Constant.NOTIFICATION_ONGOING_ID,
								contentIntent,
								getString(R.string.login_timeout),
								getString(R.string.login_timeout),
								getString(R.string.notify_login_error_text),
								false);

						stopSelf();
					}
				});
		return START_STICKY;
	}

	private void launchApp() {
		String packageName = Memory.getString(getApplicationContext(),
				Preferences.KEY_PACKAGE_NAME, "");
		String className = Memory.getString(getApplicationContext(),
				Preferences.KEY_CLASS_NAME, "");

		try {
			if (packageName.length() > 0 && className.length() > 0) {
				Intent i = new Intent();
				i.setClassName(packageName, className);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		} catch (ActivityNotFoundException e) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(this, AppLoader.class), 0);
			NotificationHelper.createNotification(getApplicationContext(),
					Constant.NOTIFICATION_ERROR_ID, content_intent,
					getString(R.string.notify_app_not_exist),
					getString(R.string.app_name),
					getString(R.string.tap_to_change_app), false);

			Memory.setString(getApplicationContext(),
					Preferences.KEY_PACKAGE_NAME, "");
			Memory.setString(getApplicationContext(),
					Preferences.KEY_CLASS_NAME, "");
			Memory.setString(getApplicationContext(), Preferences.KEY_APP_NAME,
					"");
		}
	}
}
