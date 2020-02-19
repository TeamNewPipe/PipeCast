package org.schabi.newpipe.cast;

public enum MediaFormat {
    MUSIC_MP3   ("audio/mpeg",  "object.item.audioItem.musicTrack", "MP3"),
    MUSIC_M4A   ("audio/mp4",   "object.item.audioItem.musicTrack", "AAC_ISO"),
    MUSIC_WEBM  ("audio/webm",  "object.item.audioItem.musicTrack", null),
    MOVIE_WEBM  ("video/webm",  "object.item.videoItem.movie", null);
    // TODO: add all formats we need

    public String mimeType;
    public String upnpClass;
    public String dlnaProfile;

    MediaFormat(String mimeType, String upnpClass, String dlnaProfile) {
        this.mimeType = mimeType;
        this.upnpClass = upnpClass;
        this.dlnaProfile = dlnaProfile;
    }
}
