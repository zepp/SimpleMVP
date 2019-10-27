package com.testapp;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpEditText;
import com.simplemvp.view.MvpFragment;


public class MainFragment extends MvpFragment<MvpPresenter<MainState>, MainState> {
    private Button showToast;
    private Button showSnackBar;
    private MvpEditText toastText;
    private Spinner durationSpinner;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance(int presenterId) {
        MainFragment fragment = new MainFragment();
        fragment.initArguments(presenterId);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_main;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toastText = view.findViewById(R.id.toast_text);
        showToast = view.findViewById(R.id.show_toast);
        showSnackBar = view.findViewById(R.id.show_snackbar);
        durationSpinner = view.findViewById(R.id.duration_spinner);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        showToast.setOnClickListener(getMvpListener());
        showSnackBar.setOnClickListener(getMvpListener());
        toastText.addTextChangedListener(newTextWatcher(toastText));
        durationSpinner.setOnItemSelectedListener(getMvpListener());
        durationSpinner.setAdapter(new SpinnerAdapter(getContext(), new ActionDuration[]{
                ActionDuration.LongDuration, ActionDuration.ShortDuration}));
    }

    @Override
    public void onStateChanged(MainState state) {
        toastText.setTextNoWatchers(state.text);
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }

    private static class SpinnerAdapter extends ArrayAdapter<ActionDuration> {
        SpinnerAdapter(@NonNull Context context, @NonNull ActionDuration[] objects) {
            super(context, R.layout.holder_duration, R.id.duration, objects);
        }
    }
}
