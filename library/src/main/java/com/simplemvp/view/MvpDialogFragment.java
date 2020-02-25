/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.simplemvp.common.MvpListener;
import com.simplemvp.common.MvpPresenter;
import com.simplemvp.common.MvpState;
import com.simplemvp.common.MvpView;
import com.simplemvp.common.MvpViewHandle;
import com.simplemvp.presenter.MvpPresenterManager;

/**
 * Dialog base class that implements {@link MvpView MvpView} interface.
 * {@link MvpDialogFragment MvpDialogFragment} refers to presenter implementation using interface
 * that is specified by generic parameter. In most cases {@link MvpPresenter MvpPresenter} may be
 * specified if no custom methods (handlers) to be used.
 *
 * @param <P> presenter type, must be an interface that is implemented by presenter.
 * @param <S> state type, any class inherited from {@link MvpState MvpState}
 */
public abstract class MvpDialogFragment<P extends MvpPresenter<S>, S
        extends MvpState> extends DialogFragment implements MvpView<S, P> {
    private final static String PRESENTER_ID = "presenter-id";
    protected final String tag = getClass().getSimpleName();
    protected MvpEventHandler<S> eventHandler;
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
        presenter.connect(this);
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
        presenter.disconnect(this);
        getLifecycle().removeObserver(eventHandler);
    }

    @Override
    public void onFirstStateChange(S state) {
        Log.d(tag, "onFirstStateChange(" + state + ")");
    }
    
    @Override
    public MvpViewHandle<S> getViewHandle() {
        return eventHandler.getProxy();
    }

    @Override
    public MvpListener getMvpListener() {
        return eventHandler;
    }

    @Override
    public TextWatcher newTextWatcher(EditText view) {
        return eventHandler.newTextWatcher(view);
    }

    @Override
    public SearchView.OnQueryTextListener newQueryTextListener(SearchView view) {
        return eventHandler.newQueryTextListener(view);
    }

    @Override
    public ViewPager.OnPageChangeListener newOnPageChangeListener(ViewPager view) {
        return eventHandler.newOnPageChangeListener(view);
    }

    @Override
    public void showDialog(DialogFragment dialog) {
        dialog.show(getFragmentManager(), dialog.getClass().getSimpleName());
    }

    @Override
    public P getPresenter() {
        return presenter;
    }

    public void finish() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public int getMenuId() {
        return 0;
    }

    @Override
    public P onInitPresenter(MvpPresenterManager manager) {
        if (getArguments() == null) {
            throw new RuntimeException("presenter ID is not supplied");
        } else {
            return manager.getPresenterInstance(getPresenterId(getArguments()));
        }
    }
}
