/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.os.Handler;
import android.os.Looper;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

class ProxyHandler<S extends MvpState> implements InvocationHandler {
    private final static String tag = ProxyHandler.class.getSimpleName();
    private final static Thread mainThread = Looper.getMainLooper().getThread();
    private final WeakReference<MvpDispatcher<S>> dispatcher;
    private final Map<Method, Proxify> annotations;
    private final MvpPresenter<S> presenter;
    private final Handler handler;
    private final int viewId;

    ProxyHandler(MvpDispatcher<S> dispatcher, MvpPresenter<S> presenter) {
        this.dispatcher = new WeakReference<>(dispatcher);
        this.presenter = presenter;
        annotations = Collections.synchronizedMap(getAnnotatedMethods(dispatcher));
        handler = new Handler(Looper.getMainLooper());
        viewId = dispatcher.getMvpId();
    }

    private static boolean isMainThread(Thread thread) {
        return mainThread.equals(thread);
    }

    private Map<Method, Proxify> getAnnotatedMethods(MvpViewHandle<S> view) {
        Map<Method, Proxify> result = new TreeMap<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
        for (Method method : view.getClass().getMethods()) {
            Proxify annotation = method.getAnnotation(Proxify.class);
            if (annotation != null) {
                result.put(method, annotation);
            }
        }
        return result;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MvpDispatcher<S> dispatcher = this.dispatcher.get();
        Proxify annotation = annotations.get(method);
        if (dispatcher == null || dispatcher.isParentViewDestroyed()) {
            presenter.disconnectLazy(viewId);
            if (annotation.alive()) {
                throw new RuntimeException("view has been already destroyed");
            }
            return null;
        } else {
            if (annotation.looper()) {
                if (isMainThread(Thread.currentThread())) {
                    return handle(dispatcher, method, args);
                } else {
                    handler.post(() -> {
                        try {
                            handle(dispatcher, method, args);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                    return null;
                }
            } else {
                return method.invoke(dispatcher, args);
            }
        }
    }

    private Object handle(MvpDispatcher<S> dispatcher, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (dispatcher.isParentViewReady()) {
            return method.invoke(dispatcher, args);
        } else {
            dispatcher.submitEvent(() -> method.invoke(dispatcher, args));
            return null;
        }
    }
}
