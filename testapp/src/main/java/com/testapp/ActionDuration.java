package com.testapp;

import android.support.design.widget.Snackbar;
import android.widget.Toast;

enum ActionDuration {
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

    @Override
    public String toString() {
        return description;
    }
}
