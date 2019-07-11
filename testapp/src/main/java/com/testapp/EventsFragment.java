package com.testapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.simplemvp.common.MvpPresenter;
import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpFragment;


public class EventsFragment extends MvpFragment<MvpPresenter<MainState>, MainState> {
    private RecyclerView events;
    private EventsAdapter eventsAdapter;

    public EventsFragment() {
        // Required empty public constructor
    }

    public static EventsFragment newInstance(int presenterId) {
        EventsFragment fragment = new EventsFragment();
        fragment.initArguments(presenterId);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_events;
    }

    @Override
    public void onStateChanged(MainState state) {
        eventsAdapter.setEvents(state.events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventsAdapter = new EventsAdapter(getContext().getResources());
        events = view.findViewById(R.id.events);
        events.setAdapter(eventsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        eventsAdapter.setListener(event ->
                presenter.onItemSelected(getViewHandle(), events.getId(), event));
    }

    @Override
    public MvpPresenter<MainState> onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }
}
