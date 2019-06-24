/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.presenter.MvpBasePresenter;
import com.simplemvp.presenter.MvpPresenterManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* Базовый класс для всех Activity, которые реализуют паттерн MVP */
public abstract class MvpActivity<P extends MvpBasePresenter<S>, S extends MvpState> extends AppCompatActivity
        implements MvpView<P, S>, View.OnClickListener {
    protected ExecutorService executor;
    protected MvpStateHandler<S> stateHandler;
    protected MvpPresenterManager manager;
    protected P presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        executor = Executors.newSingleThreadExecutor();
        stateHandler = new MvpStateHandler<>(this);
        getLifecycle().addObserver(stateHandler);
        manager = MvpPresenterManager.getInstance(this);
        presenter = onInitPresenter(manager);
        presenter.attach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.detach(this);
        if (isFinishing()) {
            manager.releasePresenter(presenter);
        }
        getLifecycle().removeObserver(stateHandler);

    }

    @Override
    public void post(S state) {
        stateHandler.post(state);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        executor.execute(() -> presenter.onOptionsItemSelected(item.getItemId()));
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        executor.execute(() -> presenter.onActivityResult(requestCode, resultCode, data));
    }

    @Override
    public void onClick(View v) {
        executor.execute(() -> presenter.onViewClicked(v.getId()));
    }
}
