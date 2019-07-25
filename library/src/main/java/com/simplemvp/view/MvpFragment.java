/*
 * Copyright (c) 2019 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpPresenterManager;

/**
 * Fragment base class that implements {@link MvpView MvpView} interface.
 * {@link MvpDialogFragment MvpDialogFragment} refers to presenter implementation using interface
 * that is specified by generic parameter. In most cases {@link MvpPresenter MvpPresenter} may be
 * specified if no custom methods (handlers) to be used.
 *
 * @param <P> presenter type, must be an interface that is implemented by presenter.
 * @param <S> state type, any class inherited from {@link MvpState MvpState}
 */
public abstract class MvpFragment<P extends MvpPresenter<S>, S extends MvpState>
        extends Fragment implements MvpView<S, P> {
    private final static String PRESENTER_ID = "presenter-id";
    protected final String tag = getClass().getSimpleName();
    protected MvpEventHandler<S, P> eventHandler;
    protected P presenter;
    private MvpPresenterManager manager;

    protected Bundle initArguments(int presenterId) {
        Bundle args = new Bundle();
        args.putInt(PRESENTER_ID, presenterId);
        setArguments(args);
        return args;
    }

    protected static int getPresenterId(Bundle bundle) {
        return bundle.getInt(PRESENTER_ID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int presenterId = savedInstanceState == null ? 0 : getPresenterId(savedInstanceState);
        manager = MvpPresenterManager.getInstance(getContext());
        if (presenterId == 0) {
            presenter = onInitPresenter(manager);
        } else {
            presenter = manager.getPresenterInstance(presenterId);
        }
        eventHandler = new MvpEventHandler<>(this, presenter);
        eventHandler.setEnabled(getMenuId() == 0);
        getLifecycle().addObserver(eventHandler);
        presenter.connect(getViewHandle());
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
        presenter.disconnect(getViewHandle());
        manager.releasePresenter(presenter);
        getLifecycle().removeObserver(eventHandler);
    }

    @CallSuper
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getMenuId() != 0) {
            inflater.inflate(getMenuId(), menu);
            eventHandler.setEnabled(true);
        }
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
    public TextWatcher newTextWatcher(View view) {
        return eventHandler.newTextWatcher(view);
    }

    @Override
    public SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        return eventHandler.newQueryTextListener(view);
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
    }

    @Override
    public P getPresenter() {
        return presenter;
    }

    @Override
    public void finish() {
        getFragmentManager().beginTransaction().remove(this).commit();
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
