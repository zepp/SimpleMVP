/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.common;

import android.os.Bundle;
import android.support.annotation.StringRes;

/**
 * This interface describes interface to be used by presenter to interact with MvpView
 *
 * @param <S> state type
 */
public interface MvpViewHandle<S extends MvpState> {

    /**
     * Method to access {@link MvpView} reference.
     *
     * @return null or reference to real view
     */
    MvpView<S, ?> getMvpView();

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
}
