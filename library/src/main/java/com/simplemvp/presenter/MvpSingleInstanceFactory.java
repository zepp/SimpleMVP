/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */
package com.simplemvp.presenter;

import com.simplemvp.common.MvpState;

/**
 * This factory allows only one presenter instance to be constructed
 */
public class MvpSingleInstanceFactory extends MvpFactory {
    private int id;

    @Override
    public synchronized <S extends MvpState, P extends MvpBasePresenter<S>> P newPresenter(Class<P> pClass, Class<S> sClass, S state) {
        if (id == 0 || !manager.isPresenterExist(id)) {
            P presenter = super.newPresenter(pClass, sClass, state);
            id = presenter.getId();
            return presenter;
        } else {
            return manager.getPresenterInstance(id);
        }
    }
}
