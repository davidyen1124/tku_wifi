package com.devandroid.tkuautowifi;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class LoginAppWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int i = 0; i < appWidgetIds.length; i++) {
			PendingIntent pending_intent = PendingIntent.getService(context, 0,
					new Intent(context, WifiLogin.class), 0);
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.login_appwidget_layout);
			views.setOnClickPendingIntent(R.id.button, pending_intent);

			appWidgetManager.updateAppWidget(appWidgetIds[i], views);
		}
	}
}
