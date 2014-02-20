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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class TutorialActivity extends Activity implements OnClickListener,
		OnFocusChangeListener, OnEditorActionListener, OnCheckedChangeListener {
	private TextView view_title;
	private Button btn_back, btn_next;
	private ProgressBar progressbar;

	private LinearLayout linearlayout, scrollview_layout;
	private EditText field_input;
	private TextView view_content;
	private ImageView image;
	private ScrollView scrollview;
	private CheckBox checkbox;
	private Button btn_select_auto_launch_app;
	private LinearLayout.LayoutParams center_horizontal_layoutparams;

	private SharedPreferences pref;
	private SharedPreferences.Editor editor;

	private String[] content_title;
	private int page = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.tutorial));
		setContentView(R.layout.tutorial_layout);

		findViews();
		setListeners();

		if (savedInstanceState != null) {
			page = savedInstanceState.getInt("page");
			changePage();
		}
	}

	@SuppressWarnings("deprecation")
	private void findViews() {
		content_title = getResources().getStringArray(R.array.tutorial_title);

		pref = PreferenceManager.getDefaultSharedPreferences(this);
		editor = pref.edit();

		view_title = (TextView) findViewById(R.id.view_title);
		btn_back = (Button) findViewById(R.id.btn_back);
		btn_next = (Button) findViewById(R.id.btn_next);
		progressbar = (ProgressBar) findViewById(R.id.progressbar);
		linearlayout = (LinearLayout) findViewById(R.id.linearlayout);

		field_input = new EditText(this);
		field_input.setSingleLine(true);
		field_input.setOnFocusChangeListener(this);
		field_input.setImeOptions(EditorInfo.IME_ACTION_DONE);
		field_input.setOnEditorActionListener(this);
		field_input.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.textsize_medium));

		center_horizontal_layoutparams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		center_horizontal_layoutparams.gravity = Gravity.CENTER_HORIZONTAL;

		view_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.textsize_large));

		view_content = new TextView(this);
		view_content.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.textsize_big));

		checkbox = new CheckBox(this);
		checkbox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources()
				.getDimension(R.dimen.textsize_medium));
		checkbox.setOnCheckedChangeListener(this);
		checkbox.setLayoutParams(center_horizontal_layoutparams);
		if (pref.getBoolean(Preferences.KEY_ENABLED, true)) {
			checkbox.setText(String.format(getString(R.string.auto_login),
					getString(R.string.enabled)));
			checkbox.setTextColor(getResources().getColor(R.color.green));
		} else {
			checkbox.setText(String.format(getString(R.string.auto_login),
					getString(R.string.disabled)));
			checkbox.setTextColor(getResources().getColor(R.color.red));
		}

		btn_select_auto_launch_app = new Button(this);
		String app_name = pref.getString(Preferences.KEY_APP_NAME, "");
		if (app_name.equals("")) {
			btn_select_auto_launch_app.setText(String.format(
					getString(R.string.select_auto_launch_app), ""));
		} else {
			btn_select_auto_launch_app.setText(String.format(
					getString(R.string.select_auto_launch_app),
					": " + pref.getString(Preferences.KEY_APP_NAME, "")));
		}
		btn_select_auto_launch_app.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getResources().getDimension(R.dimen.textsize_medium));
		btn_select_auto_launch_app
				.setLayoutParams(center_horizontal_layoutparams);

		scrollview = new ScrollView(this);
		scrollview.setFillViewport(true);

		scrollview_layout = new LinearLayout(this);
		scrollview_layout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		params.gravity = Gravity.CENTER;
		image = new ImageView(this);
		image.setLayoutParams(params);
		image.setScaleType(ScaleType.FIT_CENTER);

		progressbar.setMax(content_title.length);
		progressbar.setProgress(page + 1);

		changePage();
	}

	private void setListeners() {
		btn_back.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_select_auto_launch_app.setOnClickListener(this);
	}

	private void changePage() {
		toggleBackButton();
		toggleNextButton();

		view_title.setText(content_title[page]);

		linearlayout.removeAllViews();

		switch (page) {
		case 0:
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			params.gravity = Gravity.CENTER;
			params.weight = 1;
			linearlayout.setOrientation(LinearLayout.HORIZONTAL);
			if (!Utils.isScreenPortrait(this)) {
				ImageView image_developer = new ImageView(this);
				image_developer.setImageResource(R.drawable.devandroid);
				image_developer.setScaleType(ScaleType.FIT_CENTER);
				image_developer.setLayoutParams(params);
				linearlayout.addView(image_developer);
			}
			ImageView image_app = new ImageView(this);
			image_app.setImageResource(R.drawable.ic_launcher_web);
			image_app.setScaleType(ScaleType.FIT_CENTER);
			image_app.setLayoutParams(params);
			linearlayout.addView(image_app);
			break;
		case 1:
			field_input.setText(pref.getString(Preferences.KEY_USERNAME, ""));
			field_input.setSelection(field_input.getText().toString().length());
			field_input.setInputType(InputType.TYPE_CLASS_TEXT);

			linearlayout.setOrientation(LinearLayout.VERTICAL);
			linearlayout.addView(field_input);
			field_input.clearFocus();
			field_input.requestFocus();
			break;
		case 2:
			field_input.setText(pref.getString(Preferences.KEY_PASSWORD, ""));
			field_input.setSelection(field_input.getText().toString().length());
			field_input.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);

			linearlayout.setOrientation(LinearLayout.VERTICAL);
			linearlayout.addView(field_input);
			field_input.clearFocus();
			field_input.requestFocus();
			break;
		case 3:
			checkbox.setChecked(pref.getBoolean(Preferences.KEY_ENABLED, true));
			view_content.setText(getString(R.string.auto_login_tutorial));

			scrollview_layout.removeAllViews();
			scrollview_layout.addView(checkbox);
			scrollview_layout.addView(view_content);

			scrollview.removeAllViews();
			scrollview.addView(scrollview_layout);
			linearlayout.setOrientation(LinearLayout.VERTICAL);
			linearlayout.addView(scrollview);
			break;
		case 4:
			view_content.setText(getString(R.string.auto_launch_app_tutorial));

			scrollview_layout.removeAllViews();
			scrollview_layout.addView(btn_select_auto_launch_app);
			scrollview_layout.addView(view_content);

			scrollview.removeAllViews();
			scrollview.addView(scrollview_layout);
			linearlayout.setOrientation(LinearLayout.VERTICAL);
			linearlayout.addView(scrollview);
			break;
		case 5:
			if (Utils.isScreenPortrait(this)) {
				image.setImageResource(R.drawable.widget_screenshot);
			} else {
				image.setImageResource(R.drawable.widget_screenshot_landscape);
			}
			linearlayout.setOrientation(LinearLayout.VERTICAL);
			linearlayout.addView(image);
		}
	}

	private void toggleBackButton() {
		if (page == 0) {
			btn_back.setEnabled(false);
		} else {
			btn_back.setEnabled(true);
		}
	}

	private void toggleNextButton() {
		if (page == content_title.length - 1) {
			btn_next.setText(getString(R.string.lets_start));
		} else {
			btn_next.setText(getString(R.string.next));
		}
	}

	private void nextPage() {
		page++;
		progressbar.setProgress(page + 1);
		if (page <= content_title.length - 1) {
			changePage();
		} else {
			setResult(RESULT_OK, new Intent());
			finish();
		}
	}

	private void previousPage() {
		page--;
		progressbar.setProgress(page + 1);
		if (page <= content_title.length - 1) {
			changePage();
		}
	}

	private void savePreference() {
		switch (page) {
		case 1:
			editor.putString(Preferences.KEY_USERNAME, field_input.getText()
					.toString());
			break;
		case 2:
			editor.putString(Preferences.KEY_PASSWORD, field_input.getText()

			.toString());
			break;
		case 3:
			editor.putBoolean(Preferences.KEY_ENABLED, checkbox.isChecked());
			break;
		}

		editor.commit();
	}

	@Override
	public void onClick(View v) {
		if (v == btn_next) {
			if (page == 1 || page == 2) {
				Utils.toggleSoftKeyboard(this, false, field_input);
				if (field_input.getText().toString().length() > 0) {
					savePreference();
					nextPage();
				}
			} else if (page == 3) {
				savePreference();
				nextPage();
			} else {
				nextPage();
			}
		} else if (v == btn_back) {
			if (page == 1 || page == 2) {
				Utils.toggleSoftKeyboard(this, false, field_input);
			}
			previousPage();
		} else if (v == btn_select_auto_launch_app) {
			if (page == 4) {
				startActivityForResult(new Intent(this, AppLoader.class), 100);
			}
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (v == field_input) {
			if (hasFocus) {
				Utils.toggleSoftKeyboard(this, true, field_input);
			}
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (v == field_input && actionId == EditorInfo.IME_ACTION_DONE) {
			if (page == 1 || page == 2) {
				Utils.toggleSoftKeyboard(this, false, field_input);
				if (field_input.getText().toString().length() > 0) {
					savePreference();
					nextPage();
				}
			} else {
				nextPage();
			}
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (page > 0) {
				if (page == 1 || page == 2) {
					Utils.toggleSoftKeyboard(this, false, field_input);
				}
				previousPage();
			}
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView == checkbox) {
			if (isChecked) {
				checkbox.setText(String.format(getString(R.string.auto_login),
						getString(R.string.enabled)));
				checkbox.setTextColor(getResources().getColor(R.color.green));
			} else {
				checkbox.setText(String.format(getString(R.string.auto_login),
						getString(R.string.disabled)));
				checkbox.setTextColor(getResources().getColor(R.color.red));
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100 && resultCode == RESULT_OK) {
			String app_name = pref.getString(Preferences.KEY_APP_NAME, "");
			if (app_name.equals("")) {
				btn_select_auto_launch_app.setText(String.format(
						getString(R.string.select_auto_launch_app), ""));
			} else {
				btn_select_auto_launch_app.setText(String.format(
						getString(R.string.select_auto_launch_app),
						": " + pref.getString(Preferences.KEY_APP_NAME, "")));
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("page", page);
		savePreference();
	}
}
