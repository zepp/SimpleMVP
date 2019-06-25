/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.CallSuper;
import android.util.Log;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public abstract class MvpBasePresenter<S extends MvpState> implements LifecycleObserver, MvpPresenter<S> {
    protected final String tag = getClass().getSimpleName();
    protected final MvpPresenterManager manager;
    protected final Context context;
    protected final ExecutorService executor;
    protected final List<MvpView<S, ?>> views = new CopyOnWriteArrayList<>();
    protected final Resources resources;
    protected final S state;

    public MvpBasePresenter(Context context, S state) {
        this.manager = MvpPresenterManager.getInstance(context);
        this.context = context;
        this.executor = manager.getExecutor();
        this.state = state;
        this.resources = context.getResources();
    }

    // добавляет представление в список клиентов текущего представителя
    public final void attach(MvpView<S, ?> view) {
        synchronized (views) {
            views.add(view);
            if (views.size() == 1) {
                executor.execute(this::onStart);
            }
        }
        view.getViewImpl().post(getStateSnapshot());
    }

    // удаляет представление из списка клиентов текущего представления
    public final void detach(MvpView<S, ?> view) {
        synchronized (views) {
            views.remove(view);
            if (views.size() == 0) {
                executor.execute(this::onStop);
            }
        }
    }

    @Override
    public boolean isDetached() {
        return views.isEmpty();
    }

    public S getState() {
        return (S) getStateSnapshot();
    }

    // делает копию состояния и отправляет её на отображение
    // копия делается с целью избежания ситуации, когда состояние изменяется в процессе отображения
    public void commit() {
        if (state.isChanged() || state.isInitial()) {
            S snapshot = getStateSnapshot();
            state.clearChanged();
            for (MvpView<S, ?> view : views) {
                view.getViewImpl().post(snapshot);
            }
        }
    }

    @Override
    public void finish() {
        for (MvpView<S, ?> view : views) {
            view.getViewImpl().finish();
        }
    }

    @CallSuper
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(tag, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data + ")");
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
}
