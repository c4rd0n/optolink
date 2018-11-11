package de.myandres.optolink.config;

import org.xml.sax.SAXException;

class ConfigRuntimeException extends RuntimeException {
    public ConfigRuntimeException(Exception e) {
        super(e);
    }
}
