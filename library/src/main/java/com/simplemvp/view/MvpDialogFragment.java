/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
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
    @NonNull
    protected P presenter;

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
        MvpPresenterManager manager = MvpPresenterManager.getInstance(getContext());
        if (presenterId == 0 || !manager.isPresenterExist(presenterId)) {
            presenter = onInitPresenter(manager);
        } else {
            presenter = manager.getPresenterInstance(presenterId);
        }
        eventHandler = new MvpEventHandler<>(this, savedInstanceState);
        eventHandler.initialize();
        eventHandler.setEnabled(true);
        presenter.connect(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public <T extends View> T findViewById(int id) {
        return getView().findViewById(id);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PRESENTER_ID, presenter.getId());
        eventHandler.saveId(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        presenter.disconnect(this);
    }

    @Override
    public void onFirstStateChange(@NonNull S state) {
        Log.d(tag, "onFirstStateChange(" + state + ")");
    }

    @Override
    public int getMvpId() {
        return eventHandler.getId();
    }

    @Override
    @NonNull
    public MvpViewHandle<S> getViewHandle() {
        return eventHandler.getProxy();
    }

    @Override
    @NonNull
    public MvpListener getMvpListener() {
        return eventHandler;
    }

    @Override
    @NonNull
    public TextWatcher newTextWatcher(@NonNull EditText view) {
        return eventHandler.newTextWatcher(view);
    }

    @Override
    @NonNull
    public SearchView.OnQueryTextListener newQueryTextListener(@NonNull SearchView view) {
        return eventHandler.newQueryTextListener(view);
    }

    @Override
    @NonNull
    public ViewPager.OnPageChangeListener newOnPageChangeListener(@NonNull ViewPager view) {
        return eventHandler.newOnPageChangeListener(view);
    }

    @Override
    @NonNull
    public View.OnClickListener newMvpClickListener(boolean isAutoLocking) {
        return eventHandler.newMvpClickListener(isAutoLocking);
    }

    @NonNull
    @Override
    public TabLayout.OnTabSelectedListener newTabLayoutListener(TabLayout view) {
        return eventHandler.newTabLayoutListener(view);
    }

    @Override
    public void showDialog(@NonNull DialogFragment dialog) {
        dialog.show(requireFragmentManager(), dialog.getClass().getSimpleName());
    }

    @Override
    @NonNull
    public P getPresenter() {
        return presenter;
    }

    public void finish() {
        requireFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public final int getMenuId() {
        return 0;
    }

    @Override
    @NonNull
    public P onInitPresenter(@NonNull MvpPresenterManager manager) {
        if (getArguments() == null) {
            throw new RuntimeException("presenter ID is not supplied");
        } else {
            return manager.getPresenterInstance(getPresenterId(getArguments()));
        }
    }
}
