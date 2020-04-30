/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Context;

import androidx.annotation.NonNull;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.presenter.MvpPresenterManager;

public abstract class MvpHostedFragment<P extends MvpPresenter<S>, S extends MvpState> extends MvpFragment<P, S> {
    private MvpView<S, P> view;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MvpView) {
            view = (MvpView<S, P>) context;
        }
    }

    @NonNull
    @Override
    public P onInitPresenter(@NonNull MvpPresenterManager manager) {
        if (view == null) {
            throw new RuntimeException("parent view doesn't implement MvpView interfaces");
        } else {
            return view.getPresenter();
        }
    }
}
