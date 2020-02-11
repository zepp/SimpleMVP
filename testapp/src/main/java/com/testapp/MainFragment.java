package com.testapp;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpEditText;
import com.simplemvp.view.MvpFragment;


public class MainFragment extends MvpFragment<MvpPresenter<MainState>, MainState> {
    private Button showToast;
    private Button showSnackBar;
    private EditText toastText;
    private Spinner durationSpinner;
    private MvpEditText expression;
    private Button eval;
    private Button reqPermissions;
    private CheckBox writeGranted;

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
        expression = view.findViewById(R.id.expression);
        eval = view.findViewById(R.id.eval);
        reqPermissions = view.findViewById(R.id.request_permissions);
        writeGranted = view.findViewById(R.id.write_granted);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        showToast.setOnClickListener(getMvpListener());
        showSnackBar.setOnClickListener(getMvpListener());
        toastText.addTextChangedListener(newTextWatcher(toastText));
        toastText.setText(state.text);
        durationSpinner.setOnItemSelectedListener(getMvpListener());
        durationSpinner.setAdapter(new SpinnerAdapter(getContext(), new ActionDuration[]{
                ActionDuration.LongDuration, ActionDuration.ShortDuration}));
        expression.setText(state.expression);
        expression.addTextChangedListener(newTextWatcher(expression));
        eval.setOnClickListener(getMvpListener());
        reqPermissions.setOnClickListener(v ->
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0));
    }

    @Override
    public void onStateChanged(MainState state) {
        showToast.setEnabled(!state.text.isEmpty());
        showSnackBar.setEnabled(!state.text.isEmpty());
        writeGranted.setChecked(state.isWriteGranted);
        expression.setTextNoWatchers(state.expression);
        eval.setEnabled(!state.expression.isEmpty());
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
