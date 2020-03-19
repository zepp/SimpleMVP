package com.testapp.view;


import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpEditText;
import com.simplemvp.view.MvpFragment;
import com.testapp.R;
import com.testapp.common.ActionDuration;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;


public class MainFragment extends MvpFragment<MainPresenter, MainState> {
    private InputMethodManager imm;
    private Button showToast;
    private Button showSnackBar;
    private EditText toastText;
    private Spinner durationSpinner;
    private MvpEditText expression;
    private ImageButton eval;
    private Button reqPermissions;
    private CheckBox writeGranted;
    private Button invoke;

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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
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
        invoke = view.findViewById(R.id.custom_handler_invoke);
    }

    @Override
    public void onFirstStateChange(@NonNull MainState state) {
        super.onFirstStateChange(state);
        showToast.setOnClickListener(getMvpListener());
        showSnackBar.setOnClickListener(getMvpListener());
        toastText.addTextChangedListener(newTextWatcher(toastText));
        toastText.setText(state.text);
        toastText.setSelection(state.text.length());
        durationSpinner.setOnItemSelectedListener(getMvpListener());
        durationSpinner.setAdapter(new SpinnerAdapter(getContext(), new ActionDuration[]{
                ActionDuration.LongDuration, ActionDuration.ShortDuration}));
        expression.setText(state.expression);
        expression.addTextChangedListener(newTextWatcher(expression));
        eval.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            v.setEnabled(false);
            getMvpListener().onClick(v);
        });
        reqPermissions.setOnClickListener(v ->
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0));
        invoke.setOnClickListener(v -> presenter.customHandler(getViewHandle(), v.getId()));
    }

    @Override
    public void onStateChanged(@NonNull MainState state) {
        showToast.setEnabled(!state.text.isEmpty());
        showSnackBar.setEnabled(!state.text.isEmpty());
        writeGranted.setChecked(state.isWriteGranted);
        if (state.isEvaluated) {
            expression.setTextNoWatchers(state.expression);
        }
        eval.setEnabled(!state.expression.isEmpty());
    }

    @Override
    @NonNull
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }

    private static class SpinnerAdapter extends ArrayAdapter<ActionDuration> {
        SpinnerAdapter(@NonNull Context context, @NonNull ActionDuration[] objects) {
            super(context, R.layout.holder_duration, R.id.duration, objects);
        }
    }
}
