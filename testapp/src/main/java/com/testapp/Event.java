package com.testapp;

class Event {
    final EventType type;
    final int id;
    final String handler;
    final int view;
    final String broadcast;
    final String info;

    Event(EventType type, int id, String handler, int view) {
        this.type = type;
        this.id = id;
        this.handler = handler;
        this.broadcast = null;
        this.view = view;
        this.info = null;
    }

    Event(int id, String handler, int view) {
        this.type = EventType.UI;
        this.id = id;
        this.handler = handler;
        this.view = view;
        this.broadcast = null;
        this.info = null;
    }

    Event(int id, String broadcast, String info) {
        this.type = EventType.BROADCAST;
        this.id = id;
        this.handler = "onBroadcastReceived";
        this.broadcast = broadcast;
        this.info = info;
        this.view = 0;
    }
}
