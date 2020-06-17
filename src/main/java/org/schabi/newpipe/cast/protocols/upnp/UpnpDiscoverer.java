package org.schabi.newpipe.cast.protocols.upnp;

import org.schabi.newpipe.cast.Device;
import org.schabi.newpipe.cast.Discoverer;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;

public class UpnpDiscoverer extends Discoverer {
    private static final UpnpDiscoverer instance = new UpnpDiscoverer();

    public static UpnpDiscoverer getInstance() {
        return instance;
    }

    private List<Device> devices;

    private class ReceiveDevices implements Callable<Object> {
        private DatagramSocket socket;

        public ReceiveDevices(DatagramSocket skt) {
            this.socket = skt;
        }

        @Override
        public Object call() throws IOException, ParserConfigurationException, SAXException {
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(packet);
                byte[] data = packet.getData();
                String dataString = new String(data, packet.getOffset(), packet.getLength());
                Scanner dataScanner = new Scanner(dataString);
                String location = "";
                boolean add = false;
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
    public List<Device> discoverDevices() throws IOException, InterruptedException {
        List<DatagramSocket> sockets = new ArrayList<>();
        Set<String> addresses = new HashSet<>();
        // get all site-local IPs to scan from
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface ni = ifaces.nextElement();
            Enumeration<InetAddress> addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress current = addrs.nextElement();
                if (current.isSiteLocalAddress()) {
                    addresses.add(current.getHostAddress());
                }
            }
        }

        devices = new ArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(addresses.size());
        Collection<ReceiveDevices> tasks = new ArrayList<>();

        for (String addr : addresses) {
            DatagramSocket socket = new DatagramSocket(null);
            InetSocketAddress address = new InetSocketAddress(addr, 0);
            socket.bind(address);

            byte[] request = ("M-SEARCH * HTTP/1.1\r\n" +
                    "HOST: 239.255.255.250:1900\r\n" +
                    "MAN: \"ssdp:discover\"\r\n" +
                    "MX: 5\r\n" +
                    "ST: urn:schemas-upnp-org:device:MediaRenderer:1\r\n" +
                    "CPFN.UPNP.ORG: PipeCast\r\n\r\n").getBytes();
            DatagramPacket requestDatagram = new DatagramPacket(request, request.length, Inet4Address.getByName("239.255.255.250"), 1900);
            socket.send(requestDatagram);
            tasks.add(new ReceiveDevices(socket));
            sockets.add(socket);
        }

        List<Future<Object>> futures = executor.invokeAll(tasks, 5, TimeUnit.SECONDS);

        executor.shutdown();

        for (DatagramSocket skt : sockets) {
            skt.close();
        }
        return devices;
    }
}
