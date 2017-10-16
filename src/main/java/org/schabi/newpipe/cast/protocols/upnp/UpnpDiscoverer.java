package org.schabi.newpipe.cast.protocols.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.schabi.newpipe.cast.Device;
import org.schabi.newpipe.cast.Discoverer;

public class UpnpDiscoverer extends Discoverer {
    private static final UpnpDiscoverer instance = new UpnpDiscoverer();

    private DatagramSocket socket;

    public static UpnpDiscoverer getInstance() {
        return instance;
    }

    private List<Device> devices;

    private class ReceiveDevices implements Callable<Object> {
        @Override
        public Object call() throws IOException {
            devices = new ArrayList<Device>();
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                String dataString = new String(data, packet.getOffset(), packet.getLength());
                Scanner dataScanner = new Scanner(dataString);
                String location = "";
                Boolean add = false;
                while (dataScanner.hasNextLine()) {
                    String line = dataScanner.nextLine();
                    if (line.startsWith("LOCATION: ")) {
                        location = line.substring(10);
                    }
                    // Some devices still respond, even if they aren't the type we asked for, so we've to filter them out
                    if (line.startsWith("ST: urn:schemas-upnp-org:device:MediaRenderer:1")) {
                        add = true;
                    }
                }
                if (add && !location.equals("")) {
                    devices.add(new UpnpDevice(location));
                }
                dataScanner.close();
            }
        }
    }

    @Override
    public List<Device> discoverDevices() throws IOException, InterruptedException, ExecutionException {
        socket = new DatagramSocket(null);
        InetSocketAddress address = new InetSocketAddress("192.168.1.124", 1900); // TODO: get IP automagically
        socket.bind(address);

        byte[] request = new String("M-SEARCH * HTTP/1.1\n" +
                                    "HOST: 239.255.255.250:1900\n" +
                                    "MAN: \"ssdp:discover\"\n" +
                                    "MX: 5\n" +
                                    "ST: urn:schemas-upnp-org:device:MediaRenderer:1\n" +
                                    "CFPN.UPNP.ORG: PipeCast\n\n").getBytes();
        DatagramPacket requestDatagram = new DatagramPacket(request, request.length, Inet4Address.getByName("239.255.255.250"), 1900);
        socket.send(requestDatagram);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(new ReceiveDevices());
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
        }
        executor.shutdownNow();

        socket.close();

        return devices;
    }
}
