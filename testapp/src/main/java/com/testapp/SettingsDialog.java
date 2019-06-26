package com.testapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.view.MvpDialogFragment;

public class SettingsDialog extends MvpDialogFragment<MvpPresenter<MainState>, MainState> {
    private Button ok;
    private SwitchCompat switch_;
    private RadioGroup options;

    public static SettingsDialog newInstance(int presenterId) {
        SettingsDialog dialog = new SettingsDialog();
        dialog.initArguments(presenterId);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_settings;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ok = view.findViewById(R.id.settings_ok);
        options = view.findViewById(R.id.options);
        switch_ = view.findViewById(R.id.settings_switch);
    }

    @Override
    public void onStart() {
        super.onStart();
        ok.setOnClickListener(view -> finish());
        options.setOnCheckedChangeListener(getViewImpl());
        switch_.setOnCheckedChangeListener(getViewImpl());
    }

    @Override
    public void onStateChanged(MainState state) {
        Log.d(tag, state.toString());
        if (state.option == 0) {
            options.clearCheck();
        } else {
            options.check(state.option);
        }
    }
}
