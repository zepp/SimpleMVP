/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.support.annotation.LayoutRes;

import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.presenter.MvpState;

// интерфейс описывающий представление
public interface MvpView<P extends MvpPresenter<S>, S extends MvpState> {
    @LayoutRes
    int getLayoutId();

    // вызывается при изменение состояния
    void onStateChanged(S state);

    // вызывается презентером, когда есть новое состояние
    void post(S state);

    // вызывается при необходимости создать представителя
    P onInitPresenter(MvpPresenterManager manager);

    // завершает работу представления
    void finish();
}
