package com.testapp;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.simplemvp.presenter.MvpPresenterManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestApplication extends Application {
    private final static String tag = TestApplication.class.getSimpleName();
    private final static int ERROR_NOTIFICATION_ID = 1;
    private MvpPresenterManager presenterManager;
    private NotificationManager notificationManager;
    private ExecutorService executor;
    private Consumer<Throwable> errorHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        executor = Executors.newSingleThreadExecutor();
        errorHandler = error -> {
            notificationManager
                    .notify(ERROR_NOTIFICATION_ID, getErrorNotification(error.getMessage()));
            Log.e(tag, "error: ", error);
        };
        presenterManager = MvpPresenterManager.getInstance(this);
        presenterManager.initialize(executor, errorHandler);
    }

    Notification getErrorNotification(String text) {
        NotificationCompat.Builder builder = getBuilder(true)
                .setSmallIcon(R.drawable.ic_error)
                .setContentTitle(getString(R.string.app_error))
                .setContentText(text);
        return builder.build();
    }

    private NotificationCompat.Builder getBuilder(boolean headUp) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(),
                null).setLocalOnly(true);
        if (headUp) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setVibrate(new long[]{0});
        }
        return builder;
    }

}
