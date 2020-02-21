package com.testapp.view;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.simplemvp.presenter.MvpPresenterManager;
import com.simplemvp.view.MvpFragment;
import com.testapp.R;
import com.testapp.common.Event;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;

import java.util.List;


public class EventsFragment extends MvpFragment<MainPresenter, MainState> {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventsAdapter = new EventsAdapter(getContext().getResources());
        events = view.findViewById(R.id.events);
        events.setAdapter(eventsAdapter);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        eventsAdapter.setListener(pair ->
                presenter.onItemSelected(getViewHandle(), pair.first.getId(), pair.second));
    }

    @Override
    public void onStateChanged(MainState state) {
        List<Event> items = state.getFilteredEvents();
        eventsAdapter.setEvents(items);
        if (state.isEventAdded) {
            events.scrollToPosition(items.size() - 1);
        }
    }

    @Override
    public MainPresenter onInitPresenter(MvpPresenterManager manager) {
        return manager.getPresenterInstance(getPresenterId(getArguments()));
    }
}
