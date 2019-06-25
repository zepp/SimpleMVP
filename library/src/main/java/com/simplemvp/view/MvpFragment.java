/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;
import com.simplemvp.presenter.MvpBasePresenter;
import com.simplemvp.presenter.MvpPresenterManager;

/* Базовый класс для всех фрагментов, которые реализуют паттерн MVP */
public abstract class MvpFragment<P extends MvpBasePresenter<S>, S extends MvpState> extends Fragment
        implements MvpView<S, P> {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.onOptionsItemSelected(item.getItemId());
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        presenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
