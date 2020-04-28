package com.testapp;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

@SuppressWarnings("ApplySharedPref")
public final class AppState extends ContextWrapper {
    public final static String TIMER_STARTED_TIME = "timer-started-time";
    public final static String TIMER_STARTED = "timer-started";
    public final static String IS_POWER_SUPPLY = "power-supply";
    public final static String IS_CONNECTIVITY = "connectivity";
    private volatile static AppState instance;
    private final SharedPreferences preferences;

    private AppState(Context base) {
        super(base);
        preferences = getSharedPreferences(getClass().getSimpleName(), MODE_PRIVATE);
    }

    public static AppState getInstance(Context context) {
        if (instance == null) {
            synchronized (AppState.class) {
                if (instance == null) {
                    instance = new AppState(context);
                }
            }
        }
        return instance;
    }

    public long getTimerStartedTime() {
        return preferences.getLong(TIMER_STARTED_TIME, 0);
    }

    public void setTimerStartedTime(long value) {
        preferences.edit().putLong(TIMER_STARTED_TIME, value).commit();
    }

    public boolean isTimerStarted() {
        return preferences.getBoolean(TIMER_STARTED, false);
    }

    public void setTimerStarted(boolean value) {
        preferences.edit().putBoolean(TIMER_STARTED, value).commit();
    }

    public boolean isConnectivity() {
        return preferences.getBoolean(IS_CONNECTIVITY, false);
    }

    public void setConnectivity(boolean value) {
        preferences.edit().putBoolean(IS_CONNECTIVITY, value).commit();
    }

    public boolean isPowerSupply() {
        return preferences.getBoolean(IS_POWER_SUPPLY, false);
    }

    public void setPowerSupply(boolean value) {
        preferences.edit().putBoolean(IS_POWER_SUPPLY, value).commit();
    }
}
