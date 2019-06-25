/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */
package com.simplemvp.common;

import android.arch.lifecycle.LifecycleObserver;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;

/**
 * This interface describes MVP view implementation
 *
 * @param <S> state type
 * @param <P> presenter type
 */
public interface MvpViewImplementation<S extends MvpState, P extends MvpPresenter<S>>
        extends LifecycleObserver, View.OnClickListener, AdapterView.OnItemSelectedListener {

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
     * This method returns new TextWatcher to handle EditText events
     *
     * @param view view that has TextWatcher listener callback
     * @return new TextWatcher listener
     */
    TextWatcher newTextWatcher(View view);
}
