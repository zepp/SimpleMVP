/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.simplemvp.common.Executable;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
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
    private final ViewLifecycleObserver observer;
    private final int id;
    private final Map<Integer, MvpViewHandle<S>> handles;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private final Map<String, AsyncBroadcastReceiver> receivers;
    private final Consumer<Throwable> errorHandler;
    private final Map<Executable, Future<?>> futures;
    private final PresenterComponentCallbacks componentCallbacks;
    private int parentId;
    private ScheduledFuture<?> commit;

    public MvpBasePresenter(Context context, S state) {
        super(context);
        this.manager = MvpPresenterManager.getInstance(context);
        this.observer = new ViewLifecycleObserver();
        this.executor = manager.getExecutor();
        this.scheduler = manager.getScheduledExecutor();
        this.errorHandler = manager.getErrorHandler();
        this.state = state;
        this.id = lastId.incrementAndGet();
        this.handles = new TreeMap<>();
        this.receivers = new TreeMap<>();
        this.commit = scheduler.schedule(() -> null, 0, TimeUnit.MILLISECONDS);
        this.futures = new TreeMap<>((o1, o2) -> o1.hashCode() - o2.hashCode());
        this.componentCallbacks = new PresenterComponentCallbacks();
    }

    void initialize() {
        registerComponentCallbacks(componentCallbacks);
    }

    /**
     * This method is called by a view to attached oneself to presenter that is instantiated by
     * {@link MvpPresenterManager}
     *
     * @param view {@link MvpViewHandle MvpViewHandle} to connect to
     */
    @Override
    public final synchronized void connect(@NonNull MvpView<S, ?> view) {
        MvpViewHandle<S> handle = view.getViewHandle();
        boolean isFirst = handles.isEmpty();
        if (handles.put(view.getMvpId(), handle) == null) {
            Bundle arguments = view.getArguments() == null ? new Bundle() : new Bundle(view.getArguments());
            submit(() -> {
                if (isFirst) {
                    parentId = handle.getMvpId();
                    onFirstViewConnected(handle, arguments);
                }
                onViewConnected(handle, arguments);
                handle.post(cloneState());
            });
        } else {
            handle.post(cloneState());
        }
        view.getLifecycle().addObserver(observer);
    }

    @Override
    public final synchronized void disconnect(@NonNull MvpView<S, ?> view) {
        if (handles.remove(view.getMvpId()) != null && handles.isEmpty()) {
            executor.submit(() -> {
                synchronized (this) {
                    try {
                        onLastViewDisconnected();
                    } catch (Exception e) {
                        errorHandler.accept(e);
                    }
                }
            });
        }
        view.getLifecycle().removeObserver(observer);
    }

    @Override
    public final void disconnectLazy(int id) {
        submit(() -> {
            if (handles.remove(id) != null && handles.isEmpty()) {
                onLastViewDisconnected();
            }
        });
    }

    /**
     * Predicate that indicates that presenter has no attached views
     *
     * @return true if there is no attached views
     */
    @Override
    public final boolean isDisconnected() {
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
    protected final synchronized void commit() {
        commit.cancel(false);
        if (state.isChanged() || state.isInitial()) {
            S snapshot = cloneState();
            for (MvpViewHandle<S> handle : handles.values()) {
                handle.post(snapshot);
            }
            afterCommit();
            state.clearChanged();
        }
    }

    protected final synchronized void commit(long millis) {
        if (millis > 0) {
            commit.cancel(false);
            commit = schedule(this::commit, millis, TimeUnit.MILLISECONDS);
        } else {
            commit();
        }
    }

    /**
     * This method is run when {@link #commit} method is about to finish it's work
     */
    @CallSuper
    protected void afterCommit() {
        state.afterCommit();
    }

    /**
     * This method returns copy of the state.
     *
     * @return {@link S} instance
     */
    protected final synchronized S cloneState() {
        try {
            return (S) state.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Submits task to execution
     *
     * @param executable {@link Executable} task to be invoked
     * @return {@link Future} instance
     */
    public final Future<?> submit(Executable executable) {
        return executor.submit(() -> executeSync(executable, false));
    }

    /**
     * Schedule periodic task at fixed rate
     *
     * @param executable {@link Executable} task to be invoked
     * @param time     time
     * @param unit     time units
     * @return {@link ScheduledFuture} instance
     */
    protected final synchronized ScheduledFuture<?> schedulePeriodic(Executable executable, long time, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> executeSync(executable, false), time, time, unit);
        collectFuture(future, executable);
        return future;
    }

    /**
     * Schedule single shot task
     *
     * @param executable {@link Executable} task to be invoked
     * @param time     time
     * @param unit     time units
     * @return {@link ScheduledFuture} instance
     */
    protected final synchronized ScheduledFuture<?> schedule(Executable executable, long time, TimeUnit unit) {
        ScheduledFuture<?> future = scheduler.schedule(() -> executeSync(executable, true), time, unit);
        collectFuture(future, executable);
        return future;
    }

    /**
     * Executes method in synchronized context with respect to presenter life cycle and handles errors
     * @param executable {@link Executable} task to be invoked
     * @param isRemoveFuture if true then remove future from the collection
     */
    private synchronized void executeSync(Executable executable, boolean isRemoveFuture) {
        try {
            if (!isDisconnected()) {
                executable.execute();
            }
        } catch (Exception e) {
            errorHandler.accept(e);
        } finally {
            if (isRemoveFuture) {
                futures.remove(executable);
            }
        }
    }

    /**
     * This method is used by {@link ProxyHandler} to call presenter methods in synchronized context
     *
     * @param <T>      type of returned argument
     * @param callable {@link Callable} instance
     * @param isThrow throw exception on error
     * @return result of {@link Callable#call()}
     */
    synchronized <T> T callSync(Callable<T> callable, boolean isThrow) {
        try {
            return callable.call();
        } catch (Exception e) {
            errorHandler.accept(e);
            if (isThrow) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Collects provided future to cancel it in case of presenter lifecycle is finished
     *
     * @param future     {@link Future} instance
     * @param executable {@link Executable} instance
     */
    private synchronized void collectFuture(Future<?> future, Executable executable) {
        Future<?> previous = futures.put(executable, future);
        if (previous != null) {
            if (previous.cancel(false)) {
                Log.w(tag, future.toString() + " is cancelled");
            }
        }
    }

    @CallSuper
    @Override
    public void finish() {
        // finish only parent element to keep smooth animation
        getParentHandle().finish();
    }

    @CallSuper
    @Override
    public void onActivityResult(@NonNull MvpViewHandle<S> handle, int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(tag, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(@NonNull MvpViewHandle<S> handle, int requestCode, @NonNull Map<String, Integer> permissions) {
        Log.d(tag, "onRequestPermissionsResult(" + requestCode + ", " + permissions.keySet());
    }

    @CallSuper
    @Override
    public void onViewClicked(@NonNull MvpViewHandle<S> handle, int viewId) {
        Log.d(tag, "onViewClicked(" + getResources().getResourceName(viewId) + ")");
    }

    @CallSuper
    @Override
    public void onItemSelected(@NonNull MvpViewHandle<S> handle, int viewId, @Nullable Object item) {
        Log.d(tag, "onItemSelected(" + getResources().getResourceName(viewId) + ", " + item + ")");
    }

    @CallSuper
    @Override
    public void onPositionChanged(@NonNull MvpViewHandle<S> handle, int viewId, int position) {
        Log.d(tag, "onPositionChanged(" + getResources().getResourceName(viewId) + ", " + position + ")");
    }

    @CallSuper
    @Override
    public void onTextChanged(@NonNull MvpViewHandle<S> handle, int viewId, String text) {
        Log.d(tag, "onTextChanged(" + getResources().getResourceName(viewId) + ", " + text + ")");
    }

    @CallSuper
    @Override
    public void onEditorAction(@NonNull MvpViewHandle<S> handle, int viewId, int actionId) {
        Log.d(tag, "onEditorAction(" + getResources().getResourceName(viewId) + ", " + actionId + ")");
    }

    @Override
    public void onCheckedChanged(@NonNull MvpViewHandle<S> handle, int viewId, boolean isChecked) {
        Log.d(tag, "onCheckedChanged(" + getResources().getResourceName(viewId) + ", " + isChecked + ")");
    }

    @Override
    public void onRadioCheckedChanged(@NonNull MvpViewHandle<S> handle, int radioViewId, int viewId) {
        Log.d(tag, "onRadioCheckedChanged(" + getResources().getResourceName(radioViewId) + ", " + viewId + ")");
    }

    @Override
    public void onDrag(@NonNull MvpViewHandle<S> handle, int viewId, DragEvent event) {
        Log.d(tag, "onDrag(" + getResources().getResourceName(viewId) + ", " + event + ")");
    }

    @Override
    public void onProgressChanged(@NonNull MvpViewHandle<S> handle, int viewId, int progress) {
        Log.d(tag, "onProgressChanged(" + getResources().getResourceName(viewId) + ", " + progress + ")");
    }

    /**
     * This method subscribes current presenter to broadcast intents to be processed in
     * {@link MvpBasePresenter#onBroadcastReceived(Intent, BroadcastReceiver.PendingResult)} method.
     *
     * @param filter {@link IntentFilter} instance
     */
    protected final synchronized void subscribeToBroadcast(IntentFilter filter) {
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
     * This method unregisters current presenter from broadcast intents with specified action.
     *
     * @param action {@link Intent#getAction()}
     */
    protected final synchronized void unsubscribeFromBroadcast(String action) {
        AsyncBroadcastReceiver receiver = receivers.remove(action);
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    /**
     * This method is called when broadcast intent is received.
     * @param intent {@link Intent} instance
     * @param result
     */
    @CallSuper
    protected void onBroadcastReceived(@NonNull Intent intent, BroadcastReceiver.PendingResult result) throws Exception {
        Log.d(tag, "onBroadcastReceived(" + intent + ")");
    }

    /**
     * Called when the operating system has determined that it is a good time for a presenter to
     * trim unneeded memory from its process. See {@link ComponentCallbacks2} for details.
     *
     * @param level The context of the trim, giving a hint of the amount of
     *              trimming the application may like to perform.
     */
    @CallSuper
    protected void onTrimMemory(int level) throws Exception {
        Log.d(tag, "onTrimMemory(" + level + ")");
    }

    /**
     * This method is called when master view is connected to current presenter. This method is to be
     * overridden to place initialization code that fills {@link MvpBasePresenter#state} with initial
     * values and subscribes to necessary events.
     *
     * @param handle {@link MvpViewHandle MvpViewHandle} interface reference
     * @param arguments {@link Bundle} that keeps arguments attached to a view
     */
    @CallSuper
    protected void onFirstViewConnected(@NonNull MvpViewHandle<S> handle, @NonNull Bundle arguments) throws Exception {
        Log.d(tag, "onFirstViewConnected(" + getResources().getResourceName(handle.getLayoutId()) + ")");
    }

    /**
     * This method is called when fresh view is connected to a presenter.
     *
     * @param handle {@link MvpViewHandle MvpViewHandle} interface reference
     * @param arguments {@link Bundle} that keeps arguments attached to a view
     */
    @CallSuper
    protected void onViewConnected(@NonNull MvpViewHandle<S> handle, @NonNull Bundle arguments) throws Exception {
        Log.d(tag, "onViewConnected(" + getResources().getResourceName(handle.getLayoutId()) + ")");
    }

    /**
     * This method is called when there is at least one connected view has been started.
     *
     * @throws Exception
     */
    @CallSuper
    protected void onViewsActive() throws Exception {
        Log.d(tag, "onViewsActive()");
    }

    /**
     * This method is called when all connected views have been stopped.
     *
     * @throws Exception
     */
    @CallSuper
    protected void onViewsInactive() throws Exception {
        Log.d(tag, "onViewsInactive()");
    }

    /**
     * This method is called when presenter has no connected views and it is about to be released
     * by {@link MvpPresenterManager}.
     */
    @CallSuper
    protected void onLastViewDisconnected() throws Exception {
        Log.d(tag, "onLastViewDisconnected()");
        unregisterComponentCallbacks(componentCallbacks);
        for (AsyncBroadcastReceiver receiver : receivers.values()) {
            unregisterReceiver(receiver);
        }
        receivers.clear();
        for (Future<?> future : futures.values()) {
            future.cancel(false);
        }
        futures.clear();
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
                executeSync(() -> onBroadcastReceived(intent, result), false);
                result.finish();
            });
        }
    }

    private class ViewLifecycleObserver implements LifecycleObserver {
        private final AtomicInteger started = new AtomicInteger();

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onStarted() {
            if (started.incrementAndGet() == 1) {
                submit(MvpBasePresenter.this::onViewsActive);
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onStopped() {
            if (started.decrementAndGet() == 0) {
                submit(MvpBasePresenter.this::onViewsInactive);
            }
        }
    }

    private class PresenterComponentCallbacks implements ComponentCallbacks2 {
        @Override
        public void onTrimMemory(int level) {
            submit(() -> MvpBasePresenter.this.onTrimMemory(level));
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
        }

        @Override
        public void onLowMemory() {
        }
    }
}
