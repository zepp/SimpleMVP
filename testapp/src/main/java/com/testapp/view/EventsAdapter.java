package com.testapp.view;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.util.Consumer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.testapp.R;
import com.testapp.common.Event;
import com.testapp.common.EventType;

import java.util.Collections;
import java.util.List;

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventHolder> {
    private final Resources resources;
    private List<Event> events = Collections.emptyList();
    private Consumer<Event> listener;

    EventsAdapter(Resources resources) {
        this.resources = resources;
        setHasStableIds(true);
        listener = event -> {
        };
    }

    void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    void setListener(Consumer<Event> listener) {
        this.listener = listener;
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

    public final class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView id;
        private final TextView handler;
        private final TextView source;
        private final TextView info;
        private final ImageButton delete;
        private Event event;

        EventHolder(@NonNull View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.event_id);
            handler = itemView.findViewById(R.id.event_handler);
            source = itemView.findViewById(R.id.event_source);
            info = itemView.findViewById(R.id.event_info);
            delete = itemView.findViewById(R.id.event_delete);
        }

        void bind(Event event) {
            this.event = event;
            id.setText(String.valueOf(event.id));
            handler.setText(event.handler);
            if (event.type == EventType.LIFECYCLE || event.type == EventType.UI) {
                source.setText(resources.getResourceEntryName(event.view));
                info.setVisibility(View.GONE);
            } else {
                source.setText(event.broadcast);
                info.setVisibility(View.VISIBLE);
                info.setText(event.info);
            }
            delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.accept(event);
        }
    }
}
