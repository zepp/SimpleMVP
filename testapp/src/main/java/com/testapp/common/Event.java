package com.testapp.common;

import java.util.Date;

public class Event {
    public final String thread = Thread.currentThread().getName();
    public final Date timestamp = new Date();
    public final EventType type;
    public final int id;
    public final String handler;
    public final int view;
    public final String broadcast;
    public final String info;

    public Event(EventType type, int id, String handler, int view) {
        this.type = type;
        this.id = id;
        this.handler = handler;
        this.broadcast = null;
        this.view = view;
        this.info = null;
    }

    public Event(EventType type, int id, String handler) {
        this.type = type;
        this.id = id;
        this.handler = handler;
        this.broadcast = null;
        this.view = 0;
        this.info = null;
    }

    public Event(int id, String handler, int view) {
        this.type = EventType.UI;
        this.id = id;
        this.handler = handler;
        this.view = view;
        this.broadcast = null;
        this.info = null;
    }

    public Event(int id, String broadcast, String info) {
        this.type = EventType.BROADCAST;
        this.id = id;
        this.handler = "onBroadcastReceived";
        this.broadcast = broadcast;
        this.info = info;
        this.view = 0;
    }

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", handler='" + handler + '\'' +
                '}';
    }
}
