/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;
import com.simplemvp.presenter.MvpPresenterManager;

/* Базовый класс для всех фрагментов, которые реализуют паттерн MVP */
public abstract class MvpFragment<P extends MvpPresenter<S>, S extends MvpState> extends Fragment
        implements MvpView<S, P> {
    private final static String PRESENTER_ID = "presenter-id";
    protected final String tag = getClass().getSimpleName();
    protected MvpViewImpl<S, P> viewImpl;
    protected P presenter;
    private MvpPresenterManager manager;

    protected Bundle initArguments(int presenterId) {
        Bundle args = new Bundle();
        args.putInt(PRESENTER_ID, presenterId);
        setArguments(args);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int presenterId = savedInstanceState == null ? 0 : savedInstanceState.getInt(PRESENTER_ID);
        manager = MvpPresenterManager.getInstance(getContext());
        if (presenterId == 0) {
            presenter = onInitPresenter(manager);
        } else {
            presenter = manager.getPresenterInstance(presenterId);
        }
        viewImpl = new MvpViewImpl<>(this, presenter, manager.getReferenceQueue());
        getLifecycle().addObserver(viewImpl);
        presenter.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // do not call super.onSaveInstanceState to avoid saving views state
        outState.clear();
        outState.putInt(PRESENTER_ID, presenter.getId());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
        manager.releasePresenter(presenter);
        getLifecycle().removeObserver(viewImpl);
    }

    @CallSuper
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getMenuId() != 0) {
            inflater.inflate(getMenuId(), menu);
        }
    }

    @Override
    public MvpViewImplementation<S, P> getViewImpl() {
        return viewImpl;
    }

    @Override
    public P getPresenter() {
        return presenter;
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

    @Override
    public int getMenuId() {
        return 0;
    }
}
