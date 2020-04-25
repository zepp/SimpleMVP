/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */
package com.simplemvp.common;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import java.util.Map;

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
     * This method posts new state to parent view
     *
     * @param state
     */
    void post(@NonNull S state);

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
     * This method shows {@link com.google.android.material.snackbar.Snackbar} using root view as parent view
     *
     * @param text     text to be shown
     * @param duration How long to display text. Either {@link com.google.android.material.snackbar.Snackbar#LENGTH_SHORT}
     *                 or {@link com.google.android.material.snackbar.Snackbar#LENGTH_LONG}
     */
    void showSnackBar(String text, int duration);

    /**
     * This method shows {@link com.google.android.material.snackbar.Snackbar} using root view as parent view
     *
     * @param res      string ID to be shown
     * @param duration How long to display text. Either {@link com.google.android.material.snackbar.Snackbar#LENGTH_SHORT}
     *                 or {@link com.google.android.material.snackbar.Snackbar#LENGTH_LONG}
     */
    void showSnackBar(@StringRes int res, int duration);

    /**
     * This method shows {@link com.google.android.material.snackbar.Snackbar} using root view as parent view
     *
     * @param text     string ID to be shown
     * @param duration How long to display text. Either {@link com.google.android.material.snackbar.Snackbar#LENGTH_SHORT}
     *                 or {@link com.google.android.material.snackbar.Snackbar#LENGTH_LONG}
     * @param action   action title to be displayed
     */
    void showSnackBar(String text, int duration, String action);

    /**
     * This method starts new activity. It is a wrapper around {@link android.app.Activity#startActivity(Intent)}
     * method that does real work.
     *
     * @param intent {@link Intent intent} instance
     */
    void startActivity(@NonNull Intent intent);

    /**
     * Launch an activity for which you would like a result when it finished. This method invokes
     * {@link android.app.Activity#startActivity(Intent, Bundle)} internally.
     *
     * @param intent      The intent to start.
     * @param requestCode If >= 0, this code will be returned in onActivityResult() when the
     *                    activity exits.
     */
    void startActivityForResult(@NonNull Intent intent, int requestCode);

    /**
     * This method shows dialog using compat fragment manager.
     *  @param dialog {@link DialogFragment} to be shown
     *
     */
    void showDialog(@NonNull DialogFragment dialog);

    /**
     * This method requests permissions on behalf of view
     *
     * @param permissions The requested permissions. Must me non-null and not empty.
     * @param requestCode Application specific request code to match with a result reported to
     *                    {@link com.simplemvp.presenter.MvpBasePresenter#onRequestPermissionsResult(MvpViewHandle, int, Map)}.
     */
    void requestPermissions(@NonNull String[] permissions, int requestCode);

    /**
     * Request to hide the soft input window from the context of the window that is currently
     * accepting input. See
     * {@link android.view.inputmethod.InputMethodManager#hideSoftInputFromWindow(IBinder, int)}
     * for details.
     *
     * @param viewId view's ID that requests to hide input method
     * @param flags  flags
     */
    void hideInputMethod(@IdRes int viewId, int flags);
}
