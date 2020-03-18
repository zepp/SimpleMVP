package com.testapp.common;

import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public enum ActionDuration {
    LongDuration(Toast.LENGTH_LONG, Snackbar.LENGTH_LONG, "Long"),
    ShortDuration(Toast.LENGTH_SHORT, Snackbar.LENGTH_SHORT, "Short");

    int toastDuration;
    int snackBarDuration;
    String description;

    ActionDuration(int duration, int snackBarDuration, String description) {
        this.toastDuration = duration;
        this.snackBarDuration = snackBarDuration;
        this.description = description;
    }

    public int getToastDuration() {
        return toastDuration;
    }

    public int getSnackBarDuration() {
        return snackBarDuration;
    }

    @Override
    public String toString() {
        return description;
    }
}
