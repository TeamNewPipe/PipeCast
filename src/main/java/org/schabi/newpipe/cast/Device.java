package org.schabi.newpipe.cast;

import org.schabi.newpipe.cast.exceptions.XmlWriterException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public abstract class Device {
    public final String location;
    public final InetAddress inetAddress;

    public Device(String location, InetAddress inetAddress) {
        this.location = location;
        this.inetAddress = inetAddress;
    }

    public abstract String getName();

    public abstract void play(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XmlWriterException;

    public abstract void addToQueue(String url, String title, String creator, MediaFormat mediaFormat) throws IOException, XmlWriterException;

    public abstract void playPause() throws IOException, XmlWriterException, ParserConfigurationException, SAXException;

    public abstract List<MediaFormat> getSupportedFormats() throws IOException, ParserConfigurationException, SAXException, XmlWriterException;

    public abstract Stoppable startBackgroundWork() throws IOException;
}
