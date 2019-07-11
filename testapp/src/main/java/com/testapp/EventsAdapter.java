package com.testapp;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    private final Resources resources;
    private List<Event> events = Collections.emptyList();
    private ItemClickListener listener;

    EventsAdapter(Resources resources) {
        this.resources = resources;
        setHasStableIds(true);
    }

    void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    void setListener(ItemClickListener listener) {
        this.listener = listener;
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

    interface ItemClickListener {
        void onItemClicked(Event event);
    }

    public final class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView id;
        private final TextView type;
        private final TextView view;
        private final ImageButton delete;
        private Event event;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.event_id);
            type = itemView.findViewById(R.id.event_type);
            view = itemView.findViewById(R.id.event_view);
            delete = itemView.findViewById(R.id.event_delete);
        }

        void bind(Event event) {
            this.event = event;
            id.setText(String.valueOf(event.id));
            type.setText(event.type);
            view.setText(resources.getResourceEntryName(event.view));
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClicked(event);
            }
        }
    }
}
