package com.devandroid.tkuautowifi;

import java.io.IOException;
import java.net.SocketTimeoutException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class WifiLogin extends IntentService {
	private static final String TAG = "TKUWIFILOGIN";

	private Handler mHandler;
	private SharedPreferences pref;
	private NotificationManager mNotifMan;

	public WifiLogin() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		findViews();
	}

	private void findViews() {
		mHandler = new Handler();
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		mNotifMan = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	private boolean isUsernamePasswordValid() {
		String username = pref.getString(Preferences.KEY_USERNAME, "");
		String password = pref.getString(Preferences.KEY_PASSWORD, "");
		if (username.length() <= 0 || password.length() <= 0) {
			return false;
		}
		return true;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.wifi_disabled_to_login),
					getString(R.string.wifi_disabled_to_login),
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

		if (!isUsernamePasswordValid()) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(this, Preferences.class), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent, getString(R.string.id_password_not_valid),
					getString(R.string.id_password_not_valid),
					getString(R.string.tap_to_modify_id_password), false);
			return;
		}

		WifiClient loginClient = new WifiClient(pref.getString(
				Preferences.KEY_USERNAME, ""), pref.getString(
				Preferences.KEY_PASSWORD, ""), this);

		try {
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
					getString(R.string.notify_login_ongoing_text_logging_in),
					getString(R.string.app_name),
					getString(R.string.notify_login_ongoing_text_logging_in),
					true);

			loginClient.login();

			if (pref.getBoolean(Preferences.KEY_TOAST_NOTIFY_SUCCESS, true)) {
				createToastNotification(getString(R.string.login_successful),
						Toast.LENGTH_SHORT);
			}

			createLoginSuccessNotification();
			createChangelogNotification(loginClient.getChangelog());
			launchApp();
		} catch (SocketTimeoutException e) {
			Log.v(TAG, "Login failed: SocketTimeoutException");
			Log.v(TAG, Utils.stackTraceToString(e));

			createTimeoutRetryNotification();
			// } catch (ConnectTimeoutException e) {
			// Log.v(TAG, "Login failed: ConnectTimeoutException");
			// Log.v(TAG, Utils.stackTraceToString(e));
			//
			// createTimeoutRetryNotification();
		} catch (LoginException e) {
			Log.v(TAG, "Login failed: LoginException");
			Log.v(TAG, Utils.stackTraceToString(e));

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, Preferences.class), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
					getString(R.string.check_id_password),
					getString(R.string.check_id_password),
					getString(R.string.tap_to_modify_id_password), false);
		} catch (IOException e) {
			Log.v(TAG, "Login failed: IOException");
			Log.v(TAG, Utils.stackTraceToString(e));

			createTimeoutRetryNotification();
		} catch (NullPointerException e) {
			// a bug in HttpClient library
			// thrown when there is a connection failure when handling a
			// redirect
			Log.v(TAG, "Login failed: NullPointerException");
			Log.v(TAG, Utils.stackTraceToString(e));

			createTimeoutRetryNotification();
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
				.setAutoCancel(true).setOngoing(ongoing);

		mNotifMan.notify(notification_id, mBuilder.build());
	}

	private void createTimeoutRetryNotification() {
		PendingIntent contentIntent = PendingIntent.getService(this, 0,
				new Intent(WifiLogin.this, WifiLogin.class), 0);
		createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.login_timeout),
				getString(R.string.login_timeout),
				getString(R.string.notify_login_error_text), false);
	}

	private void createLoginSuccessNotification() {
		PendingIntent contentIntent = PendingIntent.getService(WifiLogin.this,
				0, new Intent(WifiLogin.this, WifiLogout.class), 0);
		createNotification(Constant.NOTIFICATION_ONGOING_ID, contentIntent,
				getString(R.string.login_successful),
				getString(R.string.app_name),
				getString(R.string.click_here_to_logout), false);
	}

	private void createChangelogNotification(String changelog) {
		if (changelog.equals("")) {
			return;
		}

		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse(Constant.MARKET_PREFIX + getPackageName()));
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		createNotification(Constant.NOTIFICATION_CHANGELOG_ID, contentIntent,
				changelog, getString(R.string.update_app),
				getString(R.string.tap_go_play_store), false);
	}

	private void createToastNotification(final String message, final int length) {
		if (message.length() > 0) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(WifiLogin.this, message, length).show();
				}
			});
		}
	}

	private void launchApp() {
		String package_name = pref.getString(Preferences.KEY_PACKAGE_NAME, "");
		String class_name = pref.getString(Preferences.KEY_CLASS_NAME, "");

		try {
			if (!package_name.equals("") && !class_name.equals("")) {
				Intent i = new Intent();
				i.setClassName(package_name, class_name);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(i);
			}
		} catch (ActivityNotFoundException e) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(this, AppLoader.class), 0);
			createNotification(Constant.NOTIFICATION_ERROR_ID, content_intent,
					getString(R.string.notify_app_not_exist),
					getString(R.string.app_name),
					getString(R.string.tap_to_change_app), false);

			SharedPreferences.Editor editor = pref.edit();
			editor.putString(Preferences.KEY_PACKAGE_NAME, "");
			editor.putString(Preferences.KEY_CLASS_NAME, "");
			editor.putString(Preferences.KEY_APP_NAME, "");
			editor.commit();
		}
	}
}
