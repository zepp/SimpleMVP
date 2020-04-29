/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.presenter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.simplemvp.annotations.MvpHandler;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

class ProxyHandler<S extends MvpState> implements InvocationHandler {
    private final MvpBasePresenter<S> presenter;
    private final Map<Method, MvpHandler> handlers;

    private ProxyHandler(MvpBasePresenter<S> presenter) {
        this.presenter = presenter;
        this.handlers = getMethodAnnotations(presenter);
    }

    private static <S extends MvpState> Map<Method, MvpHandler> getMethodAnnotations(MvpPresenter<S> presenter) {
        String tag = presenter.getClass().getSimpleName();
        Map<Method, MvpHandler> result = new TreeMap<>((o1, o2) -> o1.getName().compareTo(o2.getName()));
        for (Method method : presenter.getClass().getMethods()) {
            MvpHandler handler = method.getAnnotation(MvpHandler.class);
            if (handler != null) {
                if (handler.executor()) {
                    if (method.getReturnType().equals(void.class) || method.getReturnType().equals(Void.class)) {
                        result.put(method, handler);
                    } else {
                        Log.w(tag, "@MvpHandler method " + method.getName() + " is ignored since return value is incorrect");
                    }
                } else {
                    result.put(method, handler);
                }
            }
        }
        return result;
    }

    private static Set<Class<?>> getAllImplementedInterfaces_(Set<Class<?>> set, Class<?> clazz) {
        Collections.addAll(set, clazz.getInterfaces());
        if (!clazz.getSuperclass().equals(Object.class)) {
            getAllImplementedInterfaces_(set, clazz.getSuperclass());
        }
        return set;
    }

    private static Class<?>[] getAllImplementedInterfaces(Class<?> clazz) {
        return getAllImplementedInterfaces_(new TreeSet<>((o1, o2) ->
                o1.getName().compareTo(o2.getName())), clazz).toArray(new Class[0]);
    }

    @NonNull
    static <S extends MvpState, P extends MvpPresenter<S>> P newProxy(MvpBasePresenter<S> presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                getAllImplementedInterfaces(presenter.getClass()), new ProxyHandler<>(presenter));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MvpHandler handler = handlers.get(method);
        if (handler == null) {
            // exception has to be rethrown otherwise return value unboxing error happens
            return presenter.callSync(() -> method.invoke(presenter, args), true);
        } else {
            if (handler.executor()) {
                presenter.submit(() -> method.invoke(presenter, args));
            } else {
                return presenter.callSync(() -> method.invoke(presenter, args), false);
            }
            return null;
        }
    }
}
