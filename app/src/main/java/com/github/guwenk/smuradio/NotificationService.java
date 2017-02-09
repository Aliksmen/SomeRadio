package com.github.guwenk.smuradio;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.Toast;


public class NotificationService extends Service{
    String LOG_TAG = "NotificationService";
    RemoteViews views;
    boolean ready = false;
    MyBinder binder = new MyBinder();
    MainActivity activity;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {
            showNotification();
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        } else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            Log.i(LOG_TAG, "Clicked Play");
            activity.doPlayPause();
            new ButtonTimeout((ImageButton)activity.findViewById(R.id.status_bar_play), 300);
        } else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            activity.changeRadioStatus();
            activity.stopBASS();
            stopForeground(true);
            stopSelf();
        }
        return START_STICKY;
    }

    Notification status;
    private void showNotification() {
        views = new RemoteViews(getPackageName(),
                R.layout.notification_layout);


        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);


        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_stop_24dp);
        views.setInt(R.id.small_notification_bg, "setBackgroundResource", R.color.notificationOrange);

        views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        ready = true;

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_radio_24dp;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    public void toStopButton(){
        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_play_arrow_24dp);
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }
    public void toPlayButton(){
        views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_stop_24dp);
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }
    public void refreshTitle(String title){
        Log.d(LOG_TAG, "Changing title");
        views.setTextViewText(R.id.status_bar_track_name, title);
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void registerClient(MainActivity activity){
        this.activity = activity;
    }
    class MyBinder extends Binder{
        NotificationService getService(){
            return NotificationService.this;
        }
    }
}