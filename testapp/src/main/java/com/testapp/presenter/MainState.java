/*
 * Copyright (c) 2020 Pavel A. Sokolov
 */

package com.testapp.presenter;

import com.simplemvp.common.MvpState;
import com.testapp.common.ActionDuration;
import com.testapp.common.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainState extends MvpState {
    public List<Event> events = new ArrayList<>();
    public boolean isEventAdded;
    public int delay = 100;
    public String text = "";
    public ActionDuration duration = ActionDuration.LongDuration;
    public boolean isSubscribedToConnectivity;
    public boolean isSubscribedToPowerSupply;
    public String searchPattern = "";
    public boolean isWriteGranted;
    public String expression = "";
    public boolean isEvaluated;
    public int currentPage;
    public int progress;
    public boolean isStarted;
    public String fileName = "";

    public void setText(String text) {
        setChanged(!this.text.equals(text));
        this.text = text;
    }

    public void setEvents(List<Event> events) {
        setChanged(true);
        this.events = events;
    }

    public void addEvent(Event event) {
        setChanged(true);
        events.add(event);
        isEventAdded = true;
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

    public Event getEventById(long eventId) {
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

    public void setDelay(int delay) {
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

    public void setExpression(String expression, boolean isEvaluated) {
        setChanged(!this.expression.equals(expression));
        this.expression = expression;
        this.isEvaluated = isEvaluated;
    }

    public void setCurrentPage(int currentPage) {
        setChanged(this.currentPage != currentPage);
        this.currentPage = currentPage;
    }

    public void setProgress(int progress) {
        setChanged(this.progress != progress);
        this.progress = progress;
    }

    public void incProgress() {
        if (isStarted) {
            setChanged(true);
            progress = (progress + 1) % 3600;
        }
    }

    public void setStarted(boolean started) {
        setChanged(isStarted != started);
        isStarted = started;
    }

    public String getTextProgress() {
        return String.format(Locale.getDefault(), "%02d:%02d",
                progress / 60, progress % 60);
    }

    public boolean isTimerStateChanged() {
        return (progress == 0 && isStarted) || (progress > 0 && !isStarted);
    }

    public void setFileName(String fileName) {
        setChanged(!this.fileName.equals(fileName));
        this.fileName = fileName;
    }

    @Override
    public synchronized MainState clone() throws CloneNotSupportedException {
        MainState state = (MainState) super.clone();
        state.events = new ArrayList<>(events);
        return state;
    }

    @Override
    public void afterCommit() {
        isEventAdded = false;
        isEvaluated = false;
    }

    @Override
    public String toString() {
        return "MainState{" +
                "delay=" + delay +
                ", text='" + text + '\'' +
                ", duration=" + duration +
                ", isSubscribedToConnectivity=" + isSubscribedToConnectivity +
                ", isSubscribedToPowerSupply=" + isSubscribedToPowerSupply +
                ", searchPattern='" + searchPattern + '\'' +
                ", isWriteGranted=" + isWriteGranted +
                ", expression='" + expression + '\'' +
                ", currentPage=" + currentPage +
                ", progress=" + progress +
                ", isStarted=" + isStarted +
                '}';
    }
}
