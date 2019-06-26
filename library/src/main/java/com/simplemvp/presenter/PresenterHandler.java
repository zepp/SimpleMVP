/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.presenter;

import com.simplemvp.annotations.Handling;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

class PresenterHandler<S extends MvpState> implements InvocationHandler {
    private final ExecutorService executor;
    private final MvpErrorHandler handler;
    private final MvpPresenter<S> presenter;

    private PresenterHandler(ExecutorService executor, MvpErrorHandler handler, MvpPresenter<S> presenter) {
        this.executor = executor;
        this.handler = handler;
        this.presenter = presenter;
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
            ExecutorService executor, MvpErrorHandler handler, P presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                getAllImplementedInterfaces(presenter.getClass()),
                new PresenterHandler<>(executor, handler, presenter));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Handling handling = method.getAnnotation(Handling.class);
        if (handling == null) {
            offload(method, args);
        } else {
            if (handling.offload()) {
                offload(method, args);
            } else {
                return method.invoke(presenter, args);
            }
        }
        return null;
    }

    private void offload(Method method, Object[] args) {
        executor.execute(() -> {
            try {
                method.invoke(presenter, args);
            } catch (Exception e) {
                Throwable cause = e.getCause();
                handler.onError(cause == null ? e : cause);
            }
        });
    }
}
