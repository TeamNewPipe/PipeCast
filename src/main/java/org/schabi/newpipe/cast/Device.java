package org.schabi.newpipe.cast;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

public abstract class Device {
    public final String location;

    public Device(String location) {
        this.location = location;
    }

    public abstract String getName();

    public abstract void play(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XMLStreamException;

    public abstract void addToQueue(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XMLStreamException;

    public abstract List<MediaFormat> getSupportedFormats() throws IOException, XMLStreamException, ParserConfigurationException, SAXException;

    public abstract void startBackgroundWork() throws IOException;
}
