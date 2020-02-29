package com.simplemvp.view;

import android.view.View;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

class MvpClickListener<S extends MvpState> implements View.OnClickListener {
    private final boolean isAutoLocking;
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;

    MvpClickListener(MvpViewHandle<S> handle, MvpPresenter<S> presenter, boolean isAutoLocking) {
        this.handle = handle;
        this.presenter = presenter;
        this.isAutoLocking = isAutoLocking;
    }

    @Override
    public void onClick(View v) {
        v.setEnabled(!isAutoLocking);
        presenter.onViewClicked(handle, v.getId());
    }
}
