package de.myandres.optolink.viessmann;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.optointerface.OptolinkInterface;

public class ViessmannHandlerFactory {

    private ViessmannHandlerFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static ViessmannHandler getViessmannHandler(Config config, OptolinkInterface optolinkInterface) {
        if (config.getTTYType().matches("URL")) {
            return new ViessmannHandlerURLImpl(config, optolinkInterface);
        } else {
            return new ViessmannHandlerImpl(config, optolinkInterface);
        }
    }

}
