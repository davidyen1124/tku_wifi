package com.devandroid.tkuautowifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

		findViews();
		setListeners();
		getBundle();

		new LoadingPackage().execute();
	}

	private void findViews() {
		listview = new ListView(this);
		setContentView(listview);

		list_appinfo = new ArrayList<AppInfo>();

		adapter = new MyAdapter();
		listview.setAdapter(adapter);

		pm = getPackageManager();
	}

	private void setListeners() {
		listview.setOnItemClickListener(this);
	}

	private void getBundle() {
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);
		show_clear_item = !pref.getString(Preferences.KEY_APP_NAME, "").equals(
				"");
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

			AppInfo item = new AppInfo();

			if (show_clear_item) {
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

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = prefs.edit();

			editor.putString(Preferences.KEY_PACKAGE_NAME, appInfo.packageName);
			editor.putString(Preferences.KEY_CLASS_NAME, appInfo.className);
			if (appInfo.label.equals(getString(R.string.clear))) {
				editor.putString(Preferences.KEY_APP_NAME, "");
			} else {
				editor.putString(Preferences.KEY_APP_NAME, appInfo.label);
			}
			editor.commit();

			if (!appInfo.label.equals(getString(R.string.clear))) {
				Toast.makeText(
						AppLoader.this,
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
