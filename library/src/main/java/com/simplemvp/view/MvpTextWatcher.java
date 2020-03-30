/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

/**
 * This class implements {@link android.text.TextWatcher TextWatcher} interface to handle events from
 * {@link android.widget.EditText EditText} view and so on.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed backspace to remove all input for example.
 *
 * @param <S> state type
 */
class MvpTextWatcher<S extends MvpState> implements TextWatcher, DisposableListener {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    private final EditText view;

    MvpTextWatcher(MvpViewHandle<S> handle, MvpPresenter<S> presenter, EditText view) {
        this.handle = handle;
        this.presenter = presenter;
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        presenter.onTextChanged(handle, view.getId(), s.toString());
    }

    @Override
    public void dispose() {
        view.removeTextChangedListener(this);
    }
}
