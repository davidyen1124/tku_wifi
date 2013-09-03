package com.devandroid.tkuautowifi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

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
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

public class Preferences extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnPreferenceClickListener {

	public static final String KEY_LOGIN_NOW = "login_now";
	public static final String KEY_LOGOUT_NOW = "logout_now";
	public static final String KEY_SELECT_APP = "select_app_launch";
	public static final String KEY_ENABLED = "enabled";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ERROR_NOTIFY = "error_notify";
	public static final String KEY_ERROR_NOTIFY_SOUND = "error_notify_sound";
	public static final String KEY_ERROR_NOTIFY_VIBRATE = "error_notify_vibrate";
	public static final String KEY_ERROR_NOTIFY_LIGHTS = "error_notify_lights";
	public static final String KEY_TOAST_NOTIFY = "toast_notify";
	public static final String KEY_TOAST_NOTIFY_SUCCESS = "toast_notify_success";
	public static final String KEY_TOAST_NOTIFY_NOT_REQUIRED = "toast_notify_not_required";
	public static final String KEY_VERSION = "version";
	public static final String KEY_WEBSITE = "website";
	public static final String KEY_AUTHOR = "author";
	public static final String KEY_FIRST_TIME_OPEN = "first_time_open";
	public static final String KEY_START_TUTORIAL = "start_tutorial";
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
			pref_select_app, pref_version, pref_website, pref_author;
	private EditTextPreference field_username, field_password;
	private PreferenceScreen pref_success, pref_error;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		findViews();
		setListeners();
		checkStartTutorial();
	}

	private void findViews() {
		prefs = getPreferenceManager().getSharedPreferences();

		setTitle(getString(R.string.app_name));

		pref_login = (Preference) findPreference(KEY_LOGIN_NOW);
		pref_logout = (Preference) findPreference(KEY_LOGOUT_NOW);
		pref_connection_detail = (Preference) findPreference(KEY_CONNECTION_DETAIL);
		pref_select_app = (Preference) findPreference(KEY_SELECT_APP);
		pref_version = (Preference) findPreference(KEY_VERSION);
		pref_website = (Preference) findPreference(KEY_WEBSITE);
		pref_author = (Preference) findPreference(KEY_AUTHOR);
		field_username = (EditTextPreference) findPreference(KEY_USERNAME);
		field_password = (EditTextPreference) findPreference(KEY_PASSWORD);
		pref_success = (PreferenceScreen) findPreference(KEY_TOAST_NOTIFY);
		pref_error = (PreferenceScreen) findPreference(KEY_ERROR_NOTIFY);
	}

	private void setListeners() {
		pref_login.setOnPreferenceClickListener(this);
		pref_logout.setOnPreferenceClickListener(this);
		pref_connection_detail.setOnPreferenceClickListener(this);
		pref_select_app.setOnPreferenceClickListener(this);
		pref_version.setOnPreferenceClickListener(this);
		pref_website.setOnPreferenceClickListener(this);
		pref_author.setOnPreferenceClickListener(this);
	}

	private void checkStartTutorial() {
		if (prefs.getBoolean(KEY_FIRST_TIME_OPEN, true)) {
			prefs.edit().putBoolean(KEY_FIRST_TIME_OPEN, false).commit();

			startActivityForResult(new Intent(this, TutorialActivity.class),
					100);
		} else {
			if (prefs.getString(KEY_USERNAME, "").equals("")
					&& prefs.getString(KEY_PASSWORD, "").equals("")) {
				prefs.edit().putBoolean(KEY_FIRST_TIME_OPEN, false).commit();

				startActivityForResult(
						new Intent(this, TutorialActivity.class), 100);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		updateUsernameSummary();
		updatePasswordSummary();
		updateErrorNotificationSummary();
		updateToastNotificationSummary();
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
		} else if (key.equals(KEY_ERROR_NOTIFY_SOUND)
				|| key.equals(KEY_ERROR_NOTIFY_VIBRATE)
				|| key.equals(KEY_ERROR_NOTIFY_LIGHTS)) {
			updateErrorNotificationSummary();
		} else if (key.equals(KEY_TOAST_NOTIFY_SUCCESS)
				|| key.equals(KEY_TOAST_NOTIFY_NOT_REQUIRED)) {
			updateToastNotificationSummary();
		}
	}

	private void updateAppLaunchSummary() {
		pref_select_app.setSummary(prefs.getString(KEY_APP_NAME, ""));
	}

	private void openSelectAppDialog() {
		new AlertDialog.Builder(this)
				.setTitle(
						getString(R.string.menu_select_app_launch_title)
								.toString())
				.setMessage(
						String.format(getString(R.string.app_loader_select),
								prefs.getString(KEY_APP_NAME, "")))
				.setPositiveButton(getString(R.string.ok).toString(), null)
				.setNegativeButton(
						getString(R.string.select_an_app).toString(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								startActivity(new Intent(Preferences.this,
										AppLoader.class));
							}
						})
				.setNeutralButton(getString(R.string.clear),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								SharedPreferences.Editor editor = prefs.edit();
								editor.putString(Preferences.KEY_PACKAGE_NAME,
										"");
								editor.putString(Preferences.KEY_CLASS_NAME, "");
								editor.putString(Preferences.KEY_APP_NAME, "");
								editor.commit();

								updateAppLaunchSummary();
							}
						}).show();
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

	private void updateErrorNotificationSummary() {
		ArrayList<String> methods = new ArrayList<String>();

		if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_SOUND, false)) {
			methods.add(getString(R.string.pref_error_notify_sound));
		}
		if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_VIBRATE, false)) {
			methods.add(getString(R.string.pref_error_notify_vibrate));
		}
		if (prefs.getBoolean(Preferences.KEY_ERROR_NOTIFY_LIGHTS, false)) {
			methods.add(getString(R.string.pref_error_notify_lights));
		}

		if (methods.size() == 0) {
			pref_error.setSummary(R.string.pref_error_notify_none);
		} else {
			String summaryStr = join(methods,
					getString(R.string.pref_error_notify_deliminator));
			pref_error.setSummary(summaryStr);
		}
	}

	private void updateToastNotificationSummary() {
		ArrayList<String> methods = new ArrayList<String>();

		if (prefs.getBoolean(KEY_TOAST_NOTIFY_SUCCESS, true)) {
			methods.add(getString(R.string.pref_toast_notify_success));
		}
		if (prefs.getBoolean(KEY_TOAST_NOTIFY_NOT_REQUIRED, true)) {
			methods.add(getString(R.string.pref_toast_notify_not_required));
		}

		if (methods.size() == 0) {
			pref_success.setSummary(R.string.pref_error_notify_none);
		} else {
			String summaryStr = join(methods,
					getString(R.string.pref_error_notify_deliminator));
			pref_success.setSummary(summaryStr);
		}
	}

	private void updateVersionSummary() {
		String versionSummary = String.format(
				getString(R.string.pref_version_summary),
				Utils.getVersionName(this));
		pref_version.setSummary(versionSummary);
	}

	private static String join(Collection<String> col, String deliminator) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> iter = col.iterator();

		sb.append(iter.next());
		while (iter.hasNext()) {
			sb.append(deliminator);
			sb.append(iter.next());
		}
		return sb.toString();
	}

	private void login() {
		Intent i = new Intent(Preferences.this, WifiLogin.class);
		startService(i);
	}

	private void logout() {
		Intent i = new Intent(Preferences.this, WifiLogout.class);
		startService(i);
	}

	private void openMoreApps() {
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(WEBSITE_URL));
		startActivity(i);
	}

	private void openAppLink() {
		Intent i = new Intent(Intent.ACTION_VIEW,
				Uri.parse(Constant.MARKET_PREFIX + getPackageName()));
		startActivity(i);
	}

	private void sendFeedback() {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType(EMAIL_TYPE);
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { EMAIL_AUTHOR });
		i.putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT);
		i.putExtra(Intent.EXTRA_TEXT, Utils.getDebugInfo(this));

		startActivity(Intent.createChooser(i,
				getString(R.string.choose_to_feedback)));
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (preference == pref_login) {
			login();
		} else if (preference == pref_logout) {
			logout();
		} else if (preference == pref_connection_detail) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_DETAIL)));
		} else if (preference == pref_select_app) {
			openSelectAppDialog();
		} else if (preference == pref_website) {
			openMoreApps();
		} else if (preference == pref_version) {
			openAppLink();
		} else if (preference == pref_author) {
			sendFeedback();
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {
			startActivity(new Intent(this, Preferences.class));
			finish();
		}
	}
}