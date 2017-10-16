package org.schabi.newpipe.cast;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class Discoverer {
    public abstract List<Device> discoverDevices() throws IOException, InterruptedException, ExecutionException;
}
