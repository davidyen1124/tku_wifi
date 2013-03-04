package com.devandroid.tkuautowifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class NetworkStateChanged extends BroadcastReceiver {
	static final String TAG = "TKUWIFILOGIN";
	static final String SSID = "tku";

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (!prefs.getBoolean(Preferences.KEY_ENABLED, false)) {
			Utils.setEnableBroadcastReceiver(context, false);
			return;
		}

		try {
			NetworkInfo netInfo = intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (netInfo == null) {
				return;
			}
			if (!netInfo.isConnected()) {
				return;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			return;
		}

		try {
			if ((WifiManager) context.getSystemService(Context.WIFI_SERVICE) == null
					|| ((WifiManager) context
							.getSystemService(Context.WIFI_SERVICE))
							.getConnectionInfo() == null) {
				return;
			}
			if (((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
					.getConnectionInfo().getSSID() != null) {
				if (!((WifiManager) context
						.getSystemService(Context.WIFI_SERVICE))
						.getConnectionInfo().getSSID().equals(SSID)) {
					return;
				} else {
					Intent i = new Intent(context, WifiLogin.class);
					context.startService(i);
				}
			}
		} catch (NullPointerException e) {
			return;
		}
	}
}
