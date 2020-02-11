package com.testapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.view.MvpDialogFragment;

public class SettingsDialog extends MvpDialogFragment<MvpPresenter<MainState>, MainState> {
    private SeekBar delay;
    private SwitchCompat connectivity;
    private SwitchCompat powerSupply;
    private Button ok;

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
        delay = view.findViewById(R.id.settings_delay);
        connectivity = view.findViewById(R.id.settings_connectivity);
        powerSupply = view.findViewById(R.id.settings_power_supply);
        ok = view.findViewById(R.id.settings_ok);
    }

    @Override
    public void onStart() {
        super.onStart();
        ok.setOnClickListener(view -> finish());
        connectivity.setOnCheckedChangeListener(getMvpListener());
        powerSupply.setOnCheckedChangeListener(getMvpListener());
        delay.setOnSeekBarChangeListener(getMvpListener());
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        delay.setProgress((int) state.delay / 100);
    }

    @Override
    public void onStateChanged(MainState state) {
        Log.d(tag, state.toString());
        connectivity.setChecked(state.isSubscribedToConnectivity);
        connectivity.setEnabled(!state.isSubscribedToConnectivity);
        powerSupply.setChecked(state.isSubscribedToPowerSupply);
        powerSupply.setEnabled(!state.isSubscribedToPowerSupply);
    }
}
