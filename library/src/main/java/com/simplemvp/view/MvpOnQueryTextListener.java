/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import androidx.appcompat.widget.SearchView;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

/**
 * This class implements {@link SearchView.OnQueryTextListener}
 * interface to handle events from {@link SearchView} view.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed a button and holds it for period of time.
 *
 * @param <S> state type
 */
class MvpOnQueryTextListener<S extends MvpState> implements SearchView.OnQueryTextListener {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    private final SearchView view;

    MvpOnQueryTextListener(MvpViewHandle<S> handle, MvpPresenter<S> presenter, SearchView view) {
        this.handle = handle;
        this.presenter = presenter;
        this.view = view;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        presenter.onTextChanged(handle, view.getId(), s);
        return false;
    }
}
