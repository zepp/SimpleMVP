/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;

import java.util.LinkedList;
import java.util.Queue;

final class MvpStateHandler<S extends MvpState> implements LifecycleObserver {
    private final static int QUEUE_SIZE = 8;
    private final String tag = getClass().getSimpleName();
    private final MvpView<?, S> view;
    private final Queue<S> queue = new LinkedList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isResumed;

    MvpStateHandler(MvpView<?, S> view) {
        this.view = view;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed = true;
        flushQueue();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPaused() {
        isResumed = false;
    }

    void post(S state) {
        if (isResumed) {
            view.onStateChanged(state);
        } else {
            queue.offer(state);
            while (queue.size() > QUEUE_SIZE) {
                queue.poll();
            }
        }
    }

    private void flushQueue() {
        Log.d(tag, "flushing event queue");
        while (!queue.isEmpty()) {
            handler.post(() -> view.onStateChanged(queue.poll()));
        }
    }
}
