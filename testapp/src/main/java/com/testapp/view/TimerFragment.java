package com.testapp.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpFragment;
import com.testapp.R;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;


public class TimerFragment extends MvpFragment<MainPresenter, MainState> {
    public TimerFragment() {
        // Required empty public constructor
    }

    public static TimerFragment newInstance(int presenterId) {
        TimerFragment fragment = new TimerFragment();
        fragment.initArguments(presenterId);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_timer;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
    }

    @Override
    public void onStateChanged(MainState state) {
    }

    @Override
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }
}
