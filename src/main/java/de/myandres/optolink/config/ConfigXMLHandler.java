package de.myandres.optolink.config;

import de.myandres.optolink.entity.Channel;
import de.myandres.optolink.entity.Telegram;
import de.myandres.optolink.entity.Thing;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;

public class ConfigXMLHandler implements ContentHandler {

    private Config config;
    private Thing thing = null;
    private Channel channel = null;
    private String path;
    private static final String IPADDRESS_PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5]):([0-9]{1,5})$";

    ConfigXMLHandler(Config config){
        this.config = config;
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String s = new String(ch, start, length);
        switch (path) {
            case "root.optolink.tty":
                if (s.matches(IPADDRESS_PATTERN)) { 	// device is at an URL
                    config.setTTYType ("URL");
                    config.setTTY(s);
                    String[] urlPort = s.split(":");
                    config.setTTYIP (urlPort[0]);
                    config.setTTYPort (urlPort[1]);
                } else {								// device is local
                    config.setTTYType ("GPIO");
                    config.setTTY(s);
                }
                break;
            case "root.optolink.ttytimeout":
                config.setTtyTimeOut(s);
                break;
            case "root.optolink.port":
                config.setPort(s);
                break;
            case "root.optolink.adapterID":
                config.setAdapterID(s);
                break;
            case "root.optolink.thing.description":
                thing.setDescription(s);
                break;
            case "root.optolink.thing.channel.description":
                channel.setDescription(s);
                break;
            default:break;
        }

    }

    @Override
    public void endDocument() {
        // Not use Auto-generated method stub
    }

    @Override
    public void endElement(String uri, String localName, String pName) {

        if (localName.equals("thing")) {
            config.getThingList().add(thing);
        }
        if (localName.equals("channel")) {
            thing.addChannel(channel);
        }
        path = path.substring(0, path.lastIndexOf('.'));
    }

    @Override
    public void startDocument() {
        path = "root";

    }

    @Override
    public void startElement(String uri, String localName, String pName,
                             Attributes attr) {
        path = path + "." + localName;
        switch (path) {
            case "root.optolink":
                config.setDeviceType(attr.getValue("device"));
                config.setProtocol(attr.getValue("protocol"));
                break;
            case "root.optolink.thing":
                thing = new Thing(attr.getValue("id"), attr.getValue("type"));
                break;
            case "root.optolink.thing.channel":
                channel = new Channel (attr.getValue("id"));
                break;
            case "root.optolink.thing.channel.telegram":
                channel.setTelegram(new Telegram(attr.getValue("address"),
                        attr.getValue("type"),
                        attr.getValue("divider")));
                break;
            default:break;
        }

    }

    @Override
    public void endPrefixMapping(String prefix) {
        // Not use Auto-generated method stub

    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
        // Not use  Auto-generated method stub

    }

    @Override
    public void processingInstruction(String target, String data) {
        // Not use  Auto-generated method stub

    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // Not use  Auto-generated method stub

    }

    @Override
    public void skippedEntity(String name) {
        // Not use  Auto-generated method stub

    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        // Not use  Auto-generated method stub

    }
}
