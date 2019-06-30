/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;


import android.content.Context;
import android.util.Log;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
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
    private final Map<Integer, MvpPresenter<?>> map;
    private final ReferenceQueue<MvpView<?, ?>> referenceQueue;
    private volatile ExecutorService executor;
    private volatile MvpErrorHandler handler;

    private MvpPresenterManager(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.map = Collections.synchronizedMap(new HashMap<>());
        this.referenceQueue = new ReferenceQueue<>();
        this.handler = e -> Log.e(tag, formStackTrace(e));
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

    private static String formStackTrace(Throwable e) {
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

    public void initialize(ExecutorService executor, MvpErrorHandler handler) {
        this.executor = executor;
        this.handler = handler;
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
        expungeStaleEntries();
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
        expungeStaleEntries();
        return (P) map.get(presenterId);
    }

    /**
     * This method releases presenter instance to be garbage collected
     *
     * @param presenter instance to be released if one has no attached views
     */
    public void releasePresenter(MvpPresenter<?> presenter) {
        if (presenter.isDetached()) {
            Log.d(tag, "release presenter: " + presenter);
            map.remove(presenter.getId());
        }
    }

    public ReferenceQueue<MvpView<?, ?>> getReferenceQueue() {
        return referenceQueue;
    }

    ExecutorService getExecutor() {
        return executor;
    }

    private void expungeStaleEntries() {
        synchronized (referenceQueue) {
            Reference<? extends MvpView> reference = referenceQueue.poll();
            while (reference != null) {
                MvpView view = reference.get();
                MvpPresenter presenter = view.getPresenter();
                presenter.detach(view);
                releasePresenter(presenter);
                reference.clear();
                reference = referenceQueue.poll();
            }
        }
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
