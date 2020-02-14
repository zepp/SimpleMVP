package com.testapp.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.view.View;
import android.widget.TextView;

import com.simplemvp.view.MvpDialogFragment;
import com.testapp.R;
import com.testapp.common.Event;
import com.testapp.common.EventType;
import com.testapp.presenter.MainPresenter;
import com.testapp.presenter.MainState;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EventInfoDialog extends MvpDialogFragment<MainPresenter, MainState> {
    private final static String EVENT_ID = "event-id";
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
    private TextView title;
    private TextView time;
    private TextView thread;
    private TextView view;
    private TextView broadcast;
    private TextView broadcastInfo;
    private Group viewGroup;
    private Group broadcastGroup;


    public static EventInfoDialog newInstance(int presenterId, int eventId) {
        EventInfoDialog dialog = new EventInfoDialog();
        Bundle args = dialog.initArguments(presenterId);
        args.putInt(EVENT_ID, eventId);
        return dialog;
    }

    @Override
    public int getLayoutId() {
        return R.layout.dialog_event_info;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = view.findViewById(R.id.info_title);
        time = view.findViewById(R.id.info_time);
        thread = view.findViewById(R.id.info_thread);
        this.view = view.findViewById(R.id.info_view);
        broadcast = view.findViewById(R.id.info_broadcast);
        broadcastInfo = view.findViewById(R.id.info_broadcast_info);
        viewGroup = view.findViewById(R.id.info_view_group);
        broadcastGroup = view.findViewById(R.id.info_broadcast_group);
    }

    @Override
    public void onFirstStateChange(MainState state) {
        super.onFirstStateChange(state);
        Event event = state.getEventById(getArguments().getInt(EVENT_ID));
        title.setText(event.handler);
        time.setText(format.format(event.timestamp));
        thread.setText(event.thread);
        if (event.type == EventType.BROADCAST) {
            broadcastGroup.setVisibility(View.VISIBLE);
            broadcast.setText(event.broadcast);
            broadcastInfo.setText(event.info);
        } else {
            view.setText(getResources().getResourceEntryName(event.view));
            viewGroup.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStateChanged(MainState state) {
    }
}
