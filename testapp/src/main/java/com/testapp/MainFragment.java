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
    private Button button;
    private MvpEditText editText;
    private Spinner spinner;

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
        editText = view.findViewById(R.id.edit_text);
        button = view.findViewById(R.id.button);
        editText = view.findViewById(R.id.edit_text);
        spinner = view.findViewById(R.id.spinner);
        spinner.setAdapter(new SpinnerAdapter(getContext(), new String[]{"One", "Two", "Three"}));
    }

    @Override
    public void onStart() {
        super.onStart();
        button.setOnClickListener(getMvpListener());
        editText.addTextChangedListener(newTextWatcher(editText));
        spinner.setOnItemSelectedListener(getMvpListener());
    }

    @Override
    public void onStateChanged(MainState state) {
        editText.setTextNoWatchers(state.text);
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }

    private class SpinnerAdapter extends ArrayAdapter<String> {
        SpinnerAdapter(@NonNull Context context, @NonNull String[] objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }
    }
}
