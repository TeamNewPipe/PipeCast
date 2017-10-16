package org.schabi.newpipe.cast.protocols.upnp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.schabi.newpipe.cast.Device;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UpnpDevice extends Device {
    private Document description;
    private Element device;

    public UpnpDevice(String location) throws IOException {
        super(location);
        getDescription();
    }

    private void getDescription() throws IOException {
        try {
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
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }

    }

    @Override
    public String getName() {
        return device.getElementsByTagName("friendlyName").item(0).getTextContent();
    }
}
