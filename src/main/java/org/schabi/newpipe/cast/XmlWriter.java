package org.schabi.newpipe.cast;

import org.schabi.newpipe.cast.exceptions.XmlWriterException;

public abstract class XmlWriter {
    abstract public void writeStartDocument(String encoding, String version) throws XmlWriterException;
    abstract public void writeStartElement(String localName) throws XmlWriterException;
    abstract public void writeAttribute(String localName, String value) throws XmlWriterException;
    abstract public void writeNamespace(String prefix, String namespaceURI) throws XmlWriterException;
    abstract public void writeCharacters(String text) throws XmlWriterException;
    abstract public void writeEndElement() throws XmlWriterException;
    abstract public void writeEndDocument() throws XmlWriterException;

    abstract public String end() throws XmlWriterException;
}
