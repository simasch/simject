package org.simject.util;

public enum Protocol {

    Xml("xml", "text/xml"), Binary("bin",
            "application/x-java-serialized-object");

    private String string;

    private String contentType;

    private Protocol(final String string, final String contentType) {
        this.string = string;
        this.contentType = contentType;
    }

    public String getString() {
        return this.string;
    }

    public String getContentType() {
        return this.contentType;
    }
}
