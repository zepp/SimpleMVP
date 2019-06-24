/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.presenter.MvpBasePresenter;
import com.simplemvp.presenter.MvpPresenterManager;

/* Базовый класс для всех фрагментов, которые реализуют паттерн MVP */
public abstract class MvpFragment<P extends MvpBasePresenter<S>, S extends MvpState> extends Fragment
        implements MvpView<P, S>, View.OnClickListener, AdapterView.OnItemSelectedListener {
    protected MvpStateHandler<S> stateHandler;
    protected MvpPresenterManager manager;
    protected P presenter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stateHandler = new MvpStateHandler<>(this);
        getLifecycle().addObserver(stateHandler);
        manager = MvpPresenterManager.getInstance(getContext());
        presenter = onInitPresenter(manager);
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
        getLifecycle().removeObserver(stateHandler);
    }

    @Override
    public void post(S state) {
        stateHandler.post(state);
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
    public void onClick(View v) {
        presenter.onViewClicked(v.getId());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        presenter.onItemSelected(adapterView.getId(), adapterView.getItemAtPosition(i));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
