package com.devandroid.tkuautowifi;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.devandroid.tkuautowifi.callback.LoginCallback;

public class WifiLoginService extends Service {
	private Handler mHandler;
	private NotificationManager mNotificationManager;

	private String mUsername, mPassword;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void initialValues() {
		mHandler = new Handler();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.id_password_not_valid),
					getString(R.string.id_password_not_valid),
					getString(R.string.tap_to_modify_id_password), false);

			stopSelf();
			return START_STICKY;
		}

		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.wifi_disabled_to_login),
					getString(R.string.wifi_disabled_to_login),
					getString(R.string.tap_to_modify_wifi_settings), false);

			stopSelf();
			return START_STICKY;
		}

		if (!Utils.isConnectToSSID(this, Constant.SSID)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.not_connected_to_tku),
					getString(R.string.not_connected_to_tku),
					getString(R.string.tap_to_modify_wifi_settings), false);

			stopSelf();
			return START_STICKY;
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(), 0);
		createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.notify_login_ongoing_text_logging_in),
				getString(R.string.app_name),
				getString(R.string.notify_login_ongoing_text_logging_in), true);

		WifiClient.login(getApplicationContext(), mUsername, mPassword,
				new LoginCallback() {

					@Override
					public void onSuccess() {
						super.onSuccess();

						if (Memory.getBoolean(getApplicationContext(),
								Preferences.KEY_TOAST_NOTIFY_SUCCESS, true)) {
							Utils.createToastNotification(
									getApplicationContext(), mHandler,
									getString(R.string.login_successful),
									Toast.LENGTH_SHORT);
						}

						createLoginSuccessNotification();
						launchApp();

						stopSelf();
					}

					@Override
					public void onFailure(String message) {
						super.onFailure(message);

						createTimeoutRetryNotification();

						stopSelf();
					}
				});
		return START_STICKY;
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
				.setAutoCancel(true).setOngoing(ongoing);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}

	private void createTimeoutRetryNotification() {
		PendingIntent contentIntent = PendingIntent.getService(this, 0,
				new Intent(getApplicationContext(), WifiLoginService.class), 0);
		createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.login_timeout),
				getString(R.string.login_timeout),
				getString(R.string.notify_login_error_text), false);
	}

	private void createLoginSuccessNotification() {
		PendingIntent contentIntent = PendingIntent.getService(
				getApplicationContext(), 0, new Intent(getApplicationContext(),
						WifiLogoutService.class), 0);
		createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.login_successful),
				getString(R.string.app_name),
				getString(R.string.click_here_to_logout), false);
	}

	// private void createChangelogNotification(String changelog) {
	// if (changelog.equals("")) {
	// return;
	// }
	//
	// Intent i = new Intent(Intent.ACTION_VIEW,
	// Uri.parse(Constant.MARKET_PREFIX + getPackageName()));
	// PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
	// Intent.FLAG_ACTIVITY_NEW_TASK);
	// createNotification(Constant.NOTIFICATION_CHANGELOG_ID, contentIntent,
	// changelog, getString(R.string.update_app),
	// getString(R.string.tap_go_play_store), false);
	// }

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
			createNotification(Constant.NOTIFICATION_ERROR_ID, content_intent,
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
