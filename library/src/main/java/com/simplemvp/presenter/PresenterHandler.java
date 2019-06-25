package com.simplemvp.presenter;

import android.util.Log;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;

class PresenterHandler<S extends MvpState> implements InvocationHandler {
    private final String tag;
    private final ExecutorService executor;
    private final MvpPresenter<S> presenter;

    private PresenterHandler(ExecutorService executor, MvpPresenter<S> presenter) {
        this.tag = presenter.getClass().getSimpleName();
        this.executor = executor;
        this.presenter = presenter;
    }

    static <S extends MvpState, P extends MvpPresenter<S>> P newProxy(ExecutorService executor, P presenter) {
        return (P) Proxy.newProxyInstance(presenter.getClass().getClassLoader(),
                presenter.getClass().getInterfaces(), new PresenterHandler<>(executor, presenter));
    }

    private static String formStackTrace(Exception e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e.toString());
        builder.append('\n');
        for (StackTraceElement element : e.getStackTrace()) {
            builder.append('\t');
            builder.append(element.toString());
            builder.append('\n');
        }
        return builder.toString();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        executor.execute(() -> {
            try {
                method.invoke(presenter, args);
            } catch (Exception e) {
                Log.e(tag, "error: " + formStackTrace(e));
            }
        });
        return null;
    }
}
