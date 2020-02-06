/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.ContextWrapper;
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

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

class MvpEventHandler<S extends MvpState, P extends MvpPresenter<S>> extends ContextWrapper
        implements MvpViewHandle<S>, MvpListener, LifecycleObserver {
    private final static Thread mainThread = Looper.getMainLooper().getThread();
    private final static String tag = MvpEventHandler.class.getSimpleName();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MvpView<S, P> view;
    private final P presenter;
    private final Queue<Callable<?>> events = new LinkedList<>();
    private final List<TextWatcher> textWatchers = new ArrayList<>();
    private final List<SearchView.OnQueryTextListener> queryTextListeners = new ArrayList<>();
    private final AtomicBoolean isFirstStateChange = new AtomicBoolean(true);
    private final AtomicBoolean isEnabled = new AtomicBoolean();
    private final AtomicBoolean isResumed = new AtomicBoolean();
    private S state;

    MvpEventHandler(MvpView<S, P> view, P presenter) {
        super(view.getContext());
        this.view = view;
        this.presenter = presenter;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResumed() {
        isResumed.set(true);
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
    public MvpView<S, P> getMvpView() {
        return view;
    }

    @Override
    public int getLayoutId() {
        return view.getLayoutId();
    }

    @Override
    public Bundle getArguments() {
        return view.getArguments() == null ? new Bundle() : view.getArguments();
    }

    @Override
    public void post(S state) {
        if (isFirstStateChange()) {
            view.onFirstStateChange(state);
        }
        view.onStateChanged(state);
    }

    @Override
    public void finish() {
        view.finish();
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        view.showDialog(dialog);
    }

    @Override
    public void showSnackBar(String text, int duration) {
        Snackbar.make(view.getView(), text, duration).show();
    }

    @Override
    public void showSnackBar(int res, int duration) {
        Snackbar.make(view.getView(), res, duration).show();
    }

    @Override
    public void showToast(String text, int duration) {
        Toast.makeText(view.getContext(), text, duration).show();
    }

    @Override
    public void showToast(int resId, int duration) {
        Toast.makeText(view.getContext(), resId, duration).show();
    }

    @Override
    public void startActivity(Intent intent) {
        view.getContext().startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (view instanceof AppCompatActivity) {
            ((AppCompatActivity) view).startActivityForResult(intent, requestCode);
        } else {
            Log.e(tag, "only activity can start activity for result");
        }
    }

    /**
     * This method handles last saved state runnable. It is called from inside and outside of
     * current class.
     */
    void handleLastState() {
        if (state != null) {
            post(state);
        }
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
        if (isChanged) {
        }
        return isChanged;
    }

    private boolean isResumed() {
        return isEnabled.get() && isResumed.get();
    }

    private boolean isFirstStateChange() {
        return isFirstStateChange.getAndSet(false);
    }

    private static boolean isMainThread(Thread thread) {
        return mainThread.equals(thread);
    }

    private void postEvent(Callable<?> event) {
        events.add(event);
    }

    private void drainEvents() {
        try {
            while (!events.isEmpty()) {
                events.remove().call();
            }
        } catch (Exception e) {
            events.clear();
            Log.e(tag, "error: ", e);
        }
    }

    private MvpViewHandle<S> newProxy() {
        return (MvpViewHandle<S>) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{MvpViewHandle.class}, new MvpProxyHandler(this, presenter));
    }

    private static class MvpProxyHandler<S extends MvpState, P extends MvpPresenter<S>> implements InvocationHandler {
        private final WeakReference<MvpEventHandler<S, P>> eventHandler;
        private final Set<Method> annotatedMethods;
        private final MvpPresenter<S> presenter;
        private final Handler handler;
        private final int layoutId;

        MvpProxyHandler(MvpEventHandler<S, P> eventHandler, MvpPresenter<S> presenter) {
            this.eventHandler = new WeakReference<>(eventHandler);
            this.presenter = presenter;
            annotatedMethods = Collections.synchronizedSet(getAnnotatedMethods(eventHandler));
            handler = new Handler(Looper.getMainLooper());
            layoutId = eventHandler.getLayoutId();
        }

        private Set<Method> getAnnotatedMethods(MvpViewHandle<S> view) {
            Set<Method> result = new TreeSet<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
            for (Method method : view.getClass().getMethods()) {
                if (method.getAnnotation(com.simplemvp.annotations.MvpEventHandler.class) != null) {
                    result.add(method);
                }
            }
            return result;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            MvpEventHandler<S, P> eventHandler = this.eventHandler.get();
            if (eventHandler == null) {
                presenter.disconnect(layoutId);
                return null;
            } else {
                if (annotatedMethods.contains(method)) {
                    if (isMainThread(Thread.currentThread())) {
                        return invoke(eventHandler, method, args);
                    } else {
                        handler.post(() -> {
                            try {
                                invoke(eventHandler, method, args);
                            } catch (Exception e) {
                                Log.e(tag, "error: ", e);
                            }
                        });
                        return null;
                    }
                } else {
                    return method.invoke(eventHandler, args);
                }
            }
        }

        private Object invoke(MvpEventHandler<S, P> handler, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
            if (handler.isResumed()) {
                return method.invoke(handler, args);
            } else {
                handler.postEvent(() -> method.invoke(handler, args));
                return null;
            }
        }
    }
}
