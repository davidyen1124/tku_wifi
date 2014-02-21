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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.devandroid.tkuautowifi.Memory;
import com.devandroid.tkuautowifi.R;
import com.devandroid.tkuautowifi.R.id;
import com.devandroid.tkuautowifi.R.layout;
import com.devandroid.tkuautowifi.R.string;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AppLoader extends Activity implements OnItemClickListener {
	private ListView listview;
	private MyAdapter adapter;
	private ArrayList<AppInfo> list_appinfo;
	private PackageManager pm;

	private boolean show_clear_item = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initialValues();
		findViews();
		setListeners();

		new LoadingPackage().execute();
	}

	private void initialValues() {
		show_clear_item = !Memory.getString(getApplicationContext(),
				Preferences.KEY_APP_NAME, "").equals("");

		list_appinfo = new ArrayList<AppLoader.AppInfo>();
		pm = getPackageManager();
	}

	private void findViews() {
		listview = new ListView(this);
		setContentView(listview);

		adapter = new MyAdapter();
		listview.setAdapter(adapter);
	}

	private void setListeners() {
		listview.setOnItemClickListener(this);
	}

	class AppInfo {
		public String packageName;
		public String className;
		public Drawable icon;
		public String label;
	}

	class LoadingPackage extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog progressDialog;

		@Override
		protected Void doInBackground(Void... params) {
			list_appinfo.clear();

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(
					mainIntent, PackageManager.GET_ACTIVITIES);
			Collections.sort(resolveInfoList,
					new ResolveInfo.DisplayNameComparator(pm));

			AppInfo item = null;

			if (show_clear_item) {
				item = new AppInfo();
				item.packageName = "";
				item.className = "";
				item.icon = getResources().getDrawable(
						android.R.drawable.ic_menu_close_clear_cancel);
				item.label = getString(R.string.clear);
				list_appinfo.add(item);
			}

			for (ResolveInfo resolveInfo : resolveInfoList) {
				item = new AppInfo();
				item.packageName = resolveInfo.activityInfo.packageName;
				item.className = resolveInfo.activityInfo.name;
				item.icon = resolveInfo.loadIcon(pm);
				item.label = (String) resolveInfo.loadLabel(pm);
				list_appinfo.add(item);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			adapter.notifyDataSetChanged();
			progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = ProgressDialog.show(AppLoader.this,
					getString(R.string.please_wait),
					getString(R.string.loading_apps));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position,
			long arg3) {
		if (arg0 == listview) {
			AppInfo appInfo = new AppInfo();
			appInfo = list_appinfo.get(position);

			boolean isSelectClear = appInfo.label
					.equals(getString(R.string.clear));
			Memory.setString(this, Preferences.KEY_APP_NAME, isSelectClear ? ""
					: appInfo.label);
			Memory.setString(this, Preferences.KEY_PACKAGE_NAME,
					isSelectClear ? "" : appInfo.packageName);
			Memory.setString(this, Preferences.KEY_CLASS_NAME,
					isSelectClear ? "" : appInfo.className);

			if (!isSelectClear) {
				Toast.makeText(
						this,
						String.format(getString(R.string.app_loader_select),
								appInfo.label), Toast.LENGTH_SHORT).show();
			}

			setResult(RESULT_OK, new Intent());
			finish();
		}
	}

	class MyAdapter extends BaseAdapter {
		LayoutInflater inflater;

		public MyAdapter() {
			inflater = LayoutInflater.from(AppLoader.this);
		}

		@Override
		public int getCount() {
			return list_appinfo.size();
		}

		@Override
		public Object getItem(int position) {
			return list_appinfo.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.apploader_layout, null);

				holder = new ViewHolder();
				holder.image_icon = (ImageView) convertView
						.findViewById(R.id.image_icon);
				holder.view_label = (TextView) convertView
						.findViewById(R.id.view_label);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.image_icon.setImageDrawable(list_appinfo.get(position).icon);
			holder.view_label.setText(list_appinfo.get(position).label);

			return convertView;
		}

		class ViewHolder {
			ImageView image_icon;
			TextView view_label;
		}
	}
}
