package com.testapp.presenter;

import com.simplemvp.common.MvpState;
import com.testapp.common.ActionDuration;
import com.testapp.common.Event;

import java.util.ArrayList;
import java.util.List;

public class MainState extends MvpState {
    public List<Event> events = new ArrayList<>();
    public long delay;
    public String text = "";
    public ActionDuration duration = ActionDuration.LongDuration;
    public boolean isSubscribedToConnectivity;
    public boolean isSubscribedToPowerSupply;
    public String searchPattern = "";
    public boolean isWriteGranted;
    public String expression = "";
    public int currentPage;

    public void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }

    public void addEvent(Event event) {
        setChanged(true);
        events.add(event);
    }

    public void removeEvent(Event event) {
        setChanged(events.remove(event));
    }

    public void clearEvents() {
        setChanged(true);
        events.clear();
    }

    public List<Event> getFilteredEvents() {
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

    public Event getEventById(int eventId) {
        for (Event event : events) {
            if (event.id == eventId) {
                return event;
            }
        }
        throw new RuntimeException("event is not found");
    }

    public void setSubscribedToConnectivity(boolean subscribedToConnectivity) {
        setChanged(isSubscribedToConnectivity != subscribedToConnectivity);
        isSubscribedToConnectivity = subscribedToConnectivity;
    }

    public void setSubscribedToPowerSupply(boolean subscribedToPowerSupply) {
        setChanged(isSubscribedToPowerSupply != subscribedToPowerSupply);
        isSubscribedToPowerSupply = subscribedToPowerSupply;
    }

    public void setDuration(ActionDuration duration) {
        setChanged(!this.duration.equals(duration));
        this.duration = duration;
    }

    public void setDelay(long delay) {
        setChanged(this.delay != delay);
        this.delay = delay;
    }

    public void setSearchPattern(String value) {
        setChanged(!searchPattern.equals(value));
        this.searchPattern = value;
    }

    public void setWriteGranted(boolean writeGranted) {
        setChanged(isWriteGranted != writeGranted);
        isWriteGranted = writeGranted;
    }

    public void setExpression(String expression) {
        setChanged(!this.expression.equals(expression));
        this.expression = expression;
    }

    public void setCurrentPage(int currentPage) {
        setChanged(this.currentPage != currentPage);
        this.currentPage = currentPage;
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
                ", events=" + events +
                ", isSubscribedToConnectivity=" + isSubscribedToConnectivity +
                ", isSubscribedToPowerSupply=" + isSubscribedToPowerSupply +
                ", duration=" + duration +
                ", delay=" + delay +
                ", searchPattern='" + searchPattern + '\'' +
                ", isWriteGranted=" + isWriteGranted +
                ", expression='" + expression + '\'' +
                '}';
    }
}
