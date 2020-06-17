package org.schabi.newpipe.cast.protocols.upnp;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class GenaServer extends NanoHTTPD {
    private UpnpDevice upnpDevice;

    public GenaServer(UpnpDevice upnpDevice) throws IOException {
        super(30303);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

        this.upnpDevice = upnpDevice;

        String request = "SUBSCRIBE " + upnpDevice.avTransportEventUrl.getPath() + " HTTP/1.1\r\n"
                + "HOST: " + upnpDevice.avTransportEventUrl.getHost() + ":" + upnpDevice.avTransportEventUrl.getPort() + "\r\n"
                + "CALLBACK: <http://192.168.1.16:30303>\r\n"
                + "NT: upnp:event\r\n"
                + "TIMEOUT: Second-infinite\r\n\r\n"; // TODO: implement timeout properly as infinite is deprecated, also implement UNSUBSCRIBE

        Socket socket = new Socket(upnpDevice.avTransportEventUrl.getHost(), upnpDevice.avTransportEventUrl.getPort());
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(request.getBytes());
        outputStream.close();
        socket.close();
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
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.getMessage() + "\n");
        }

        return Response.newFixedLengthResponse("\n");
    }
}
