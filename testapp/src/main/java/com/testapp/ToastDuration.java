package com.testapp;

import android.widget.Toast;

enum ToastDuration {
    LongDuration(Toast.LENGTH_LONG, "Long"),
    ShortDuration(Toast.LENGTH_SHORT, "Short");

    int duration;
    String description;

    ToastDuration(int duration, String description) {
        this.duration = duration;
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
