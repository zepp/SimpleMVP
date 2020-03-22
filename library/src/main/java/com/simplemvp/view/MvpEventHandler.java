/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is encapsulated by parent {@link MvpView} implementation. {@link MvpView} and {@link MvpPresenter}
 * implementations delegate event processing to this class. It is represented as a {@link MvpListener}
 * for a {@link MvpView} or as a {@link MvpViewHandle} for a {@link MvpPresenter}.
 * There is an internal event queue that collects events that comes from a {@link MvpPresenter}
 * implementation since a {@link MvpView} implementation has a lifecycle and can not handle state
 * changes anytime. Since this class implements {@link MvpListener} interface it implicitly invokes
 * {@link MvpPresenter} handlers.
 *
 * @param <S> state type
 */
class MvpEventHandler<S extends MvpState> extends ContextWrapper
        implements MvpViewHandle<S>, MvpListener, LifecycleObserver {
    private final static String INSTANCE_ID = "mvp-event-handler-id";
    private final static String tag = MvpEventHandler.class.getSimpleName();
    private final static AtomicInteger lastId = new AtomicInteger();
    private final int id;
    private final MvpView<S, ?> view;
    private final MvpPresenter<S> presenter;
    private final Queue<Callable<?>> events = new LinkedList<>();
    private final List<MvpTextWatcher<S>> textWatchers = new ArrayList<>();
    private final List<MvpOnQueryTextListener<S>> queryTextListeners = new ArrayList<>();
    private final List<MvpOnPageChangeListener<S>> pageChangeListeners = new ArrayList<>();
    private boolean isFirstStateChange = true;
    private boolean isEnabled;
    private boolean isResumed;
    private MvpViewHandle<S> proxy;
    private S state;

    MvpEventHandler(@NonNull MvpView<S, ?> view, @Nullable Bundle savedState) {
        super(view.getContext());
        this.id = getId(savedState);
        this.view = view;
        this.presenter = view.getPresenter();
    }

    private static int getId(Bundle bundle) {
        return bundle == null ? lastId.incrementAndGet() : bundle.getInt(INSTANCE_ID);
    }

    void saveId(@NonNull Bundle bundle) {
        bundle.putInt(INSTANCE_ID, id);
    }

    int getId() {
        return id;
    }

    void initialize() {
        view.getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed = true;
        onEnabledOrResumed();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPaused() {
        isResumed = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStopped() {
        isFirstStateChange = true;
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

    /**
     * This method enables or disables event processing. Typically {@link MvpView} implementation
     * disables {@link MvpEventHandler} until menu is inflated.
     *
     * @param enabled true to enable event processing
     * @return true if state is changed
     */
    boolean setEnabled(boolean enabled) {
        boolean isChanged = isEnabled != enabled;
        if (isChanged) {
            isEnabled = enabled;
            onEnabledOrResumed();
        }
        return isChanged;
    }

    /**
     * This method is called when parent {@link MvpView} has been resumed or event processing is
     * enabled. It drains event queue or posts last saved state in case of parent {@link MvpView}
     * is ready.
     */
    private void onEnabledOrResumed() {
        if (isParentViewReady()) {
            if (events.isEmpty()) {
                postLastState();
            } else {
                drainEvents();
            }
        }
    }

    /**
     * This predicate checks if parent {@link MvpView} implementation is ready to handle state changes.
     * {@link MvpView} becomes ready to handle state changes when it has been resumed and menu is
     * inflated. Typically parent {@link MvpView} enables event processing after menu is prepared.
     *
     * @return true in case of view is ready, false otherwise
     */
    boolean isParentViewReady() {
        return isEnabled && isResumed;
    }

    /**
     * Typically this method is called by parent {@link MvpView} implementation internally
     * to update itself. It is suitable in cases when menu has been invalidated and so on.
     */
    void postLastState() {
        if (state != null) {
            post(state);
        }
    }

    /**
     * This method drains event queue when parent view becomes ready to handle state changes.
     */
    private void drainEvents() {
        try {
            while (!events.isEmpty()) {
                events.remove().call();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is invoked by proxy to submit event for execution
     *
     * @param event {@link Callable} instance
     */
    void submitEvent(Callable<?> event) {
        events.add(event);
    }

    private boolean isFirstStateChange() {
        boolean result = isFirstStateChange;
        isFirstStateChange = false;
        return result;
    }

    @NonNull
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

    @NonNull
    TextWatcher newTextWatcher(@NonNull EditText view) {
        Log.d(tag, "new text watcher for view: " + view);
        MvpTextWatcher<S> watcher = new MvpTextWatcher<>(getProxy(), presenter, view);
        textWatchers.add(watcher);
        return watcher;
    }

    @NonNull
    SearchView.OnQueryTextListener newQueryTextListener(@NonNull SearchView view) {
        Log.d(tag, "new query text listener for view: " + view);
        MvpOnQueryTextListener<S> listener = new MvpOnQueryTextListener<>(getProxy(), presenter, view);
        queryTextListeners.add(listener);
        return listener;
    }

    @NonNull
    ViewPager.OnPageChangeListener newOnPageChangeListener(@NonNull ViewPager view) {
        Log.d(tag, "new page change lster for view: " + view);
        MvpOnPageChangeListener<S> listener = new MvpOnPageChangeListener<>(getProxy(), presenter, view);
        pageChangeListeners.add(listener);
        return listener;
    }

    @Override
    public int getMvpId() {
        return view.getMvpId();
    }

    @Override
    public int getLayoutId() {
        return view.getLayoutId();
    }

    @Override
    @MvpHandler
    public void post(@NonNull S state) {
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
    public void showDialog(@NonNull DialogFragment dialog) {
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
        Toast.makeText(getBaseContext(), text, duration).show();
    }

    @Override
    @MvpHandler
    public void showToast(int resId, int duration) {
        Toast.makeText(getBaseContext(), resId, duration).show();
    }

    // This method must be overridden to be annotated with @MvpHandler
    @Override
    @MvpHandler
    public void startActivity(@NonNull Intent intent) {
        super.startActivity(intent);
    }

    @Override
    @MvpHandler
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {
        view.startActivityForResult(intent, requestCode);
    }
}
