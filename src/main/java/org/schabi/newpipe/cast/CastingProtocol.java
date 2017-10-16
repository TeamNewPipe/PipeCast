package org.schabi.newpipe.cast;

public abstract class CastingProtocol {
    public class ProtocolInfo {
        public final String name;

        public ProtocolInfo(String name) {
            this.name = name;
        }
    }

    private final int protocolId;
    private final ProtocolInfo protocolInfo;

    public CastingProtocol(int id, String name) {
        this.protocolId = id;
        this.protocolInfo = new ProtocolInfo(name);
    }

    public final int getProtocolId() {
        return protocolId;
    }

    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    public abstract Discoverer getDiscoverer();
}
