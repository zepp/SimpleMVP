/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

class ProxyHandler<S extends MvpState> implements InvocationHandler {
    private final static String tag = ProxyHandler.class.getSimpleName();
    private final static Thread mainThread = Looper.getMainLooper().getThread();
    private final WeakReference<MvpEventHandler<S>> eventHandler;
    private final Set<Method> annotatedMethods;
    private final MvpPresenter<S> presenter;
    private final Handler handler;
    private final int layoutId;

    ProxyHandler(MvpEventHandler<S> eventHandler, MvpPresenter<S> presenter) {
        this.eventHandler = new WeakReference<>(eventHandler);
        this.presenter = presenter;
        annotatedMethods = Collections.synchronizedSet(getAnnotatedMethods(eventHandler));
        handler = new Handler(Looper.getMainLooper());
        layoutId = eventHandler.getLayoutId();
    }

    private static boolean isMainThread(Thread thread) {
        return mainThread.equals(thread);
    }

    private Set<Method> getAnnotatedMethods(MvpViewHandle<S> view) {
        Set<Method> result = new TreeSet<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
        for (Method method : view.getClass().getMethods()) {
            if (method.getAnnotation(MvpHandler.class) != null) {
                result.add(method);
            }
        }
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MvpEventHandler<S> eventHandler = this.eventHandler.get();
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

    private Object invoke(MvpEventHandler<S> handler, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (handler.isResumed()) {
            return method.invoke(handler, args);
        } else {
            handler.postEvent(() -> method.invoke(handler, args));
            return null;
        }
    }
}
