package org.teavm.jso.dom.events;

public interface EventListener<T> {
    void handleEvent(T evt);
}
