package com.simplemvp.view;

import com.google.android.material.tabs.TabLayout;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

class MvpTabLayoutListener<S extends MvpState> implements TabLayout.OnTabSelectedListener, DisposableListener {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    private final TabLayout view;

    MvpTabLayoutListener(MvpViewHandle<S> handle, MvpPresenter<S> presenter, TabLayout view) {
        this.handle = handle;
        this.presenter = presenter;
        this.view = view;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        presenter.onItemSelected(handle, view.getId(), tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void dispose() {
        view.removeOnTabSelectedListener(this);
    }
}
