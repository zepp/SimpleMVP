/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;

import com.simplemvp.presenter.MvpPresenterManager;

/**
 * This interface describes a generic MVP view
 *
 * @param <S> state type
 * @param <P> presenter type
 */
public interface MvpView<S extends MvpState, P extends MvpPresenter<S>> {
    /**
     * This method returns layout Id to be inflated by view
     *
     * @return
     */
    @LayoutRes
    int getLayoutId();

    /**
     * This methods returns menu ID to be inflated by Activity
     *
     * @return
     */
    @MenuRes
    int getMenuId();

    /**
     * This method returns presenter which current view is attached to
     *
     * @return presenter
     */
    P getPresenter();

    /**
     * This method is called when state is changed and view should update its state
     *
     * @param state new state
     */
    void onStateChanged(S state);

    /**
     * This method returns implementation of current MVP view
     *
     * @return view implementation
     */
    MvpViewImplementation<S, P> getViewImpl();

    /**
     * This method is called when view is just created and new presenter instance should be created
     *
     * @param manager
     * @return
     */
    P onInitPresenter(MvpPresenterManager manager);

    /**
     * This method terminates current view
     */
    void finish();
}
