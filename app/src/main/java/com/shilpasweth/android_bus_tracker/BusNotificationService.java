package com.shilpasweth.android_bus_tracker;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static com.shilpasweth.android_bus_tracker.MapsActivity.mBuses;

/**
 * Created by shilpa on 3/24/2018.
 */

public class BusNotificationService extends IntentService {
    @Override
    protected void onHandleIntent(Intent workIntent) {
        Log.d("BusNotificationService","Entered onHandleIntent");
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        // Do work here, based on the contents of dataString


        final CountDownTimer busNotify = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //mTextField.setText("done!");
                Log.d("BusNotificationService","Entered CountDownTimer-onFinish()");


                this.start();

                Log.d("BusNotificationService","Exited CountDownTimer-onFinish()");
            }
        };

        busNotify.start();







    }

    public void createNearNotification(){
        String CHANNEL_ID="C1";

        Date currentTime = Calendar.getInstance().getTime();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.bus_marker)
                .setContentTitle("Start Notification")
                .setContentText("Howdy Folks!"+currentTime.toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "Channel";
            String description = "This is a channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, mBuilder.build());
        }
    }

    public BusNotificationService() {
        super("DisplayNotification");
    }
}
