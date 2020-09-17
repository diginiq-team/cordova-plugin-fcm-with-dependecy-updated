package com.gae.scaffolder.plugin;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {
    public int counter = 0;
    public AlarmService(Context applicationContext) {
        super();
        Log.i("AlarmService HERE", "here I am!");
    }

    public AlarmService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean IsGPSEnabled = false;

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            Class mainActivity;
            Context context = getApplicationContext();
            String  packageName = context.getPackageName();
            Intent  launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
            String  className = launchIntent.getComponent().getClassName();

            try {
                //loading the Main Activity to not import it in the plugin
                mainActivity = Class.forName(className);

                Intent notificationIntent = new Intent(this, mainActivity);
                notificationIntent.setAction(Long.toString(System.currentTimeMillis()));
                Notification.Builder mBuilder = new Notification.Builder(this);
                mBuilder.setSmallIcon(context.getResources().getIdentifier("ic_launcher","mipmap",context.getPackageName()))
                        .setContentTitle(context.getResources().getString(context.getResources().getIdentifier("app_name","string",context.getPackageName())))
                        .setContentText("Alarm Service")
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));


                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel notificationChannel = new NotificationChannel("ALARM-SERVICE", "Alarm Service", importance);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

                assert mNotificationManager != null;
                mBuilder.setChannelId("ALARM-SERVICE");
                mNotificationManager.createNotificationChannel(notificationChannel);

                startForeground(100001, mBuilder.getNotification());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("mobis.poldakepri.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); //true will remove notification
        }
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if(counter > 60) {
                    counter = 1;
                }
                Log.i("AlarmService in timer", "in timer ++++  "+ (counter++));
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
    }
}
