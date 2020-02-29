/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.common;

import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.simplemvp.presenter.MvpPresenterManager;

/**
 * This interface specifies a generic MVP view
 *
 * @param <S> state type
 * @param <P> presenter type
 */
public interface MvpView<S extends MvpState, P extends MvpPresenter<S>> extends LifecycleOwner {

    /**
     * This method returns unique ID of the current view. It is totally unrelated to view ID
     * specified in layout file.
     *
     * @return
     */
    int getMvpId();

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
     * This method returns root {@link View view} of the current {@link MvpView}
     *
     * @return
     */
    View getView();

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
     * This method is called during view initialization (typically in {@link android.app.Activity#onCreate(Bundle) onCreate}
     * or in {@link android.support.v4.app.Fragment#onCreate(Bundle) onCreate}) to created a new presenter
     * instance
     *
     * @param manager {@link MvpPresenterManager MvpPresenterManager} instance
     * @return new presenter instance
     */
    P onInitPresenter(MvpPresenterManager manager);

    /**
     * This method is called when state is changed and view should update its state
     *
     * @param state new state
     */
    void onStateChanged(S state);

    /**
     * This method is called once after activity/fragment startup
     *
     * @param state current presenter state
     */
    void onFirstStateChange(S state);

    /**
     * This method returns handle of current MVP view
     *
     * @return view implementation
     */
    MvpViewHandle<S> getViewHandle();

    /**
     * Method to be called when {@link MvpView MvpView} is no longer needed and should be closed
     */
    void finish();

    /**
     * This method shows a dialog using view {@link android.support.v4.app.FragmentManager} and {@link Context}
     *
     * @param dialog dialog fragment to be shown
     */
    void showDialog(DialogFragment dialog);

    /**
     * This methods returns universal listener that combines a lot of {@link View View} listeners to
     * handle various events
     *
     * @return
     */
    MvpListener getMvpListener();

    /**
     * This method returns new {@link android.text.TextWatcher TextWatcher} for provided view to
     * handle text change.
     *
     * @param view {@link EditText} instance
     * @return new listener instance
     */
    TextWatcher newTextWatcher(EditText view);

    /**
     * This method returns new {@link android.support.v7.widget.SearchView.OnQueryTextListener OnQueryTextListener}
     * to handle text change.
     *
     * @param view {@link android.support.v7.widget.SearchView SearchView} instance
     * @return new listener instance
     */
    SearchView.OnQueryTextListener newQueryTextListener(SearchView view);

    /**
     * This method new {@link android.support.v4.view.ViewPager.OnPageChangeListener OnPageChangeListener}
     * to handle page change of {@link ViewPager}
     *
     * @param view {@link ViewPager} instance
     * @return {@link android.support.v4.view.ViewPager.OnPageChangeListener OnPageChangeListener} instance
     */
    ViewPager.OnPageChangeListener newOnPageChangeListener(ViewPager view);

    /**
     * This method returns listener that implements {@link View.OnClickListener} to handle view clicks
     *
     * @param isAutoLocking disable view instance after click
     * @return {@link View.OnClickListener} instance
     */
    View.OnClickListener getMvpClickListener(boolean isAutoLocking);
}
