package com.testapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpActivity;

public class MainActivity extends MvpActivity<MvpPresenter<MainState>, MainState> {
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private Button button;
    private EditText editText;
    private Spinner spinner;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void onStateChanged(MainState state) {

    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenter.class, MainState.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new SpinnerAdapter(this, new String[]{"One", "Two", "Three"}));
    }

    @Override
    protected void onStart() {
        super.onStart();
        fab.setOnClickListener(getViewImpl());
        button.setOnClickListener(getViewImpl());
        editText.addTextChangedListener(getViewImpl().newTextWatcher(editText));
        spinner.setOnItemSelectedListener(getViewImpl());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private class SpinnerAdapter extends ArrayAdapter<String> {
        public SpinnerAdapter(@NonNull Context context, @NonNull String[] objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }
    }
}
