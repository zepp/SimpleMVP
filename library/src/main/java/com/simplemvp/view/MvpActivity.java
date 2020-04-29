/*
 * Copyright (c) 2019-2020 Pavel A. Sokolov
 */

package com.simplemvp.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    private final static String PRESENTER_ID = "mvp-presenter-id";
    protected final String tag = getClass().getSimpleName();
    protected MvpDispatcher<S> dispatcher;
    @NonNull
    protected P presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        int presenterId = savedInstanceState == null ? 0 : savedInstanceState.getInt(PRESENTER_ID);
        MvpPresenterManager manager = MvpPresenterManager.getInstance(this);
        if (presenterId == 0 || !manager.isPresenterExist(presenterId)) {
            presenter = onInitPresenter(manager);
        } else {
            presenter = manager.getPresenterInstance(presenterId);
        }
        dispatcher = new MvpDispatcher<>(this, savedInstanceState);
        dispatcher.initialize();
        dispatcher.setEnabled(getMenuId() == 0);
        presenter.connect(this);
        // onCreate recreates child fragments so presenter should be instantiated at first
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(PRESENTER_ID, presenter.getId());
        dispatcher.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            presenter.disconnect(this);
        }
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!dispatcher.setEnabled(true)) {
            dispatcher.postLastState();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onFirstStateChange(@NonNull S state) {
        Log.d(tag, "onFirstStateChange(" + state + ")");
    }

    @Override
    public int getMvpId() {
        return dispatcher.getId();
    }

    @Override
    @NonNull
    public MvpViewHandle<S> getViewHandle() {
        return dispatcher.getProxy();
    }

    @Override
    @NonNull
    public MvpListener getMvpListener() {
        return dispatcher;
    }

    @Override
    @NonNull
    public View getView() {
        return getWindow().getDecorView().getRootView();
    }

    @Override
    @NonNull
    public Context getContext() {
        return getBaseContext();
    }

    @Override
    public Bundle getArguments() {
        return getIntent().getExtras();
    }

    @Override
    @NonNull
    public TextWatcher newTextWatcher(@NonNull EditText view) {
        return dispatcher.newTextWatcher(view);
    }

    @Override
    @NonNull
    public SearchView.OnQueryTextListener newQueryTextListener(@NonNull SearchView view) {
        return dispatcher.newQueryTextListener(view);
    }

    @Override
    @NonNull
    public ViewPager.OnPageChangeListener newOnPageChangeListener(@NonNull ViewPager view) {
        return dispatcher.newOnPageChangeListener(view);
    }

    @Override
    @NonNull
    public View.OnClickListener newMvpClickListener(boolean isAutoLocking) {
        return dispatcher.newMvpClickListener(isAutoLocking);
    }

    @NonNull
    @Override
    public TabLayout.OnTabSelectedListener newTabLayoutListener(TabLayout view) {
        return dispatcher.newTabLayoutListener(view);
    }

    @Override
    public void showDialog(@NonNull DialogFragment dialog) {
        dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
    }

    @Override
    @NonNull
    public P getPresenter() {
        return presenter;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        presenter.onViewClicked(getViewHandle(), item.getItemId());
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // top bits are used to encode the fragment index
        if ((requestCode & 0xffff) == requestCode) {
            presenter.onActivityResult(getViewHandle(), requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // top bits are used to encode the fragment index
        if ((requestCode & 0xffff) == requestCode) {
            dispatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    @Override
    public int getMenuId() {
        return 0;
    }
}
