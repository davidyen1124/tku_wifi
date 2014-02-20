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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

public class NetworkStateChanged extends BroadcastReceiver {

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

		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Service.WIFI_SERVICE);

		try {
			// check manager provide enough information
			if (wifiManager == null || wifiManager.getConnectionInfo() == null) {
				return;
			}

			// after Android 4.3, the ssid becomes "ssid", so we need to remove
			// "
			String ssid = wifiManager.getConnectionInfo().getSSID();
			if (ssid != null && ssid.replace("\"", "").equals(Constant.SSID)) {
				Intent i = new Intent(context, WifiLoginService.class);
				context.startService(i);
			} else {
				return;
			}
		} catch (NullPointerException e) {
			return;
		}
	}
}
