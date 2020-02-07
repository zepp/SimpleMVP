/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;


import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.util.Consumer;
import android.util.Log;

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
    private final Map<Integer, MvpPresenter<?>> map;
    private final ScheduledExecutorService scheduledExecutor;
    private volatile ExecutorService executor;
    private volatile Consumer<Throwable> handler;

    private MvpPresenterManager(Context context) {
        super(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.map = Collections.synchronizedMap(new TreeMap<>());
        this.scheduledExecutor = Executors.newScheduledThreadPool(1);
        this.handler = e -> Log.e(tag, "error: ", e);
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

    public void initialize(ExecutorService executor, Consumer<Throwable> handler) {
        this.executor = executor;
        this.handler = handler;
    }

    ExecutorService getExecutor() {
        return executor;
    }

    ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutor;
    }

    /**
     * This method returns presenter instance of desired type. At the current moment there could be
     * only one instance per type.
     *
     * @param pClass class of presenter
     * @param sClass class of state
     * @return new presenter
     */
    public <S extends MvpState, I extends MvpPresenter<S>> I newPresenterInstance(Class<? extends I> pClass, Class<S> sClass) {
        S state = newState(sClass);
        I presenter = ProxyHandler.newProxy(executor, handler, newPresenter(pClass, sClass, state));
        map.put(presenter.getId(), presenter);
        Log.d(tag, "new presenter: " + presenter);
        return presenter;
    }

    /**
     * This method returns already created presenter by ID
     *
     * @param presenterId presenter Id
     * @param <S>         state type
     * @param <P>         presenter type
     * @return presenter of desired type
     */
    public <S extends MvpState, P extends MvpPresenter<S>> P getPresenterInstance(int presenterId) {
        return (P) map.get(presenterId);
    }

    /**
     * This method releases presenter instance to be garbage collected
     *
     * @param presenter instance to be released if one has no attached views
     */
    public void releasePresenter(MvpPresenter<?> presenter) {
        if (presenter.isDetached()) {
            if (map.remove(presenter.getId()) != null) {
                Log.d(tag, "release presenter: " + presenter);
            }
        }
    }

    private <P extends MvpPresenter<S>, S extends MvpState> P newPresenter(Class<P> pClass, Class<S> sClass, S state) {
        try {
            return pClass.getConstructor(Context.class, sClass).newInstance(getBaseContext(), state);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }

    private <S extends MvpState> S newState(Class<S> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
    }
}
