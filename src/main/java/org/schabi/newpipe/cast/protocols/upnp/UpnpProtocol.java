package org.schabi.newpipe.cast.protocols.upnp;

import org.schabi.newpipe.cast.CastingProtocol;
import org.schabi.newpipe.cast.Discoverer;

public class UpnpProtocol extends CastingProtocol {
    public UpnpProtocol(int id, String name) {
        super(id, name);
    }

    @Override
    public Discoverer getDiscoverer() {
        return UpnpDiscoverer.getInstance();
    }
}
