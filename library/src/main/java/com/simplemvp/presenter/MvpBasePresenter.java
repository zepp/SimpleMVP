/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.CallSuper;
import android.support.v4.util.Consumer;
import android.util.Log;
import android.view.DragEvent;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a base class for any MVP presenter. It has basic implementation of interface methods that
 * prints debug information. Also it keeps a list of attached views (handles)
 *
 * @param <S> state type
 */
public abstract class MvpBasePresenter<S extends MvpState> extends ContextWrapper implements MvpPresenter<S> {
    private final static AtomicInteger lastId = new AtomicInteger();
    protected final String tag = getClass().getSimpleName();
    protected final S state;
    private final MvpPresenterManager manager;
    private final int id;
    private final Map<Integer, MvpViewHandle<S>> handles;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final Map<String, AsyncBroadcastReceiver> receivers;
    private final Consumer<Throwable> errorHandler;
    private int parentId;
    private ScheduledFuture<?> commit;

    public MvpBasePresenter(Context context, S state) {
        super(context);
        this.manager = MvpPresenterManager.getInstance(context);
        this.executor = manager.getExecutor();
        this.scheduler = manager.getScheduledExecutor();
        this.errorHandler = manager.getErrorHandler();
        this.state = state;
        this.id = lastId.incrementAndGet();
        this.handles = Collections.synchronizedMap(new TreeMap<>());
        this.receivers = new TreeMap<>();
        this.commit = scheduler.schedule(() -> null, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * This method is called by a view to attached oneself to presenter that is instantiated by
     * {@link MvpPresenterManager}
     *
     * @param handle {@link MvpViewHandle MvpViewHandle} to connect to
     */
    public final synchronized void connect(MvpViewHandle<S> handle) {
        boolean isFirst = handles.isEmpty();
        if (handles.put(handle.getLayoutId(), handle) == null) {
            executor.execute(() -> {
                try {
                    synchronized (this) {
                        if (isFirst) {
                            parentId = handle.getLayoutId();
                            onFirstViewConnected(handle);
                        }
                        onViewConnected(handle);
                        // post state after onViewConnected finished (state is initialized)
                        handle.post(cloneState());
                    }
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            });
        } else {
            handle.post(cloneState());
        }
    }

    public final void disconnect(MvpViewHandle<S> handle) {
        disconnectById(handle.getLayoutId());
    }

    @Override
    public final void disconnect(int layoutId) {
        disconnectById(layoutId);
    }

    /**
     * This method is called by a view to disconnect oneself from presenter when view is about to be
     * destroyed. This method call is mandatory otherwise presenter will not be stopped and acquired
     * resources will not be released.
     *
     * @param layoutId layout ID of the connected view
     */
    private synchronized void disconnectById(int layoutId) {
        if (handles.remove(layoutId) != null) {
            boolean isLast = handles.isEmpty();
            executor.execute(() -> {
                try {
                    synchronized (this) {
                        if (isLast) {
                            onLastViewDisconnected();
                        }
                    }
                } catch (Exception e) {
                    errorHandler.accept(e);
                }
            });
        }
    }

    /**
     * Predicate that indicates that presenter has no attached views
     *
     * @return true if there is no attached views
     */
    @Override
    public final boolean isDetached() {
        return handles.isEmpty();
    }

    @Override
    public int getId() {
        return id;
    }

    /**
     * This method returns parents view handle
     *
     * @return {@link MvpViewHandle} instance
     */
    protected MvpViewHandle<S> getParentHandle() {
        return handles.get(parentId);
    }

    /**
     * This method returns executor that handles method calls of this presenter.
     *
     * @return {@link ExecutorService} instance
     */
    protected ExecutorService getExecutor() {
        return executor;
    }

    /**
     * This method sends current state to attached views to render changes.
     * Method is synchronized to keep state order otherwise state with less revision number may
     * be delivered to {@link MvpView#onStateChanged(MvpState)} before the another state snapshot with
     * bigger revision number.
     */
    protected synchronized void commit() {
        commit.cancel(false);
        if (state.isChanged() || state.isInitial()) {
            S snapshot = cloneState();
            state.clearChanged();
            synchronized (handles) {
                for (MvpViewHandle<S> handle : handles.values()) {
                    handle.post(snapshot);
                }
            }
        }
    }

    protected synchronized void commit(long millis) {
        if (millis > 0) {
            commit.cancel(false);
            commit = scheduler.schedule(() -> commit(), millis, TimeUnit.MILLISECONDS);
        } else {
            commit();
        }
    }

    /**
     * This method returns copy of the state.
     *
     * @return {@link S} instance
     */
    protected synchronized S cloneState() {
        try {
            return (S) state.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finish() {
        synchronized (handles) {
            for (MvpViewHandle<S> handle : handles.values()) {
                handle.finish();
            }
        }
    }

    @CallSuper
    @Override
    public void onActivityResult(MvpViewHandle<S> handle, int requestCode, int resultCode, Intent data) {
        Log.d(tag, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(MvpViewHandle<S> handle, int requestCode, Map<String, Integer> permissions) {
        Log.d(tag, "onRequestPermissionsResult(" + requestCode + ", " + permissions.keySet());
    }

    @CallSuper
    @Override
    public void onViewClicked(MvpViewHandle<S> handle, int viewId) {
        Log.d(tag, "onViewClicked(" + getResources().getResourceName(viewId) + ")");
    }

    @CallSuper
    @Override
    public void onOptionsItemSelected(MvpViewHandle<S> handle, int itemId) {
        Log.d(tag, "onOptionsItemSelected(" + getResources().getResourceName(itemId) + ")");
    }

    @CallSuper
    @Override
    public void onItemSelected(MvpViewHandle<S> handle, int viewId, Object item) {
        Log.d(tag, "onItemSelected(" + getResources().getResourceName(viewId) + ", " + item + ")");
    }

    @CallSuper
    @Override
    public void onTextChanged(MvpViewHandle<S> handle, int viewId, String text) {
        Log.d(tag, "onTextChanged(" + getResources().getResourceName(viewId) + ", " + text + ")");
    }

    @Override
    public void onCheckedChanged(MvpViewHandle<S> handle, int viewId, boolean isChecked) {
        Log.d(tag, "onCheckedChanged(" + getResources().getResourceName(viewId) + ", " + isChecked + ")");
    }

    @Override
    public void onRadioCheckedChanged(MvpViewHandle<S> handle, int radioViewId, int viewId) {
        Log.d(tag, "onRadioCheckedChanged(" + getResources().getResourceName(radioViewId) + ", " + viewId + ")");
    }

    @Override
    public void onDrag(MvpViewHandle<S> handle, int viewId, DragEvent event) {
        Log.d(tag, "onDrag(" + getResources().getResourceName(viewId) + ", " + event + ")");
    }

    @Override
    public void onProgressChanged(MvpViewHandle<S> handle, int viewId, int progress) {
        Log.d(tag, "onProgressChanged(" + getResources().getResourceName(viewId) + ", " + progress + ")");
    }

    /**
     * This method subscribes current presenter to broadcast intents to be processed in
     * {@link MvpBasePresenter#onBroadcastReceived(Intent)} method.
     *
     * @param filter {@link IntentFilter} instance
     */
    protected final void subscribeToBroadcast(IntentFilter filter) {
        AsyncBroadcastReceiver receiver = new AsyncBroadcastReceiver();
        registerReceiver(receiver, filter);
        for (int i = 0; i < filter.countActions(); i++) {
            AsyncBroadcastReceiver previous = receivers.put(filter.getAction(i), receiver);
            if (previous != null) {
                unregisterReceiver(previous);
            }
        }
    }

    /**
     * This method is called when broadcast intent is received.
     *
     * @param intent {@link Intent} instance
     */
    @CallSuper
    protected void onBroadcastReceived(Intent intent) {
        Log.d(tag, "onBroadcastReceived(" + intent + ")");
    }

    /**
     * This method is called when master view is connected to current presenter. This method is to be
     * overridden to place initialization code that fills {@link MvpBasePresenter#state} with initial
     * values and subscribes to necessary events.
     *
     * @param handle {@link MvpViewHandle MvpViewHandle} interface reference
     */
    @CallSuper
    protected void onFirstViewConnected(MvpViewHandle<S> handle) throws Exception {
        Log.d(tag, "onFirstViewConnected(" + handle.getMvpView() + ")");
    }

    /**
     * This method is called when fresh view is connected to a presenter.
     *
     * @param handle {@link MvpViewHandle MvpViewHandle} interface reference
     */
    @CallSuper
    protected void onViewConnected(MvpViewHandle<S> handle) throws Exception {
        Log.d(tag, "onViewConnected(" + handle.getMvpView() + ")");
    }

    /**
     * This method is called when presenter has no connected views and it is about to be released
     * by {@link MvpPresenterManager}.
     */
    @CallSuper
    protected void onLastViewDisconnected() throws Exception {
        Log.d(tag, "onLastViewDisconnected()");
        for (AsyncBroadcastReceiver receiver : receivers.values()) {
            unregisterReceiver(receiver);
        }
        receivers.clear();
        commit.cancel(false);
        manager.releasePresenter(this);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {" +
                "id=" + id +
                ", state=" + state +
                '}';
    }

    private class AsyncBroadcastReceiver extends BroadcastReceiver {
        AsyncBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            PendingResult result = goAsync();
            executor.submit(() -> {
                try {
                    synchronized (this) {
                        if (!isDetached()) {
                            onBroadcastReceived(intent);
                        }
                    }
                } catch (Exception e) {
                    errorHandler.accept(e);
                } finally {
                    result.finish();
                }
            });
        }
    }
}
