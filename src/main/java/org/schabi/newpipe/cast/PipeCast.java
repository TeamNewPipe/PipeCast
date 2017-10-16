package org.schabi.newpipe.cast;

import org.schabi.newpipe.cast.exceptions.ProtocolNotFoundException;

public class PipeCast {
    private PipeCast() {
    }

    public static CastingProtocol[] getProtocols() {
        final ProtocolList[] values = ProtocolList.values();
        final CastingProtocol[] castingProtocols = new CastingProtocol[values.length];

        for (int i = 0; i < values.length; i++) castingProtocols[i] = values[i].getProtocol();

        return castingProtocols;
    }

    public static CastingProtocol getProtocol(int protocolId) throws ProtocolNotFoundException {
        for (ProtocolList item : ProtocolList.values()) {
            if (item.getProtocol().getProtocolId() == protocolId) {
                return item.getProtocol();
            }
        }

        throw new ProtocolNotFoundException();
    }

    public static CastingProtocol getProtocol(String protocolName) throws ProtocolNotFoundException {
        for (ProtocolList item : ProtocolList.values()) {
            if (item.getProtocol().getProtocolInfo().name.equals(protocolName)) {
                return item.getProtocol();
            }
        }

        throw new ProtocolNotFoundException();
    }

    public static int getIdOfProtocol(String protocolName) throws ProtocolNotFoundException {
        return getProtocol(protocolName).getProtocolId();
    }

    public static String getNameOfProtocol(int protocolId) throws ProtocolNotFoundException {
        return getProtocol(protocolId).getProtocolInfo().name;
    }
}
