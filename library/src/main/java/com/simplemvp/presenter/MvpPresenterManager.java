/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;


import android.content.Context;
import android.util.Log;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class keeps presenter instances and instantiate ones by request
 */
public final class MvpPresenterManager {
    private static volatile MvpPresenterManager instance;
    private final String tag = getClass().getSimpleName();
    private final Context context;
    private final ExecutorService executor;
    private final Map<Class<? extends MvpPresenter<?>>, MvpPresenter<?>> map;

    private MvpPresenterManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.map = new HashMap<>();
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

    /**
     * This method returns presenter instance of desired type. At the current moment there could be
     * only one instance per type.
     *
     * @param pClass class of presenter
     * @param sClass class of state
     * @return new presenter
     */
    public <P extends MvpPresenter<S>, S extends MvpState> P getPresenterInstance(Class<P> pClass,
                                                                                  Class<S> sClass) {
        synchronized (map) {
            P presenter = (P) map.get(pClass);
            if (presenter == null) {
                S state = newState(sClass);
                presenter = PresenterHandler.newProxy(executor, newPresenter(pClass, sClass, state));
                map.put(pClass, presenter);
                Log.d(tag, "new presenter: " + presenter);
            }
            return presenter;
        }
    }

    /**
     * This method releases presenter instance to be garbage collected
     *
     * @param presenter instance to be released if one has no attached views
     */
    public void releasePresenter(MvpPresenter<?> presenter) {
        if (presenter.isDetached()) {
            Log.d(tag, "release presenter: " + presenter);
            synchronized (map) {
                map.remove(presenter.getClass());
            }
        }
    }

    ExecutorService getExecutor() {
        return executor;
    }

    private <P extends MvpPresenter<S>, S extends MvpState> P newPresenter(Class<P> pClass, Class<S> sClass, S state) {
        try {
            return pClass.getConstructor(Context.class, sClass).newInstance(context, state);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, e.getLocalizedMessage());
            return null;
        }
    }

    private <S extends MvpState> S newState(Class<S> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, e.getLocalizedMessage());
            return null;
        }
    }
}
