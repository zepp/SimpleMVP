/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

class MvpEventHandler<S extends MvpState> extends ContextWrapper
        implements MvpViewHandle<S>, MvpListener, LifecycleObserver {
    private final static String tag = MvpEventHandler.class.getSimpleName();
    private final MvpView<S, ?> view;
    private final MvpPresenter<S> presenter;
    private final Queue<Callable<?>> events = new LinkedList<>();
    private final List<MvpTextWatcher<S>> textWatchers = new ArrayList<>();
    private final List<MvpOnQueryTextListener<S>> queryTextListeners = new ArrayList<>();
    private final List<MvpOnPageChangeListener<S>> pageChangeListeners = new ArrayList<>();
    private final AtomicBoolean isFirstStateChange = new AtomicBoolean(true);
    private final AtomicBoolean isEnabled = new AtomicBoolean();
    private final AtomicBoolean isResumed = new AtomicBoolean();
    private MvpViewHandle<S> proxy;
    private S state;

    MvpEventHandler(MvpView<S, ?> view, MvpPresenter<S> presenter) {
        super(view.getContext());
        this.view = view;
        this.presenter = presenter;
    }

    void initialize() {
        view.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed.set(true);
        onEnabledResumed();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPaused() {
        isResumed.set(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStopped() {
        isFirstStateChange.set(true);
        for (MvpTextWatcher<S> watcher : textWatchers) {
            watcher.unregister();
        }
        textWatchers.clear();
        for (MvpOnPageChangeListener<S> listener : pageChangeListeners) {
            listener.unregister();
        }
        pageChangeListeners.clear();
        queryTextListeners.clear();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroyed() {
        view.getLifecycle().removeObserver(this);
    }

    /**
     * This method enables or disables event processing. Event is {@link MvpViewHandle} method call.
     *
     * @param enabled true to enable event processing
     * @return true if state is changed
     */
    boolean setEnabled(boolean enabled) {
        boolean isChanged = isEnabled.compareAndSet(!enabled, enabled);
        if (isChanged) {
            onEnabledResumed();
        }
        return isChanged;
    }

    /**
     * This method is called when view is resumed or event processing is enabled
     */
    private void onEnabledResumed() {
        if (isResumed()) {
            if (events.isEmpty()) {
                postLastState();
            } else {
                drainEvents();
            }
        }
    }

    boolean isResumed() {
        return isEnabled.get() && isResumed.get();
    }

    void postLastState() {
        if (state != null) {
            post(state);
        }
    }

    private void drainEvents() {
        try {
            while (!events.isEmpty()) {
                events.remove().call();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    void postEvent(Callable<?> event) {
        events.add(event);
    }

    private boolean isFirstStateChange() {
        return isFirstStateChange.getAndSet(false);
    }

    MvpViewHandle<S> getProxy() {
        if (proxy == null) {
            proxy = newProxy();
        }
        return proxy;
    }

    private MvpViewHandle<S> newProxy() {
        return (MvpViewHandle<S>) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{MvpViewHandle.class}, new ProxyHandler(this, presenter));
    }

    @Override
    public void onClick(View v) {
        presenter.onViewClicked(getProxy(), v.getId());
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        presenter.onViewClicked(getProxy(), item.getItemId());
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        presenter.onItemSelected(getProxy(), adapterView.getId(), adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        presenter.onItemSelected(getProxy(), adapterView.getId(), null);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        presenter.onCheckedChanged(getProxy(), buttonView.getId(), isChecked);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        presenter.onRadioCheckedChanged(getProxy(), group.getId(), checkedId);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        presenter.onDrag(getProxy(), v.getId(), event);
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        presenter.onProgressChanged(getProxy(), seekBar.getId(), progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Integer> perms = new TreeMap<>();
        for (int i = 0; i < permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
        }
        presenter.onRequestPermissionsResult(getProxy(), requestCode, perms);
    }

    TextWatcher newTextWatcher(EditText view) {
        Log.d(tag, "new text watcher for view: " + view);
        MvpTextWatcher<S> watcher = new MvpTextWatcher<>(getProxy(), presenter, view);
        textWatchers.add(watcher);
        return watcher;
    }

    SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        Log.d(tag, "new query text listener for view: " + view);
        MvpOnQueryTextListener<S> listener = new MvpOnQueryTextListener<>(getProxy(), presenter, view);
        queryTextListeners.add(listener);
        return listener;
    }

    ViewPager.OnPageChangeListener newOnPageChangeListener(ViewPager view) {
        Log.d(tag, "new page change lster for view: " + view);
        MvpOnPageChangeListener<S> listener = new MvpOnPageChangeListener<>(getProxy(), presenter, view);
        pageChangeListeners.add(listener);
        return listener;
    }

    @Override
    public int getLayoutId() {
        return view.getLayoutId();
    }

    @Override
    public Bundle getArguments() {
        return view.getArguments() == null ? new Bundle() : new Bundle(view.getArguments());
    }

    @Override
    @MvpHandler
    public void post(S state) {
        if (isFirstStateChange()) {
            view.onFirstStateChange(state);
        }
        view.onStateChanged(state);
        this.state = state;
    }

    @Override
    @MvpHandler
    public void finish() {
        view.finish();
    }

    @Override
    @MvpHandler
    public void showDialog(DialogFragment dialog) {
        view.showDialog(dialog);
    }

    @Override
    @MvpHandler
    public void showSnackBar(String text, int duration) {
        Snackbar.make(view.getView(), text, duration).show();
    }

    @Override
    @MvpHandler
    public void showSnackBar(int res, int duration) {
        Snackbar.make(view.getView(), res, duration).show();
    }

    @Override
    @MvpHandler
    public void showSnackBar(String text, int duration, String action) {
        Snackbar bar = Snackbar.make(view.getView(), text, duration);
        bar.setAction(action, v -> {
            bar.dismiss();
            onClick(v);
        });
        bar.show();
    }

    @Override
    @MvpHandler
    public void showToast(String text, int duration) {
        Toast.makeText(view.getContext(), text, duration).show();
    }

    @Override
    @MvpHandler
    public void showToast(int resId, int duration) {
        Toast.makeText(view.getContext(), resId, duration).show();
    }

    @Override
    @MvpHandler
    public void startActivity(Intent intent) {
        view.getContext().startActivity(intent);
    }

    @Override
    @MvpHandler
    public void startActivityForResult(Intent intent, int requestCode) {
        if (view instanceof AppCompatActivity) {
            ((AppCompatActivity) view).startActivityForResult(intent, requestCode);
        } else {
            throw new RuntimeException("only activity can start activity for result");
        }
    }
}
