package com.dnasoftware.smartdnsproxy.BroadCastReceivers;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import retrofit.RestAdapter;

/**
 * This class is in charge of trying to update the IP every time the user changes to a WiFi network.
 *
 * <p>Created by Matias Radzinski on 06/07/2014, 03:47 AM.</p>
 */
public class NotificationUpdateInputReceiver extends BroadcastReceiver {
    private static final String API_URL = "http://www.smartdnsproxy.com/api/IP/update";
    private static final String ACCOUNT_ID = "14053957f2c54cd";

    private Handler handler;

    @Override
    public void onReceive(Context context, Intent intent) {
        cancelUpdateNotification(context);

        String currentIP = intent.getAction();
        if(currentIP.equals("NO")){ return; }

        handler = new Handler();

        updateIP(context, currentIP);
    }


    protected void updateIP(final Context context, final String IP){
        Thread thread = new Thread(new Runnable() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void run() {
                RestAdapter adapter = new RestAdapter.Builder()
                        .setEndpoint(API_URL)
                        .build();

                SmartDNSProxy dsp = adapter.create(SmartDNSProxy.class);
                final SmartDNSProxyAPIResponse response = dsp.Update(ACCOUNT_ID);

                if(response.getStatus() == 0){
                    final SharedPreferences pref = context.getSharedPreferences("DNSSmartProxy", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("IP", IP);

                    editor.apply();
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        displayResult(context, response.getMessage(), response.getStatus());
                    }
                });
            }
        });

        thread.start();
    }

    protected void displayResult(final Context context, final String message, int Status){
        Intent intent = new Intent(context, NotificationUpdateInputReceiver.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        Notification notification;
        notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentTitle("IP Update Result")
                .setContentText(message)
                .setTicker(message)
                .setContentIntent(pIntent)
                .setLights(Color.WHITE, 1, 0)
                .setDefaults(Notification.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();

        notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

        if(Status == 0){
            notification.largeIcon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_dialog_info);
            notification.icon = android.R.drawable.ic_dialog_info;
        } else {
            notification.largeIcon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_dialog_alert);
            notification.icon = android.R.drawable.ic_dialog_alert;
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(27087226, notification);
    }

    private void cancelUpdateNotification(Context ctx){
        String  s = Context.NOTIFICATION_SERVICE;
        NotificationManager mNM = (NotificationManager) ctx.getSystemService(s);
        mNM.cancel(91345636);
    }
}