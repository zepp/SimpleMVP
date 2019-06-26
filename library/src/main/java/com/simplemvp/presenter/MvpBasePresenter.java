/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.simplemvp.annotations.Handling;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is a base class for any MVP presenter. It has basic implementation of interface methods that
 * prints debug information. Also it keeps a list of attached views (implementations)
 *
 * @param <S> state type
 */
public abstract class MvpBasePresenter<S extends MvpState> implements MvpPresenter<S> {
    private final static AtomicInteger lastId = new AtomicInteger();
    protected final String tag = getClass().getSimpleName();
    protected final MvpPresenterManager manager;
    protected final Context context;
    protected final Resources resources;
    protected final S state;
    protected final int id;
    private final List<MvpViewImplementation<S, ?>> implementations = new CopyOnWriteArrayList<>();
    private final ExecutorService executor;

    public MvpBasePresenter(Context context, S state) {
        this.manager = MvpPresenterManager.getInstance(context);
        this.context = context;
        this.executor = manager.getExecutor();
        this.state = state;
        this.resources = context.getResources();
        this.id = lastId.incrementAndGet();
    }

    /**
     * This method attaches view to the presenter
     *
     * @param view to be attached
     */
    @Handling(offload = false)
    public final void attach(MvpView<S, ?> view) {
        synchronized (implementations) {
            implementations.add(view.getViewImpl());
            if (implementations.size() == 1) {
                executor.execute(this::onStart);
            }
        }
        view.getViewImpl().post(getStateSnapshot());
    }

    /**
     * This method detaches view from the presenter
     *
     * @param view to be detached
     */
    @Handling(offload = false)
    public final void detach(MvpView<S, ?> view) {
        synchronized (implementations) {
            implementations.remove(view.getViewImpl());
            if (implementations.size() == 0) {
                executor.execute(this::onStop);
            }
        }
    }

    /**
     * Predicate that indicates that presenter has no attached views
     *
     * @return
     */
    @Override
    @Handling(offload = false)
    public final boolean isDetached() {
        return implementations.isEmpty();
    }

    @Override
    @Handling(offload = false)
    public int getId() {
        return id;
    }

    /**
     * This method sends current state to attached views to render changes
     */
    public synchronized void commit() {
        if (state.isChanged() || state.isInitial()) {
            S snapshot = getStateSnapshot();
            state.clearChanged();
            for (MvpViewImplementation<S, ?> impl : implementations) {
                impl.post(snapshot);
            }
        }
    }

    @Override
    public void finish() {
        for (MvpViewImplementation<S, ?> impl : implementations) {
            impl.finish();
        }
    }

    @CallSuper
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(tag, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(tag, "onRequestPermissionsResult(" + requestCode + ", " + Arrays.toString(permissions) + ", "
                + Arrays.toString(grantResults));
    }

    @CallSuper
    @Override
    public void onViewClicked(int viewId) {
        Log.d(tag, "onViewClicked(" + resources.getResourceName(viewId) + ")");
    }

    @CallSuper
    @Override
    public void onOptionsItemSelected(int itemId) {
        Log.d(tag, "onOptionsItemSelected(" + resources.getResourceName(itemId) + ")");
    }

    @CallSuper
    @Override
    public void onItemSelected(int viewId, Object item) {
        Log.d(tag, "onItemSelected(" + resources.getResourceName(viewId) + ", " + item + ")");
    }

    @CallSuper
    @Override
    public void onTextChanged(int viewId, String text) {
        Log.d(tag, "onTextChanged(" + resources.getResourceName(viewId) + ", " + text + ")");
    }

    @Override
    public void onCheckedChanged(int viewId, boolean isChecked) {
        Log.d(tag, "onCheckedChanged(" + resources.getResourceName(viewId) + ", " + isChecked + ")");
    }

    @CallSuper
    protected void onStart() {
        Log.d(tag, "onStart");
    }

    @CallSuper
    protected void onStop() {
        Log.d(tag, "onStop");
    }

    protected S getStateSnapshot() {
        try {
            return (S) state.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    @Handling(offload = false)
    public String toString() {
        return "MvpBasePresenter{" +
                "id=" + id +
                ", state=" + state +
                '}';
    }
}
