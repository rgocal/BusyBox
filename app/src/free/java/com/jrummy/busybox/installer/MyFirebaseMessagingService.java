package com.jrummy.busybox.installer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jrummy.busybox.installer.activities.MainActivity;
import com.jrummyapps.busybox.R;

import java.util.Map;

/**
 * Created by yaroslavsudnik on 11/07/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String TITLE_KEY   = "title";
    private final String MESSAGE_KEY = "message";
    private final String URI_KEY     = "web_link";

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> dataMap = remoteMessage.getData();

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_status_bar_icon)
            .setContentTitle(dataMap.get(TITLE_KEY))
            .setContentText(dataMap.get(MESSAGE_KEY))
            .setAutoCancel(true)
            .setSound(defaultSoundUri);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = MainActivity.linkIntent(this, dataMap.get(URI_KEY));

        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT);

        notificationBuilder.setContentIntent(pending);

        notificationManager.notify(0, notificationBuilder.build());
    }

}