package org.schabi.newpipe.cast;

public abstract class Device {
    protected final String location;

    public Device(String location) {
        this.location = location;
    }

    public abstract String getName();
}
