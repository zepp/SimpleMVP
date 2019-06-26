package com.testapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.view.MvpDialogFragment;

public class SettingsDialog extends MvpDialogFragment<MvpPresenter<MainState>, MainState> {
    private Button ok;
    private RadioButton option1;
    private RadioButton option2;
    private RadioButton option3;
    private SwitchCompat switch_;

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
        option1 = view.findViewById(R.id.settings_option_1);
        option2 = view.findViewById(R.id.settings_option_2);
        option3 = view.findViewById(R.id.settings_option_3);
        switch_ = view.findViewById(R.id.settings_switch);
    }

    @Override
    public void onStart() {
        super.onStart();
        ok.setOnClickListener(view -> finish());
        option1.setOnClickListener(getViewImpl());
        option2.setOnClickListener(getViewImpl());
        option3.setOnClickListener(getViewImpl());
        switch_.setOnCheckedChangeListener(getViewImpl());
    }

    @Override
    public void onStateChanged(MainState state) {

    }
}
