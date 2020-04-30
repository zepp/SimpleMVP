package com.testapp.view;


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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.simplemvp.view.MvpEditText;
import com.simplemvp.view.MvpHostedFragment;
import com.testapp.R;
import com.testapp.common.ActionDuration;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;


public class MainFragment extends MvpHostedFragment<MainPresenter, MainState> {
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
    private Button select;
    private TextView fileName;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        return new MainFragment();
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
        toastText = view.findViewById(R.id.main_toast_text);
        showToast = view.findViewById(R.id.main_show_toast);
        showSnackBar = view.findViewById(R.id.main_show_snackbar);
        durationSpinner = view.findViewById(R.id.main_duration_spinner);
        expression = view.findViewById(R.id.main_expression);
        eval = view.findViewById(R.id.main_eval);
        reqPermissions = view.findViewById(R.id.main_request_permissions);
        writeGranted = view.findViewById(R.id.main_write_granted);
        invoke = view.findViewById(R.id.main_custom_handler_invoke);
        select = view.findViewById(R.id.main_select);
        fileName = view.findViewById(R.id.main_selected_file);
    }

    @Override
    public void onFirstStateChange(@NonNull MainState state) {
        super.onFirstStateChange(state);
        showToast.setOnClickListener(getMvpListener());
        showSnackBar.setOnClickListener(getMvpListener());
        toastText.addTextChangedListener(newTextWatcher(toastText));
        toastText.setText(state.text);
        toastText.setSelection(state.text.length());
        toastText.setOnEditorActionListener(getMvpListener());
        durationSpinner.setOnItemSelectedListener(getMvpListener());
        durationSpinner.setAdapter(new SpinnerAdapter(getContext(), ActionDuration.values()));
        expression.setText(state.expression);
        expression.addTextChangedListener(newTextWatcher(expression));
        expression.setOnEditorActionListener(getMvpListener());
        eval.setOnClickListener(v -> {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            v.setEnabled(false);
            getMvpListener().onClick(v);
        });
        reqPermissions.setOnClickListener(getMvpListener());
        invoke.setOnClickListener(v -> presenter.customHandler(getViewHandle(), v.getId()));
        select.setOnClickListener(getMvpListener());
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
        fileName.setText(state.fileName);
    }

    private static class SpinnerAdapter extends ArrayAdapter<ActionDuration> {
        SpinnerAdapter(@NonNull Context context, @NonNull ActionDuration[] objects) {
            super(context, R.layout.holder_duration, R.id.duration, objects);
        }
    }
}
