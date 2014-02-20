package com.devandroid.tkuautowifi;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.devandroid.tkuautowifi.callback.LogoutCallback;

public class WifiLogoutService extends Service {
	private NotificationManager mNotificationManager;
	private Handler mHandler;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void initialValues() {
		mHandler = new Handler();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		initialValues();

		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
			createNotification(Constant.NOTIFICATION_ONGOING_ID,
					content_intent,
					getString(R.string.wifi_disabled_to_logout),
					getString(R.string.wifi_disabled_to_logout),
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
				getString(R.string.logging_out), getString(R.string.app_name),
				getString(R.string.logging_out), true);

		WifiClient.logout(getApplicationContext(), new LogoutCallback() {

			@Override
			public void onSuccess() {
				super.onSuccess();

				mNotificationManager.cancel(Constant.NOTIFICATION_ONGOING_ID);
				Utils.createToastNotification(getApplicationContext(),
						mHandler, getString(R.string.logout_successfully),
						Toast.LENGTH_SHORT);
				stopSelf();
			}

			@Override
			public void onFailure(String message) {
				super.onFailure(message);

				mNotificationManager.cancel(Constant.NOTIFICATION_ONGOING_ID);
				Utils.createToastNotification(getApplicationContext(),
						mHandler, getString(R.string.logout_successfully),
						Toast.LENGTH_SHORT);
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
				.setOngoing(ongoing);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}
}
