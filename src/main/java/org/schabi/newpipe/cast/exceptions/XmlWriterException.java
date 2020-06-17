package org.schabi.newpipe.cast.exceptions;

public class XmlWriterException extends Exception {
    public XmlWriterException() { }

    public XmlWriterException(String message) {
        super(message);
    }

    public XmlWriterException(Throwable cause) {
        super(cause);
    }

    public XmlWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    public XmlWriterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
