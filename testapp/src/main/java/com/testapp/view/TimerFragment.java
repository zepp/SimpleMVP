package com.testapp.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpFragment;
import com.testapp.R;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;

import java.util.Locale;


public class TimerFragment extends MvpFragment<MainPresenter, MainState> {
    private CircleProgress progress;
    private TextView text;
    private ImageButton startStop;

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
        progress = view.findViewById(R.id.timer_progress);
        text = view.findViewById(R.id.timer_text);
        startStop = view.findViewById(R.id.timer_start_stop);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        startStop.setOnClickListener(getMvpListener());
    }

    @Override
    public void onStateChanged(MainState state) {
        progress.setProgress(state.progress);
        text.setText(String.format(Locale.getDefault(), "%02d:%02d",
                (state.progress / 60) % 60, state.progress % 60));
        startStop.setImageDrawable(getResources().getDrawable(state.isStarted ? R.drawable.ic_stop : R.drawable.ic_start));
    }

    @Override
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }
}
