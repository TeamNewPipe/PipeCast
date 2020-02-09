package org.schabi.newpipe.cast;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

public abstract class Device {
    public final String location;

    public Device(String location) {
        this.location = location;
    }

    public abstract String getName();

    public abstract void play(String url, String title, String creator, String mimeType, ItemClass itemClass) throws IOException, XMLStreamException;
}
