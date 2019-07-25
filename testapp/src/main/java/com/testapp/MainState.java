package com.testapp;

import com.simplemvp.common.MvpState;

import java.util.ArrayList;
import java.util.List;

class MainState extends MvpState {
    String text = "";
    List<Event> events = new ArrayList<>();
    int option;
    boolean isSwitchChecked;
    ActionDuration duration = ActionDuration.LongDuration;

    void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }

    void addEvent(Event event) {
        setChanged(true);
        events.add(event);
    }

    void removeEvent(Event event) {
        setChanged(events.remove(event));
    }

    void clearEvents() {
        setChanged(true);
        events.clear();
    }

    void setOption(int option) {
        setChanged(this.option != option);
        this.option = option;
    }

    void setSwitchChecked(boolean switchChecked) {
        setChanged(isSwitchChecked != switchChecked);
        isSwitchChecked = switchChecked;
    }

    void setDuration(ActionDuration duration) {
        setChanged(!this.duration.equals(duration));
        this.duration = duration;
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
                "} ";
    }
}
