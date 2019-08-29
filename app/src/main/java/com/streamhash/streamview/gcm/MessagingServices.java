package com.streamhash.streamview.gcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.streamhash.streamview.R;
import com.streamhash.streamview.ui.activity.MainActivity;
import com.streamhash.streamview.ui.activity.VideoPageActivity;

import java.util.Map;

public class MessagingServices extends FirebaseMessagingService {

    private static final String TAG = "FCM Message";

    public MessagingServices() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            Log.d("msg", "onMessageReceived: " + remoteMessage.getData().get("message"));
            Log.e("data", "msg" + remoteMessage.getData());
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d(TAG, "key, " + key + " value " + value);
            }
            Log.e("video_id", "admin_video_id" + remoteMessage.getData().get("admin_video_id"));
            String video_id = remoteMessage.getData().get("admin_video_id");
            Intent intent = new Intent(getApplicationContext(), VideoPageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("videoID", video_id);
            intent.putExtra("userName", "");
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String channelId = "Default";
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("StreamView")
                    .setContentText(remoteMessage.getNotification().getBody()).setAutoCancel(true).setContentIntent(pendingIntent);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }
            if (manager != null) {
                manager.notify(0, builder.build());
            }
        } catch (Exception e) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            e.printStackTrace();
        }
    }
}


