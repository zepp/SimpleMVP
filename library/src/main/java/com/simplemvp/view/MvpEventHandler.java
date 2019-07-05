/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class MvpEventHandler<S extends MvpState, P extends MvpPresenter<S>>
        implements MvpViewHandle<S>, MvpListener, LifecycleObserver {
    private final static int QUEUE_SIZE = 8;
    private final String tag = getClass().getSimpleName();
    private final WeakReference<MvpView<S, P>> reference;
    private final P presenter;
    private final Queue<S> queue = new ConcurrentLinkedQueue<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextWatcher> textWatchers = new ArrayList<>();
    private final List<SearchView.OnQueryTextListener> queryTextListeners = new ArrayList<>();
    private final AtomicBoolean isResumed = new AtomicBoolean();
    private final AtomicBoolean isQueueFlush = new AtomicBoolean();
    private volatile S lastState;

    MvpEventHandler(MvpView<S, P> view, P presenter, ReferenceQueue<MvpView<?, ?>> queue) {
        this.reference = new WeakReference<>(view, queue);
        this.presenter = presenter;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        MvpView<S, P> view = reference.get();
        isResumed.set(true);
        if (queue.isEmpty() && lastState != null) {
            view.onStateChanged(lastState);
        } else {
            Log.d(tag, "flushing event queue");
            flushQueue();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPaused() {
        isResumed.set(false);
    }

    @Override
    public void onClick(View v) {
        presenter.onViewClicked(v.getId());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        presenter.onOptionsItemSelected(item.getItemId());
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        presenter.onItemSelected(adapterView.getId(), adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        presenter.onItemSelected(adapterView.getId(), null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.onCheckedChanged(buttonView.getId(), isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        presenter.onRadioCheckedChanged(group.getId(), checkedId);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        presenter.onDrag(v.getId(), event);
        return true;
    }

    TextWatcher newTextWatcher(View view) {
        Log.d(tag, "new text watcher for view: " + view);
        TextWatcher watcher = new MvpTextWatcher<>(handler, presenter, view.getId());
        textWatchers.add(watcher);
        return watcher;
    }

    SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        Log.d(tag, "new query text listener for view: " + view);
        SearchView.OnQueryTextListener listener = new MvpOnQueryTextListener<>(handler, presenter, view.getId());
        queryTextListeners.add(listener);
        return listener;
    }

    @Override
    public void post(S state) {
        queue.offer(state);
        if (isResumed.get() && isQueueFlush.compareAndSet(false, true)) {
            handler.post(this::flushQueue);
        }
    }

    @Override
    public void finish() {
        MvpView<S, P> view = reference.get();
        if (view != null) {
            handler.post(view::finish);
        }
    }

    private void flushQueue() {
        int size = queue.size();
        int n = size / QUEUE_SIZE;
        while (!queue.isEmpty()) {
            S state = queue.poll();
            // process every n'th state in case of queue overflow
            if (n == 0 || size % n == 0) {
                lastState = state;
                MvpView<S, P> view = reference.get();
                if (view != null) {
                    view.onStateChanged(state);
                }
                size = queue.size();
                n = size / QUEUE_SIZE;
            }
            // set flag and then check queue size again to avoid cases when item is left unprocessed
            if (queue.isEmpty()) {
                isQueueFlush.set(false);
            }
        }
    }
}
