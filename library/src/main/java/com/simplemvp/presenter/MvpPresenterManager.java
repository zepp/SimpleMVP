/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.presenter;


import android.content.Context;
import android.util.Log;

import com.simplemvp.common.MvpPresenter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/* данный класс управляет представителями и позволяет для любого представления получить
 * соответствующего представителя */
public final class MvpPresenterManager {
    private static volatile MvpPresenterManager manager;
    private final String tag = getClass().getSimpleName();
    private final Context context;
    private final Map<Class<? extends MvpPresenter>, MvpPresenter> map;

    private MvpPresenterManager(Context context) {
        this.context = context;
        this.map = new HashMap<>();
    }

    public static MvpPresenterManager getInstance(Context context) {
        if (manager == null) {
            synchronized (MvpPresenterManager.class) {
                if (manager == null) {
                    manager = new MvpPresenterManager(context.getApplicationContext());
                }
            }
        }
        return manager;
    }

    // создаёт или возвращает ссылку на представителя
    public <P extends MvpPresenter<S>, S extends MvpState> P gewPresenterInstance(Class<P> pClass,
                                                                                  Class<S> sClass) {
        synchronized (map) {
            P presenter = (P) map.get(pClass);
            if (presenter == null) {
                S state = newState(sClass);
                presenter = newPresenter(pClass, sClass, state);
                map.put(pClass, presenter);
                Log.d(tag, "new presenter: " + presenter);
            }
            return presenter;
        }
    }

    // удаляет ссылку на представителя, делая его таким образом доступным для GC
    public <P extends MvpPresenter<S>, S extends MvpState> void releasePresenter(P presenter) {
        if (presenter.isDetached()) {
            Log.d(tag, "release presenter: " + presenter);
            synchronized (map) {
                map.remove(presenter.getClass());
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
