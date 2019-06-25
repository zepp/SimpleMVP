/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class MvpViewImpl<S extends MvpState, P extends MvpPresenter<S>>
        implements MvpViewImplementation<S, P> {
    private final static int QUEUE_SIZE = 8;
    private final String tag = getClass().getSimpleName();
    private final MvpView<S, P> view;
    private final P presenter;
    private final Queue<S> queue = new ConcurrentLinkedQueue<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isResumed;

    MvpViewImpl(MvpView<S, P> view, P presenter) {
        this.view = view;
        this.presenter = presenter;
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

    @Override
    public void onClick(View v) {
        presenter.onViewClicked(v.getId());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        presenter.onItemSelected(adapterView.getId(), adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void post(S state) {
        if (isResumed) {
            handler.post(() -> view.onStateChanged(state));
        } else {
            queue.offer(state);
            while (queue.size() > QUEUE_SIZE) {
                queue.poll();
            }
        }
    }

    @Override
    public void finish() {
        handler.post(view::finish);
    }

    private void flushQueue() {
        Log.d(tag, "flushing event queue");
        while (!queue.isEmpty()) {
            handler.post(() -> view.onStateChanged(queue.poll()));
        }
    }
}
