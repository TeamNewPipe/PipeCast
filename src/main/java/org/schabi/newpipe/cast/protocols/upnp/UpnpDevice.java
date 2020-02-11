package org.schabi.newpipe.cast.protocols.upnp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.schabi.newpipe.cast.Device;
import org.schabi.newpipe.cast.ItemClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UpnpDevice extends Device {
    private Document description;
    private Element device;
    private URL controlUrl;

    public UpnpDevice(String location) throws IOException, ParserConfigurationException, SAXException {
        super(location);
        getDescription();
    }

    private void getDescription() throws IOException, ParserConfigurationException, SAXException {
        URL url = new URL(location);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = input.readLine()) != null) {
            response.append(line);
        }
        input.close();

        InputSource inputSource = new InputSource(new StringReader(response.toString()));
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        description = documentBuilder.parse(inputSource);
        description.getDocumentElement().normalize();
        device = (Element) description.getDocumentElement().getElementsByTagName("device").item(0);

        URL baseUrl = new URL(description.getDocumentElement().getElementsByTagName("URLBase").item(0).getTextContent());

        Element serviceList = (Element) device.getElementsByTagName("serviceList").item(0);
        NodeList services = serviceList.getElementsByTagName("service");
        int servicesLength = services.getLength();
        for (int i = 0; i < servicesLength; i++) {
            Element service = (Element) services.item(i);
            if (service.getElementsByTagName("serviceType").item(0).getTextContent().equals("urn:schemas-upnp-org:service:AVTransport:1")) {
                String serviceUrl = service.getElementsByTagName("controlURL").item(0).getTextContent();
                controlUrl = new URL(baseUrl, serviceUrl);
            }
        }
    }

    @Override
    public String getName() {
        return device.getElementsByTagName("friendlyName").item(0).getTextContent();
    }

    private void play() throws IOException, XMLStreamException {
        HttpURLConnection connection = (HttpURLConnection) controlUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
        connection.setRequestProperty("Soapaction", "\"urn:schemas-upnp-org:service:AVTransport:1#Play\"");
        OutputStream outputStream = connection.getOutputStream();

        StringWriter sw = new StringWriter();
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlof.createXMLStreamWriter(sw);

        writer.writeStartDocument("utf-8", "1.0");
        writer.writeStartElement("s:Envelope");
        writer.writeAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
        writer.writeNamespace("s", "http://schemas.xmlsoap.org/soap/envelope/");
        writer.writeStartElement("s:Body");
        writer.writeStartElement("u:Play");
        writer.writeNamespace("u", "urn:schemas-upnp-org:service:AVTransport:1");
        writer.writeStartElement("InstanceID");
        writer.writeCharacters("0");
        writer.writeEndElement();
        writer.writeStartElement("Speed");
        writer.writeCharacters("1");
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        byte[] xml = sw.toString().getBytes();
        outputStream.write(xml);
        outputStream.close();
        connection.getInputStream();
    }

    @Override
    public void play(String url, String title, String creator, String mimeType, ItemClass itemClass) throws IOException, XMLStreamException {
        HttpURLConnection connection = (HttpURLConnection) controlUrl.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml;charset=utf-8");
        connection.setRequestProperty("Soapaction", "\"urn:schemas-upnp-org:service:AVTransport:1#SetAVTransportURI\"");
        connection.setDoOutput(true);
        OutputStream outputStream = connection.getOutputStream();

        StringWriter didlSw = new StringWriter();
        XMLOutputFactory didlXmlof = XMLOutputFactory.newInstance();
        XMLStreamWriter didlWriter = didlXmlof.createXMLStreamWriter(didlSw);
        didlWriter.writeStartElement("DIDL-Lite");
        didlWriter.writeNamespace("", "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/");
        didlWriter.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
        didlWriter.writeNamespace("dlna", "urn:schemas-dlna-org:metadata-1-0/");
        didlWriter.writeNamespace("pv", "http://www.pv.com/pvns/");
        didlWriter.writeNamespace("sec", "http://www.sec.co.kr/");
        didlWriter.writeNamespace("upnp", "urn:schemas-upnp-org:metadata-1-0/upnp/");
        didlWriter.writeStartElement("item");
        didlWriter.writeAttribute("id", "/test/123");
        didlWriter.writeAttribute("parentID", "/test");
        didlWriter.writeAttribute("restricted", "1");
        didlWriter.writeStartElement("upnp:class");
        didlWriter.writeCharacters(itemClass.upnpClass);
        didlWriter.writeEndElement();
        didlWriter.writeStartElement("dc:title");
        didlWriter.writeCharacters(title);
        didlWriter.writeEndElement();
        didlWriter.writeStartElement("dc:creator");
        didlWriter.writeCharacters(creator);
        didlWriter.writeEndElement();
        didlWriter.writeStartElement("res");
        didlWriter.writeAttribute("protocolInfo", "http-get:*:" + mimeType + ":*"); // TODO: add DLNA-specific stuff
        didlWriter.writeCharacters(url);
        didlWriter.writeEndElement();
        didlWriter.writeEndElement();
        didlWriter.writeEndElement();
        didlWriter.writeEndDocument();
        didlWriter.close();

        StringWriter sw = new StringWriter();
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = xmlof.createXMLStreamWriter(sw);
        writer.writeStartDocument("utf-8", "1.0");
        writer.writeStartElement("s:Envelope");
        writer.writeAttribute("s:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/");
        writer.writeNamespace("s", "http://schemas.xmlsoap.org/soap/envelope/");
        writer.writeStartElement("s:Body");
        writer.writeStartElement("u:SetAVTransportURI");
        writer.writeNamespace("u", "urn:schemas-upnp-org:service:AVTransport:1");
        writer.writeStartElement("InstanceID");
        writer.writeCharacters("0");
        writer.writeEndElement();
        writer.writeStartElement("CurrentURI");
        writer.writeCharacters(url);
        writer.writeEndElement();
        writer.writeStartElement("CurrentURIMetaData");
        writer.writeCharacters(didlSw.toString());
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        byte[] xml = sw.toString().getBytes();
        outputStream.write(xml);
        outputStream.close();
        connection.getInputStream();

        play();
    }
}
