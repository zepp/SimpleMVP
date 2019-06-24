package com.simplemvp.presenter;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

class PresenterHandler<S extends MvpState> implements InvocationHandler {
    private final ExecutorService executor;
    private final MvpPresenter<S> presenter;

    PresenterHandler(ExecutorService executor, MvpPresenter<S> presenter) {
        this.executor = executor;
        this.presenter = presenter;
    }

    static <S extends MvpState, P extends MvpPresenter<S>> P newProxy(ExecutorService executor, P presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                presenter.getClass().getInterfaces(), new PresenterHandler<>(executor, presenter));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        executor.submit(() -> method.invoke(presenter, args));
        return null;
    }
}
