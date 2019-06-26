/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class MvpViewImpl<S extends MvpState, P extends MvpPresenter<S>>
        implements MvpViewImplementation<S, P> {
    private final static int DELAY = 200;
    private final static int QUEUE_SIZE = 8;
    private final String tag = getClass().getSimpleName();
    private final WeakReference<MvpView<S, P>> reference;
    private final P presenter;
    private final Queue<S> queue = new ConcurrentLinkedQueue<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextWatcher> textWatchers = new ArrayList<>();
    private final AtomicBoolean isResumed = new AtomicBoolean();

    MvpViewImpl(MvpView<S, P> view, P presenter, ReferenceQueue<MvpView<?, ?>> queue) {
        this.reference = new WeakReference<>(view, queue);
        this.presenter = presenter;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed.set(true);
        flushQueue();
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
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.onCheckedChanged(buttonView.getId(), isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        presenter.onCheckedChanged(checkedId, true);
    }

    @Override
    public TextWatcher newTextWatcher(View view) {
        Log.d(tag, "new text watcher for view: " + view);
        TextWatcher watcher = new TextWatcherImpl(view.getId());
        textWatchers.add(watcher);
        return watcher;
    }

    @Override
    public void post(S state) {
        if (isResumed.get()) {
            MvpView<S, P> view = reference.get();
            if (view != null) {
                handler.post(() -> view.onStateChanged(state));
            }
        } else {
            queue.offer(state);
            while (queue.size() > QUEUE_SIZE) {
                queue.poll();
            }
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
        MvpView<S, P> view = reference.get();
        Log.d(tag, "flushing event queue");
        while (!queue.isEmpty()) {
            S state = queue.poll();
            if (view != null) {
                handler.post(() -> view.onStateChanged(state));
            }
        }
    }

    private class TextWatcherImpl implements TextWatcher {
        @IdRes
        final int viewId;
        String text = "";
        boolean isSendTextPosted;
        long millis;

        TextWatcherImpl(int viewId) {
            this.viewId = viewId;
        }

        void sendText() {
            long delta = System.currentTimeMillis() - millis;
            if (delta > DELAY) {
                presenter.onTextChanged(viewId, text);
                isSendTextPosted = false;
            } else {
                handler.postDelayed(this::sendText, DELAY - delta);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            millis = System.currentTimeMillis();
            text = s.toString();
            if (!isSendTextPosted) {
                isSendTextPosted = true;
                handler.postDelayed(this::sendText, DELAY);
            }
        }
    }
}
