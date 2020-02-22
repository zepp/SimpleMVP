/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.presenter;

import android.util.Log;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

class ProxyHandler<S extends MvpState> implements InvocationHandler {
    private final MvpBasePresenter<S> presenter;
    private final Map<String, MvpHandler> handlers;

    private ProxyHandler(MvpBasePresenter<S> presenter) {
        this.presenter = presenter;
        this.handlers = getMethodAnnotations(presenter);
    }

    private static <S extends MvpState> Map<String, MvpHandler> getMethodAnnotations(MvpPresenter<S> presenter) {
        String tag = presenter.getClass().getSimpleName();
        Map<String, MvpHandler> result = new TreeMap<>();
        for (Method method : presenter.getClass().getMethods()) {
            MvpHandler handler = method.getAnnotation(MvpHandler.class);
            if (handler != null) {
                if (handler.executor()) {
                    if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
                        result.put(method.getName(), handler);
                    } else {
                        Log.w(tag, "@MvpHandler method " + method.getName() + " is ignored since return value is incorrect");
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

    static <S extends MvpState, P extends MvpPresenter<S>> P newProxy(MvpBasePresenter<S> presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                getAllImplementedInterfaces(presenter.getClass()), new ProxyHandler<>(presenter));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MvpHandler handler = handlers.get(method.getName());
        if (handler == null) {
            return invoke(method, args);
        } else {
            if (handler.executor()) {
                presenter.submit(() -> method.invoke(presenter, args));
            } else {
                return invoke(method, args);
            }
        }
        return null;
    }

    private Object invoke(Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        synchronized (presenter) {
            return method.invoke(presenter, args);
        }
    }
}
