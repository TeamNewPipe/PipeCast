package org.schabi.newpipe.cast.protocols.upnp;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.schabi.newpipe.cast.Stoppable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GenaServer implements Stoppable {
    private UpnpDevice upnpDevice;
    private HttpServer httpServer;

    private String sid;

    public GenaServer(UpnpDevice upnpDevice) throws IOException {
        this.upnpDevice = upnpDevice;

        httpServer = new HttpServer();

        String request = "SUBSCRIBE " + upnpDevice.avTransportEventUrl.getPath() + " HTTP/1.1\r\n"
                + "HOST: " + upnpDevice.avTransportEventUrl.getHost() + ":" + upnpDevice.avTransportEventUrl.getPort() + "\r\n"
                + "CALLBACK: <http://" + upnpDevice.inetAddress.getHostAddress() +":30303>\r\n"
                + "NT: upnp:event\r\n"
                + "TIMEOUT: Second-infinite\r\n\r\n"; // TODO: implement timeout properly as infinite is deprecated

        Socket socket = new Socket(upnpDevice.avTransportEventUrl.getHost(), upnpDevice.avTransportEventUrl.getPort());
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request.getBytes());
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = input.readLine()) != null) {
            response.append(line).append("\r\n");
        }
        outputStream.close();
        input.close();
        socket.close();

        Scanner dataScanner = new Scanner(response.toString());
        while (dataScanner.hasNextLine()) {
            line = dataScanner.nextLine();
            if (line.startsWith("SID: ")) {
                sid = line.substring(5);
                break;
            }
        }
        dataScanner.close();
    }

    @Override
    public void stop() throws IOException {
        String request = "UNSUBSCRIBE " + upnpDevice.avTransportEventUrl.getPath() + " HTTP/1.1\r\n"
                + "HOST: " + upnpDevice.avTransportEventUrl.getHost() + ":" + upnpDevice.avTransportEventUrl.getPort() + "\r\n"
                + "SID: " + sid + "\r\n\r\n";

        Socket socket = new Socket(upnpDevice.avTransportEventUrl.getHost(), upnpDevice.avTransportEventUrl.getPort());
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request.getBytes());
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = input.readLine()) != null) {
            response.append(line).append("\r\n");
        }
        outputStream.close();
        input.close();
        socket.close();

        httpServer.stop();
    }

    private class HttpServer extends NanoHTTPD {
        public HttpServer() throws IOException {
            super(30303);
            start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        }

        @Override
        public Response serve(IHTTPSession session) {
            try {
                String response = session.parseBody();

                InputSource inputSource = new InputSource(new StringReader(response));
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(inputSource);
                document.getDocumentElement().normalize();

                Node lastChange = ((Element) document.getDocumentElement().getElementsByTagName("e:property").item(0)).getElementsByTagName("LastChange").item(0);

                InputSource eventInputSource = new InputSource(new StringReader(lastChange.getTextContent()));
                DocumentBuilderFactory eventDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder eventDocumentBuilder = eventDocumentBuilderFactory.newDocumentBuilder();
                Document eventDocument = eventDocumentBuilder.parse(eventInputSource);
                eventDocument.getDocumentElement().normalize();

                NodeList nextAvTransportUri = ((Element) eventDocument.getDocumentElement().getElementsByTagName("InstanceID").item(0)).getElementsByTagName("NextAVTransportURI");
                if (nextAvTransportUri.getLength() == 1) {
                    if (((Element) nextAvTransportUri.item(0)).getAttribute("val").equals("")) {
                        upnpDevice.queue.remove(0);
                        if (!upnpDevice.queue.isEmpty()) {
                            upnpDevice.setNextAvTransportUri();
                        }
                    }
                }
            } catch (IOException | SAXException | ParserConfigurationException e) {
                return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.getMessage() + "\r\n");
            }

            return Response.newFixedLengthResponse("\r\n");
        }
    }
}
