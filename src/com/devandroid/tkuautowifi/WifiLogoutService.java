package com.devandroid.tkuautowifi;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import com.devandroid.tkuautowifi.callback.LogoutCallback;
import com.devandroid.tkuautowifi.notification.NotificationHelper;

public class WifiLogoutService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!Utils.isWifiAvailable(this)) {
			PendingIntent content_intent = PendingIntent.getActivity(this, 0,
					new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

			NotificationHelper.createNotification(getApplicationContext(),
					Constant.NOTIFICATION_ONGOING_ID, content_intent,
					getString(R.string.wifi_disabled_to_logout),
					getString(R.string.wifi_disabled_to_logout),
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
				getString(R.string.logging_out), getString(R.string.app_name),
				getString(R.string.logging_out), true);

		WifiClient.logout(getApplicationContext(), new LogoutCallback() {

			@Override
			public void onSuccess() {
				super.onSuccess();

				NotificationHelper.cancelNotification(getApplicationContext(),
						Constant.NOTIFICATION_ONGOING_ID);
				Utils.createToastNotification(getApplicationContext(),
						getString(R.string.logout_successfully),
						Toast.LENGTH_SHORT);
				stopSelf();
			}

			@Override
			public void onFailure(String message) {
				super.onFailure(message);

				NotificationHelper.cancelNotification(getApplicationContext(),
						Constant.NOTIFICATION_ONGOING_ID);
				Utils.createToastNotification(getApplicationContext(),
						getString(R.string.logout_successfully),
						Toast.LENGTH_SHORT);
				stopSelf();
			}
		});

		return START_STICKY;
	}
}
