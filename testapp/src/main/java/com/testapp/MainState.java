package com.testapp;

import com.simplemvp.common.MvpState;

import java.util.ArrayList;
import java.util.List;

class MainState extends MvpState {
    String text = "";
    List<Event> events = new ArrayList<>();
    int option;

    void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }

    void addEvent(Event event) {
        setChanged(true);
        events.add(event);
    }

    void setOption(int option) {
        setChanged(this.option != option);
        this.option = option;
    }

    @Override
    public synchronized MainState clone() throws CloneNotSupportedException {
        MainState state = (MainState) super.clone();
        state.events = new ArrayList<>(events);
        return state;
    }

    @Override
    public String toString() {
        return "MainState{" +
                "text='" + text + '\'' +
                ", option=" + option +
                "} " + super.toString();
    }
}
