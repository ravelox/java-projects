package org.teavm.jso.browser;

import org.teavm.jso.dom.html.HTMLDocument;

public class Window {
    private static final Window INSTANCE = new Window();

    public static Window current() {
        return INSTANCE;
    }

    public HTMLDocument getDocument() {
        return new HTMLDocument();
    }
}
