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
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpPresenterManager;

/**
 * Activity base class that implements {@link MvpView MvpView} interface. Basic build block of any
 * application based on SimpleMVP library.
 * {@link MvpActivity MvpActivity} refers to presenter implementation using interface that is specified
 * by generic parameter. In most cases {@link MvpPresenter MvpPresenter} may be specified if no
 * custom methods (handlers) to be used.
 *
 * @param <P> presenter type, must be an interface that is implemented by presenter.
 * @param <S> state type, any class inherited from {@link MvpState MvpState}
 */
public abstract class MvpActivity<P extends MvpPresenter<S>, S extends MvpState>
        extends AppCompatActivity implements MvpView<S, P> {
    private final static String PRESENTER_ID = "presenter-id";
    protected final String tag = getClass().getSimpleName();
    protected MvpEventHandler<S, P> eventHandler;
    protected P presenter;
    private MvpPresenterManager manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int presenterId = savedInstanceState == null ? 0 : savedInstanceState.getInt(PRESENTER_ID);
        setContentView(getLayoutId());
        manager = MvpPresenterManager.getInstance(this);
        if (presenterId == 0) {
            presenter = onInitPresenter(manager);
        } else {
            presenter = manager.getPresenterInstance(presenterId);
        }
        eventHandler = new MvpEventHandler<>(this, presenter);
        getLifecycle().addObserver(eventHandler);
        presenter.connect(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRESENTER_ID, presenter.getId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.disconnect(this);
        if (isFinishing()) {
            manager.releasePresenter(presenter);
        }
        getLifecycle().removeObserver(eventHandler);
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
    public MvpViewHandle<S> getViewHandle() {
        return eventHandler;
    }

    @Override
    public MvpListener getMvpListener() {
        return eventHandler;
    }

    @Override
    public Bundle getArguments() {
        return getIntent().getExtras();
    }

    @Override
    public TextWatcher newTextWatcher(View view) {
        return eventHandler.newTextWatcher(view);
    }

    @Override
    public SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        return eventHandler.newQueryTextListener(view);
    }

    @Override
    public P getPresenter() {
        return presenter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.onOptionsItemSelected(getViewHandle(), item.getItemId());
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(getViewHandle(), requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        presenter.onRequestPermissionsResult(getViewHandle(), requestCode, permissions, grantResults);
    }

    @Override
    public int getMenuId() {
        return 0;
    }
}
