/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewImplementation;
import com.simplemvp.presenter.MvpPresenterManager;

/* Базовый класс для всех Activity, которые реализуют паттерн MVP */
public abstract class MvpActivity<P extends MvpPresenter<S>, S extends MvpState> extends AppCompatActivity
        implements MvpView<S, P> {
    protected MvpViewImpl<S, P> viewImpl;
    protected MvpPresenterManager manager;
    protected P presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        manager = MvpPresenterManager.getInstance(this);
        presenter = onInitPresenter(manager);
        viewImpl = new MvpViewImpl<>(this, presenter, manager.getReferenceQueue());
        getLifecycle().addObserver(viewImpl);
        presenter.attach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
        if (isFinishing()) {
            manager.releasePresenter(presenter);
        }
        getLifecycle().removeObserver(viewImpl);
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuId() != 0) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(getMenuId(), menu);
            return true;
        }
        return false;
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
