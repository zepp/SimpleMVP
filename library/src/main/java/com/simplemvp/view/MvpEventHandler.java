/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.functions.Consumer;

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
    private final ReferenceQueue<MvpView<S, P>> referenceQueue;
    private final P presenter;
    private final Queue<EventRunnable> events = new ConcurrentLinkedQueue<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<TextWatcher> textWatchers = new ArrayList<>();
    private final List<SearchView.OnQueryTextListener> queryTextListeners = new ArrayList<>();
    private final AtomicBoolean isFirstStateChange = new AtomicBoolean(true);
    private final AtomicBoolean isEnabled = new AtomicBoolean();
    private final AtomicBoolean isResumed = new AtomicBoolean();
    private final AtomicBoolean isQueueDraining = new AtomicBoolean();
    private volatile EventRunnable lastStateEvent;

    MvpEventHandler(MvpView<S, P> view, P presenter) {
        this.referenceQueue = new ReferenceQueue<>();
        this.reference = new WeakReference<>(view, referenceQueue);
        this.presenter = presenter;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed.set(true);
        if (isResumed()) {
            if (events.isEmpty()) {
                handleLastState();
            } else {
                Log.d(tag, "flushing event queue");
                drainEventQueue();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPaused() {
        isResumed.set(false);
        isFirstStateChange.set(true);
    }

    @Override
    public void onClick(View v) {
        presenter.onViewClicked(this, v.getId());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        presenter.onOptionsItemSelected(this, item.getItemId());
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        presenter.onItemSelected(this, adapterView.getId(), adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        presenter.onItemSelected(this, adapterView.getId(), null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.onCheckedChanged(this, buttonView.getId(), isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        presenter.onRadioCheckedChanged(this, group.getId(), checkedId);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        presenter.onDrag(this, v.getId(), event);
        return true;
    }

    TextWatcher newTextWatcher(View view) {
        Log.d(tag, "new text watcher for view: " + view);
        TextWatcher watcher = new MvpTextWatcher<>(handler, this, presenter, view.getId());
        textWatchers.add(watcher);
        return watcher;
    }

    SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        Log.d(tag, "new query text listener for view: " + view);
        SearchView.OnQueryTextListener listener = new MvpOnQueryTextListener<>(handler, this, presenter, view.getId());
        queryTextListeners.add(listener);
        return listener;
    }

    @Override
    public MvpView<S, ?> getMvpView() {
        return reference.get();
    }

    @Override
    public int getLayoutId() {
        return getMvpView().getLayoutId();
    }

    @Override
    public Bundle getArguments() {
        MvpView<S, P> view = reference.get();
        return view == null ? new Bundle() : view.getArguments();
    }

    @Override
    public void post(S state) {
        postEvent(new StateEvent(state));
    }

    @Override
    public void finish() {
        postEvent(new PresenterEvent(view -> view.finish()));
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        postEvent(new PresenterEvent(view -> view.showDialog(dialog)));
    }

    @Override
    public void showSnackBar(String text, int duration) {
        postEvent(new PresenterEvent(view -> Snackbar.make(view.getView(), text, duration).show()));
    }

    @Override
    public void showSnackBar(int res, int duration) {
        postEvent(new PresenterEvent(view -> Snackbar.make(view.getView(), res, duration).show()));
    }

    @Override
    public void showToast(String text, int duration) {
        postEvent(new PresenterEvent(view ->
                Toast.makeText(view.getContext(), text, duration).show()));
    }

    @Override
    public void showToast(int resId, int duration) {
        postEvent(new PresenterEvent(view ->
                Toast.makeText(view.getContext(), resId, duration).show()));
    }

    @Override
    public void startActivity(Intent intent) {
        postEvent(new PresenterEvent(view -> view.getContext().startActivity(intent)));
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        postEvent(new PresenterEvent(view -> {
            if (view instanceof AppCompatActivity) {
                ((AppCompatActivity) view).startActivityForResult(intent, requestCode);
            } else {
                Log.e(tag, "only activity can start activity for result");
            }
        }));
    }

    /**
     * This method handles last saved state runnable. It is called from inside and outside of
     * current class.
     */
    void handleLastState() {
        if (lastStateEvent != null) {
            lastStateEvent.run();
        }
    }

    /**
     * This method post event to be processed on the main thread context
     *
     * @param runnable event to be sent
     */
    synchronized private void postEvent(EventRunnable runnable) {
        events.offer(runnable);
        if (isResumed() && isQueueDraining.compareAndSet(false, true)) {
            handler.post(this::drainEventQueue);
        }
        expungeStaleEntries();
    }

    /**
     * This method polls event from the queue and updates flag
     *
     * @return event or null
     */
    synchronized private EventRunnable pollEvent() {
        isQueueDraining.compareAndSet(true, !events.isEmpty());
        return events.poll();
    }

    /**
     * This method enables or disables queue drain. State queue must not be drained in some cases
     * since view is not ready to handle a state. If menu is not inflated for example.
     *
     * @param enabled true to enable queue drain, false to stop it
     * @return true if state is changed
     */
    boolean setEnabled(boolean enabled) {
        boolean isChanged = isEnabled.compareAndSet(!enabled, enabled);
        if (isChanged && isResumed()) {
            onResumed();
        }
        return isChanged;
    }

    private boolean isResumed() {
        return isEnabled.get() && isResumed.get();
    }

    private void drainEventQueue() {
        int size = events.size();
        int n = size / QUEUE_SIZE;
        // drainEventQueue may be called when View has been paused or it is about to be destroyed
        // so it is better to check this flag before start state processing
        while (isResumed()) {
            EventRunnable event = pollEvent();
            if (event == null) {
                break;
            }
            // process every n'th state in case of queue overflow
            if (n == 0 || size % n == 0) {
                if (event.isState()) {
                    lastStateEvent = event;
                }
                event.run();
                size = events.size();
                n = size / QUEUE_SIZE;
            }
        }
    }

    private void expungeStaleEntries() {
        synchronized (referenceQueue) {
            if (referenceQueue.poll() != null) {
                presenter.disconnect(this);
            }
        }
    }

    private boolean isFirstStateChange() {
        return isFirstStateChange.getAndSet(false);
    }

    private interface EventRunnable extends Runnable {
        default boolean isState() {
            return false;
        }
    }

    private class StateEvent implements EventRunnable {
        final S state;

        StateEvent(S state) {
            this.state = state;
        }

        @Override
        public boolean isState() {
            return true;
        }

        @Override
        public void run() {
            MvpView<S, P> view = reference.get();
            if (view != null) {
                if (isFirstStateChange()) {
                    view.onFirstStateChange(state);
                }
                view.onStateChanged(state);
            }
        }
    }

    private class PresenterEvent implements EventRunnable {
        final Consumer<MvpView<S, ?>> consumer;

        PresenterEvent(Consumer<MvpView<S, ?>> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void run() {
            MvpView<S, P> view = reference.get();
            if (view != null) {
                try {
                    consumer.accept(view);
                } catch (Exception e) {
                    Log.e(tag, "error:", e);
                }
            }
        }
    }
}
