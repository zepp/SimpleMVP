/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.view.View;

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
     * This method returns handle of current MVP view
     *
     * @return view implementation
     */
    MvpViewHandle<S> getViewHandle();

    /**
     * This methods returns listener that combines a lot of View listeners to handle events
     *
     * @return
     */
    MvpListener getMvpListener();

    /**
     * This method is called during view initialization (typically in {@link android.app.Activity#onCreate(Bundle) onCreate}
     * or in {@link android.support.v4.app.Fragment#onCreate(Bundle) onCreate}) to created a new presenter
     * instance
     *
     * @param manager {@link MvpPresenterManager MvpPresenterManager} instance
     * @return new presenter instance
     */
    P onInitPresenter(MvpPresenterManager manager);

    /**
     * This method terminates current view
     */
    void finish();

    /**
     * This method returns new {@link android.text.TextWatcher TextWatcher} for provided view to
     * handle text change.
     *
     * @param view view that supports {@link android.text.TextWatcher TextWatcher} callback
     * @return new listener instance
     */
    TextWatcher newTextWatcher(View view);

    /**
     * This method return new {@link android.support.v7.widget.SearchView.OnQueryTextListener OnQueryTextListener}
     * to handle text change.
     *
     * @param view {@link android.support.v7.widget.SearchView SearchView} instance
     * @return new listener instance
     */
    SearchView.OnQueryTextListener newQueryTextListener(SearchView view);
}
