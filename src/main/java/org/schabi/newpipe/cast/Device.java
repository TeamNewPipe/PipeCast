package org.schabi.newpipe.cast;

import org.schabi.newpipe.cast.exceptions.XmlWriterException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public abstract class Device {
    public final String location;

    public Device(String location) {
        this.location = location;
    }

    public abstract String getName();

    public abstract void play(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XmlWriterException;

    public abstract void addToQueue(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XmlWriterException;

    public abstract List<MediaFormat> getSupportedFormats() throws IOException, ParserConfigurationException, SAXException, XmlWriterException;

    public abstract Stoppable startBackgroundWork() throws IOException;
}
