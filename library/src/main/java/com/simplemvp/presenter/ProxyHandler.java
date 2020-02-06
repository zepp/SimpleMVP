/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.presenter;

import android.support.v4.util.Consumer;
import android.util.Log;

import com.simplemvp.annotations.MvpEventHandler;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

class ProxyHandler<S extends MvpState> implements InvocationHandler {
    private final ExecutorService executor;
    private final Consumer<Throwable> handler;
    private final MvpPresenter<S> presenter;
    private final Map<String, MvpEventHandler> handlers;

    private ProxyHandler(ExecutorService executor, Consumer<Throwable> handler, MvpPresenter<S> presenter) {
        this.executor = executor;
        this.handler = handler;
        this.presenter = presenter;
        this.handlers = getPresenterAnnotations(presenter);
    }

    private static <S extends MvpState> Map<String, MvpEventHandler> getPresenterAnnotations(MvpPresenter<S> presenter) {
        String tag = presenter.getClass().getSimpleName();
        Map<String, MvpEventHandler> result = new TreeMap<>();
        for (Method method : presenter.getClass().getMethods()) {
            MvpEventHandler handler = method.getAnnotation(MvpEventHandler.class);
            if (handler != null) {
                if (handler.executor()) {
                    if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
                        result.put(method.getName(), handler);
                    } else {
                        Log.w(tag, "@MvpEventHandler method " + method.getName() + " is ignored since return value is incorrect");
                    }
                } else {
                    result.put(method.getName(), handler);
                }
            }
        }
        return result;
    }

    private static Class<?>[] concat(Class<?>[] first, Class<?>[] second) {
        Class<?>[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    private static Class<?>[] getAllImplementedInterfaces(Class<?> clazz) {
        Class<?>[] result = clazz.getInterfaces();
        if (clazz.getSuperclass().equals(Object.class)) {
            return result;
        } else {
            return concat(result, getAllImplementedInterfaces(clazz.getSuperclass()));
        }
    }

    static <S extends MvpState, P extends MvpPresenter<S>> P newProxy(
            ExecutorService executor, Consumer<Throwable> handler, P presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                getAllImplementedInterfaces(presenter.getClass()),
                new ProxyHandler<>(executor, handler, presenter));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MvpEventHandler handler = handlers.get(method.getName());
        if (handler == null) {
            return invoke(true, method, args);
        } else {
            if (handler.executor()) {
                execute(handler.sync(), method, args);
            } else {
                return invoke(handler.sync(), method, args);
            }
        }
        return null;
    }

    private Object invoke(boolean sync, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (sync) {
            synchronized (presenter) {
                return method.invoke(presenter, args);
            }
        } else {
            return method.invoke(presenter, args);
        }
    }

    private void execute(boolean sync, Method method, Object[] args) {
        executor.execute(() -> {
            try {
                invoke(sync, method, args);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                handler.accept(cause == null ? e : cause);
            }
        });
    }
}
