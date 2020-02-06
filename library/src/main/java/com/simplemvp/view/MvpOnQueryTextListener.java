package com.simplemvp.view;

import android.support.annotation.IdRes;
import android.support.v7.widget.SearchView;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

/**
 * This class implements {@link android.support.v7.widget.SearchView.OnQueryTextListener OnQueryTextListener}
 * interface to handle events from {@link android.support.v7.widget.SearchView SearchView} view.
 * It invokes {@link MvpPresenter#onTextChanged onTextChanged} method after a short delay. Delay
 * should prevent frequent method invocation if user pressed a button and holds it for period of time.
 *
 * @param <S> state type
 */
class MvpOnQueryTextListener<S extends MvpState> implements SearchView.OnQueryTextListener {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    @IdRes
    private final int viewId;

    MvpOnQueryTextListener(MvpViewHandle<S> handle, MvpPresenter<S> presenter, int viewId) {
        this.handle = handle;
        this.presenter = presenter;
        this.viewId = viewId;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        presenter.onTextChanged(handle, viewId, s);
        return false;
    }
}
