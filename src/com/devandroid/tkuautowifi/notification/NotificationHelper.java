package com.devandroid.tkuautowifi.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.devandroid.tkuautowifi.R;

public class NotificationHelper {
	public static void createNotification(Context context, int notificationId,
			PendingIntent contentIntent, String tickerText,
			String contentTitle, String contentText, boolean ongoing) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context)
				.setLargeIcon(
						BitmapFactory.decodeResource(context.getResources(),
								R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.notification_small_icon)
				.setTicker(tickerText).setContentTitle(contentTitle)
				.setContentText(contentText).setContentIntent(contentIntent)
				.setOngoing(ongoing);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, mBuilder.build());
	}

	public static void cancelNotification(Context context, int notificationId) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
	}
}
