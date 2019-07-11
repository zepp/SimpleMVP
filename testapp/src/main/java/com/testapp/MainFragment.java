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
        durationSpinner = view.findViewById(R.id.duration_spinner);
        durationSpinner.setAdapter(new SpinnerAdapter(getContext(), new ToastDuration[]{
                ToastDuration.LongDuration, ToastDuration.ShortDuration}));
    }

    @Override
    public void onStart() {
        super.onStart();
        showToast.setOnClickListener(getMvpListener());
        toastText.addTextChangedListener(newTextWatcher(toastText));
        durationSpinner.setOnItemSelectedListener(getMvpListener());
    }

    @Override
    public void onStateChanged(MainState state) {
        toastText.setTextNoWatchers(state.text);
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }

    private class SpinnerAdapter extends ArrayAdapter<ToastDuration> {
        SpinnerAdapter(@NonNull Context context, @NonNull ToastDuration[] objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }
    }
}
