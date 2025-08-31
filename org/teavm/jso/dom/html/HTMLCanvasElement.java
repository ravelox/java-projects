package org.teavm.jso.dom.html;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MouseEvent;

public class HTMLCanvasElement {
    public CanvasRenderingContext2D getContext(String type) {
        return new CanvasRenderingContext2D();
    }

    public void addEventListener(String event, EventListener<MouseEvent> listener) {
        // no-op
    }

    public void setWidth(int w) {}
    public void setHeight(int h) {}
    public int getWidth() { return 0; }
    public int getHeight() { return 0; }
}
