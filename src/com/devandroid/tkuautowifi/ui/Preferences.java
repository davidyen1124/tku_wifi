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

package com.devandroid.tkuautowifi.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.devandroid.tkuautowifi.Constant;
import com.devandroid.tkuautowifi.Memory;
import com.devandroid.tkuautowifi.R;
import com.devandroid.tkuautowifi.Utils;
import com.devandroid.tkuautowifi.WifiLoginService;
import com.devandroid.tkuautowifi.WifiLogoutService;

public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	public static final String KEY_LOGIN_NOW = "login_now";
	public static final String KEY_LOGOUT_NOW = "logout_now";
	public static final String KEY_SELECT_APP = "select_app_launch";
	public static final String KEY_ENABLED = "enabled";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_VERSION = "version";
	public static final String KEY_WEBSITE = "website";
	public static final String KEY_CONNECTION_DETAIL = "connection_detail";

	public static final String BUNDLE_SHOW_CLEAR_ITEM = "show_clear_item";

	public static final String KEY_PACKAGE_NAME = "PREF_PACKAGE_NAME";
	public static final String KEY_CLASS_NAME = "PREF_CLASS_NAME";
	public static final String KEY_APP_NAME = "PREF_APP_NAME";

	private final String EMAIL_TYPE = "message/rfc822";
	private final String EMAIL_AUTHOR = "sparkslab.tw@gmail.com";
	private final String EMAIL_SUBJECT = "[TKU Wi-Fi]";
	private final String WEBSITE_URL = "https://play.google.com/store/apps/developer?id=Sparks+Lab";
	private final String URL_DETAIL = "http://163.13.8.254/login_online_detail.php";

	private SharedPreferences prefs;

	private Preference pref_login, pref_logout, pref_connection_detail,
			pref_select_app, pref_version, pref_website;
	private EditTextPreference field_username, field_password;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.app_name));
		addPreferencesFromResource(R.xml.preferences);

		findViews();
		setListeners();
	}

	private void findViews() {
		prefs = getPreferenceManager().getSharedPreferences();

		pref_login = (Preference) findPreference(KEY_LOGIN_NOW);
		pref_logout = (Preference) findPreference(KEY_LOGOUT_NOW);
		pref_connection_detail = (Preference) findPreference(KEY_CONNECTION_DETAIL);
		pref_select_app = (Preference) findPreference(KEY_SELECT_APP);
		pref_version = (Preference) findPreference(KEY_VERSION);
		pref_website = (Preference) findPreference(KEY_WEBSITE);
		field_username = (EditTextPreference) findPreference(KEY_USERNAME);
		field_password = (EditTextPreference) findPreference(KEY_PASSWORD);
	}

	private void setListeners() {
		pref_login.setOnPreferenceClickListener(this);
		pref_logout.setOnPreferenceClickListener(this);
		pref_connection_detail.setOnPreferenceClickListener(this);
		pref_select_app.setOnPreferenceClickListener(this);
		pref_version.setOnPreferenceClickListener(this);
		pref_website.setOnPreferenceClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		updateUsernameSummary();
		updatePasswordSummary();
		updateVersionSummary();
		updateAppLaunchSummary();
	}

	@Override
	protected void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		((BaseAdapter) getPreferenceScreen().getRootAdapter())
				.notifyDataSetChanged();
		if (key.equals(KEY_ENABLED)) {
			updateEnabled();
		} else if (key.equals(KEY_USERNAME)) {
			updateUsernameSummary();
		} else if (key.equals(KEY_PASSWORD)) {
			updatePasswordSummary();
		}
	}

	private void updateAppLaunchSummary() {
		pref_select_app.setSummary(prefs.getString(KEY_APP_NAME, ""));
	}

	private void openSelectAppDialog() {
		boolean hadChoosedApp = Memory.getString(this, KEY_APP_NAME, "")
				.length() > 0;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_loader));
		builder.setMessage(hadChoosedApp ? String.format(
				getString(R.string.app_loader_select),
				prefs.getString(KEY_APP_NAME, ""))
				: getString(R.string.select_an_app));
		builder.setNegativeButton(getString(R.string.select_an_app),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Preferences.this,
								AppLoader.class));
					}
				});
		if (hadChoosedApp) {
			builder.setNeutralButton(getString(R.string.clear),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Memory.setString(Preferences.this,
									KEY_PACKAGE_NAME, "");
							Memory.setString(Preferences.this, KEY_CLASS_NAME,
									"");
							Memory.setString(Preferences.this, KEY_APP_NAME, "");

							updateAppLaunchSummary();
						}
					});
		}
		builder.setPositiveButton(getString(R.string.ok), null);
		builder.show();
	}

	private void updateEnabled() {
		boolean enabled = prefs.getBoolean(KEY_ENABLED, false);
		Utils.setEnableBroadcastReceiver(this, enabled);
	}

	private void updateUsernameSummary() {
		String username = prefs.getString(KEY_USERNAME, "");
		if (username.length() != 0) {
			field_username.setSummary(username);
		} else {
			field_username.setSummary(R.string.pref_username_summary);
		}
	}

	private void updatePasswordSummary() {
		String password = prefs.getString(KEY_PASSWORD, "");
		if (password.equals("")) {
			field_password.setSummary(String.format(
					getString(R.string.pref_password_summary),
					getString(R.string.input)));
		} else {
			field_password.setSummary(String.format(
					getString(R.string.pref_password_summary),
					getString(R.string.edit)));
		}
	}

	private void updateVersionSummary() {
		String versionSummary = String.format(getString(R.string.version),
				Utils.getVersionName(this));
		pref_version.setSummary(versionSummary);
	}

	private void sendFeedback() {
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.textsize_medium));
		editText.setHint(R.string.input_feedback);

		AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_menu_send)
				.setView(editText)
				.setTitle(R.string.send_feedback)
				.setPositiveButton(R.string.send,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Utils.closeSoftkeyboard(Preferences.this,
										editText);

								Intent i = new Intent(Intent.ACTION_SEND);
								i.setType(EMAIL_TYPE);
								i.putExtra(Intent.EXTRA_EMAIL,
										new String[] { EMAIL_AUTHOR });
								i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
								i.putExtra(
										Intent.EXTRA_TEXT,
										editText.getText().toString()
												+ "\n\n"
												+ Utils.getDebugInfo(Preferences.this));

								startActivity(Intent.createChooser(i,
										getString(R.string.choose_to_feedback)));
							}
						}).create();

		// workaround for keyboard not opening when AlertDialog has an
		// edittext inside
		alertDialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		alertDialog.show();

		final Button positiveButton = alertDialog
				.getButton(AlertDialog.BUTTON_POSITIVE);
		if (positiveButton != null) {
			positiveButton.setEnabled(false);
		}

		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (positiveButton != null) {
					positiveButton.setEnabled(s.length() > 0);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == pref_login) {
			Intent intent = new Intent(this, WifiLoginService.class);
			startService(intent);
		} else if (preference == pref_logout) {
			Intent i = new Intent(Preferences.this, WifiLogoutService.class);
			startService(i);
		} else if (preference == pref_connection_detail) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_DETAIL)));
		} else if (preference == pref_select_app) {
			openSelectAppDialog();
		} else if (preference == pref_website) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
			startActivity(i);
		} else if (preference == pref_version) {
			Intent i = new Intent(Intent.ACTION_VIEW,
					Uri.parse(Constant.MARKET_PREFIX + getPackageName()));
			startActivity(i);
		}

		return false;
	}
}