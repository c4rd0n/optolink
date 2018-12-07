package de.myandres.optolink.optointerface;

import de.myandres.optolink.config.Config;

public class OptolinkInterfaceFactory {
    private OptolinkInterfaceFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static OptolinkInterface getOptolinkInterface(Config config) throws OptolinkException {
        if (config.getTTYType().matches("URL")) {
            return new OptolinkInterfaceURLImpl(config);
        } else {
            return new OptolinkInterfaceImpl(config);
        }
    }
}
