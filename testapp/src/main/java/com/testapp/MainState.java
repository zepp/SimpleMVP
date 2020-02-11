package com.testapp;

import com.simplemvp.common.MvpState;

import java.util.ArrayList;
import java.util.List;

public class MainState extends MvpState {
    String text = "";
    List<Event> events = new ArrayList<>();
    int option;
    boolean isSwitchChecked;
    ActionDuration duration = ActionDuration.LongDuration;
    long delay;
    String searchPattern = "";
    boolean isWriteGranted;
    String expression = "";

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

    List<Event> getFilteredEvents() {
        if (searchPattern.isEmpty()) {
            return events;
        }
        List<Event> result = new ArrayList<>(events.size());
        for (Event event : events) {
            if (event.handler.toLowerCase().contains(searchPattern)) {
                result.add(event);
            }
        }
        return result;
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

    void setDelay(long delay) {
        setChanged(this.delay != delay);
        this.delay = delay;
    }

    void setSearchPattern(String value) {
        setChanged(!searchPattern.equals(value));
        this.searchPattern = value;
    }

    void setWriteGranted(boolean writeGranted) {
        setChanged(isWriteGranted != writeGranted);
        isWriteGranted = writeGranted;
    }

    void setExpression(String expression) {
        setChanged(!this.expression.equals(expression));
        this.expression = expression;
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
