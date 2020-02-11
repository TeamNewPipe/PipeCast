package org.schabi.newpipe.cast;

public enum ItemClass {
    MUSIC   ("object.item.audioItem.musicTrack"),
    MOVIE   ("object.item.videoItem.movie");

    public String upnpClass;

    ItemClass(String upnpClass) {
        this.upnpClass = upnpClass;
    }
}
