package org.schabi.newpipe.cast.exceptions;

public class ProtocolNotFoundException extends Exception {
    private static final long serialVersionUID = 3248824733038646732L;

    public ProtocolNotFoundException() {
    }

    public ProtocolNotFoundException(String message) {
        super(message);
    }

    public ProtocolNotFoundException(Throwable cause) {
        super(cause);
    }

    public ProtocolNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtocolNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
