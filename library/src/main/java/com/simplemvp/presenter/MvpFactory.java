/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.presenter;

import android.content.Context;
import android.util.Log;

import com.simplemvp.common.MvpState;

import java.lang.reflect.InvocationTargetException;

/**
 * Default factory that construct presenter and state using reflection.
 */
public class MvpFactory {
    protected final String tag = getClass().getSimpleName();
    protected volatile MvpPresenterManager manager;
    private volatile Context context;

    public MvpFactory() {
    }

    void inject(MvpPresenterManager manager) {
        this.manager = manager;
        this.context = manager.getBaseContext();
    }

    protected String getTag() {
        return tag;
    }

    public <S extends MvpState, P extends MvpBasePresenter<S>> P newPresenter(Class<P> pClass, Class<S> sClass, S state) {
        try {
            return pClass.getConstructor(Context.class, sClass).newInstance(context, state);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, "error: ", e);
            throw new RuntimeException(e);
        }
    }

    public <S extends MvpState> S newState(Class<S> clazz) {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.e(tag, "error: ", e);
            throw new RuntimeException(e);
        }
    }
}
