/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.view.View;

import com.simplemvp.presenter.MvpPresenterManager;

/**
 * This interface specifies a generic MVP view
 *
 * @param <S> state type
 * @param <P> presenter type
 */
public interface MvpView<S extends MvpState, P extends MvpPresenter<S>> {
    /**
     * This method returns layout Id to be inflated by a view
     *
     * @return layout ID
     */
    @LayoutRes
    int getLayoutId();

    /**
     * This methods returns menu ID to be inflated by Activity
     *
     * @return menu ID
     */
    @MenuRes
    int getMenuId();

    /**
     * This method returns view's {@link Context}
     *
     * @return {@link Context}
     */
    Context getContext();

    /**
     * This method return view's arguments that were supplied in constructor or factory method.
     *
     * @return {@link Bundle Bundle} reference
     */
    Bundle getArguments();

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
     * This methods returns universal listener that combines a lot of {@link View View} listeners to
     * handle various events
     *
     * @return
     */
    MvpListener getMvpListener();

    /**
     * This method returns root {@link View view} of the current {@link MvpView}
     *
     * @return
     */
    View getView();

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
     * Method to be called when {@link MvpView MvpView} is no longer needed and should be closed
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

    /**
     * This method shows a dialog using view {@link android.support.v4.app.FragmentManager} and {@link Context}
     *
     * @param dialog dialog fragment to be shown
     */
    void showDialog(DialogFragment dialog);
}
