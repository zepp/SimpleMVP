package com.testapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpActivity;
import com.simplemvp.view.MvpEditText;

public class MainActivity extends MvpActivity<MvpPresenter<MainState>, MainState> {
    private Toolbar toolbar;
    private FloatingActionButton fab;
    private Button button;
    private MvpEditText editText;
    private Spinner spinner;
    private RecyclerView events;
    private EventsAdapter eventsAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_main;
    }

    @Override
    public void onStateChanged(MainState state) {
        eventsAdapter.setEvents(state.events);
        editText.setTextNoWatchers(state.text);
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.newPresenterInstance(MainPresenter.class, MainState.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventsAdapter = new EventsAdapter();
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(new SpinnerAdapter(this, new String[]{"One", "Two", "Three"}));
        events = findViewById(R.id.events);
        events.setAdapter(eventsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fab.setOnClickListener(getMvpListener());
        button.setOnClickListener(getMvpListener());
        editText.addTextChangedListener(newTextWatcher(editText));
        spinner.setOnItemSelectedListener(getMvpListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_settings).setOnMenuItemClickListener(view -> {
            SettingsDialog dialog = SettingsDialog.newInstance(presenter.getId());
            dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            return true;
        });
        return true;
    }

    private class SpinnerAdapter extends ArrayAdapter<String> {
        SpinnerAdapter(@NonNull Context context, @NonNull String[] objects) {
            super(context, android.R.layout.simple_spinner_item, objects);
        }
    }
}
