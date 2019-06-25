/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;
import com.simplemvp.presenter.MvpPresenterManager;

public abstract class MvpDialogFragment<P extends MvpPresenter<S>, S extends MvpState> extends DialogFragment implements MvpView<S, P> {
    protected MvpViewImpl<S, P> viewImpl;
    protected MvpPresenterManager manager;
    protected P presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        manager = MvpPresenterManager.getInstance(getContext());
        presenter = onInitPresenter(manager);
        viewImpl = new MvpViewImpl<>(this, presenter);
        getLifecycle().addObserver(viewImpl);
        presenter.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
        manager.releasePresenter(presenter);
        getLifecycle().removeObserver(viewImpl);
    }

    @Override
    public MvpViewImplementation<S, P> getViewImpl() {
        return viewImpl;
    }

    @Override
    public void finish() {
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}
