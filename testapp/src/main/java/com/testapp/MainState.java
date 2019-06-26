package com.testapp;

import com.simplemvp.common.MvpState;

import java.util.ArrayList;
import java.util.List;

class MainState extends MvpState {
    String text = "";
    List<Event> events = new ArrayList<>();

    void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }

    void addEvent(Event event) {
        setChanged(true);
        events.add(event);
    }

    @Override
    public synchronized MainState clone() throws CloneNotSupportedException {
        MainState state = (MainState) super.clone();
        state.events = new ArrayList<>(events);
        return state;
    }
}
