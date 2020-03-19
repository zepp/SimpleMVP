/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;


import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * This class keeps presenter instances and instantiate ones by request
 */
public final class MvpPresenterManager extends ContextWrapper {
    private static volatile MvpPresenterManager instance;
    private final String tag = getClass().getSimpleName();
    private final Map<Integer, Composite<?>> map;
    private final ScheduledExecutorService scheduledExecutor;
    private volatile ExecutorService executor;
    private volatile Consumer<Throwable> errorHandler;

    private MvpPresenterManager(Context context) {
        super(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.map = Collections.synchronizedMap(new TreeMap<>());
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.errorHandler = e -> Log.e(tag, "error: ", e);
    }

    public static MvpPresenterManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MvpPresenterManager.class) {
                if (instance == null) {
                    instance = new MvpPresenterManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void initialize(@NonNull ExecutorService executor, @NonNull Consumer<Throwable> handler) {
        this.executor = executor;
        this.errorHandler = handler;
    }

    @NonNull
    ExecutorService getExecutor() {
        return executor;
    }

    @NonNull
    ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    @NonNull
    Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }

    /**
     * This method returns presenter instance of desired type. At the current moment there could be
     * only one instance per type.
     *
     * @param pClass class of presenter
     * @param sClass class of state
     * @return new presenter
     */
    @NonNull
    public <S extends MvpState, P extends MvpBasePresenter<S>, I extends MvpPresenter<S>> I newPresenterInstance(Class<? extends P> pClass, Class<S> sClass) {
        S state = newState(sClass);
        P presenter = newPresenter(pClass, sClass, state);
        I proxy = ProxyHandler.newProxy(presenter);
        map.put(presenter.getId(), new Composite<>(presenter, proxy));
        Log.d(tag, "new presenter: " + presenter);
        return proxy;
    }

    /**
     * This method returns already created presenter by ID
     *
     * @param presenterId presenter Id
     * @param <S>         state type
     * @param <I>         presenter type
     * @return presenter of desired type
     */
    @NonNull
    public <S extends MvpState, I extends MvpPresenter<S>> I getPresenterInstance(int presenterId) {
        Composite<S> composite = (Composite<S>) map.get(presenterId);
        if (composite == null) {
            throw new RuntimeException("presenter instance not found");
        }
        return (I) composite.proxy;
    }

    /**
     * This method releases presenter instance to be garbage collected
     *
     * @param presenter instance to be released if one has no attached views
     */
    public void releasePresenter(@NonNull MvpPresenter<?> presenter) {
        if (presenter.isDisconnected()) {
            if (map.remove(presenter.getId()) != null) {
                Log.d(tag, "release presenter: " + presenter);
            }
        }
    }

    private <S extends MvpState, P extends MvpBasePresenter<S>> P newPresenter(Class<P> pClass, Class<S> sClass, S state) {
        try {
            return pClass.getConstructor(Context.class, sClass).newInstance(getBaseContext(), state);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, "error: ", e);
            throw new RuntimeException(e);
        }
    }

    private <S extends MvpState> S newState(Class<S> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, "error: ", e);
            throw new RuntimeException(e);
        }
    }

    private static class Composite<S extends MvpState> {
        final MvpBasePresenter<S> presenter;
        final MvpPresenter<S> proxy;

        Composite(MvpBasePresenter<S> presenter, MvpPresenter<S> proxy) {
            this.presenter = presenter;
            this.proxy = proxy;
        }
    }
}
