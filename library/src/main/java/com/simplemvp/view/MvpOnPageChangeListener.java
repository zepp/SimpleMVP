package com.simplemvp.view;

import androidx.viewpager.widget.ViewPager;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpViewHandle;

public class MvpOnPageChangeListener<S extends MvpState> implements ViewPager.OnPageChangeListener, DisposableListener {
    private final MvpViewHandle<S> handle;
    private final MvpPresenter<S> presenter;
    private final ViewPager view;

    public MvpOnPageChangeListener(MvpViewHandle<S> handle, MvpPresenter<S> presenter, ViewPager view) {
        this.handle = handle;
        this.presenter = presenter;
        this.view = view;
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        presenter.onPositionChanged(handle, view.getId(), i);
    }

    @Override
    public void onPageScrollStateChanged(int i) {
    }

    @Override
    public void dispose() {
        view.removeOnPageChangeListener(this);
    }
}
