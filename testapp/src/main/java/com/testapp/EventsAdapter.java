package com.testapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    private List<Event> events = Collections.emptyList();

    public EventsAdapter() {
        setHasStableIds(true);
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        return new EventHolder(inflater.inflate(R.layout.holder_event, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EventHolder eventHolder, int position) {
        eventHolder.bind(events.get(position));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public long getItemId(int position) {
        return events.get(position).id;
    }

    public final static class EventHolder extends RecyclerView.ViewHolder {
        private final TextView id;
        private final TextView text;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.event_id);
            text = itemView.findViewById(R.id.event_text);
        }

        void bind(Event event) {
            id.setText(String.valueOf(event.id));
            text.setText(event.text);
        }
    }
}
