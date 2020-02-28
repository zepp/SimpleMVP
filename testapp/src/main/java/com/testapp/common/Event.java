package com.testapp.common;

import java.util.Date;

import io.objectbox.annotation.Convert;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.converter.PropertyConverter;

@Entity
public class Event {
    public String thread = Thread.currentThread().getName();
    public Date timestamp = new Date();
    @Convert(converter = EventTypeConverter.class, dbType = Integer.class)
    public EventType type;
    @Id
    public long id;
    public String handler;
    public int view;
    public String broadcast;
    public String info;

    public Event() {
    }

    public Event(EventType type, String handler, int view) {
        this.type = type;
        this.handler = handler;
        this.broadcast = null;
        this.view = view;
        this.info = null;
    }

    public Event(EventType type, String handler) {
        this.type = type;
        this.handler = handler;
        this.broadcast = null;
        this.view = 0;
        this.info = null;
    }

    public Event(String handler, int view) {
        this.type = EventType.UI;
        this.handler = handler;
        this.view = view;
        this.broadcast = null;
        this.info = null;
    }

    public Event(String broadcast, String info) {
        this.type = EventType.BROADCAST;
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

    public static class EventTypeConverter implements PropertyConverter<EventType, Integer> {
        @Override
        public EventType convertToEntityProperty(Integer databaseValue) {
            return EventType.values()[databaseValue];
        }

        @Override
        public Integer convertToDatabaseValue(EventType entityProperty) {
            return entityProperty.ordinal();
        }
    }
}
