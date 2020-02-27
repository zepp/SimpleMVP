/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.common;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;

/**
 * This interface describes interface to be used by presenter to interact with MvpView
 *
 * @param <S> state type
 */
public interface MvpViewHandle<S extends MvpState> {

    /**
     * Returns the ID of the encapsulated view. See {@link MvpView#getMvpId()} for details
     *
     * @return ID
     */
    int getMvpId();

    /**
     * This method returns layout Id of real view
     *
     * @return layout ID
     */
    @LayoutRes
    int getLayoutId();

    /**
     * Method to access arguments reference
     *
     * @return {@link MvpView} arguments
     */
    Bundle getArguments();

    /**
     * This method posts new state to parent view
     *
     * @param state
     */
    void post(S state);

    /**
     * This method terminates parent view
     */
    void finish();

    /**
     * This method shows {@link android.widget.Toast Toast} using view's {@link android.content.Context Context}
     *
     * @param text     The text to be shown
     * @param duration How long to display the message.  Either {@link android.widget.Toast#LENGTH_SHORT}
     *                 or {@link android.widget.Toast#LENGTH_LONG}
     */
    void showToast(String text, int duration);

    /**
     * This method shows {@link android.widget.Toast Toast} using view's {@link android.content.Context Context}
     *
     * @param resId    string resource to be shown
     * @param duration How long to display the message.  Either {@link android.widget.Toast#LENGTH_SHORT}
     *                 or {@link android.widget.Toast#LENGTH_LONG}
     */
    void showToast(@StringRes int resId, int duration);

    /**
     * This method shows {@link android.support.design.widget.Snackbar} using root view as parent view
     *
     * @param text     text to be shown
     * @param duration How long to display text. Either {@link android.support.design.widget.Snackbar#LENGTH_SHORT}
     *                 or {@link android.support.design.widget.Snackbar#LENGTH_LONG}
     */
    void showSnackBar(String text, int duration);

    /**
     * This method shows {@link android.support.design.widget.Snackbar} using root view as parent view
     *
     * @param res      string ID to be shown
     * @param duration How long to display text. Either {@link android.support.design.widget.Snackbar#LENGTH_SHORT}
     *                 or {@link android.support.design.widget.Snackbar#LENGTH_LONG}
     */
    void showSnackBar(@StringRes int res, int duration);

    /**
     * This method shows {@link android.support.design.widget.Snackbar} using root view as parent view
     *
     * @param text     string ID to be shown
     * @param duration How long to display text. Either {@link android.support.design.widget.Snackbar#LENGTH_SHORT}
     *                 or {@link android.support.design.widget.Snackbar#LENGTH_LONG}
     * @param action   action title to be displayed
     */
    void showSnackBar(String text, int duration, String action);

    /**
     * This method starts new activity. It is a wrapper around {@link android.app.Activity#startActivity(Intent)}
     * method that does real work.
     *
     * @param intent {@link Intent intent} instance
     */
    void startActivity(Intent intent);

    /**
     * Launch an activity for which you would like a result when it finished. This method invokes
     * {@link android.app.Activity#startActivity(Intent, Bundle)} internally.
     *
     * @param intent      The intent to start.
     * @param requestCode If >= 0, this code will be returned in onActivityResult() when the
     *                    activity exits.
     */
    void startActivityForResult(Intent intent, int requestCode);

    /**
     * This method shows dialog using compat fragment manager.
     *  @param dialog {@link DialogFragment dialog} to be shown
     *
     */
    void showDialog(DialogFragment dialog);
}
