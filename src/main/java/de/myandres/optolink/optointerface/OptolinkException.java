package de.myandres.optolink.optointerface;

public class OptolinkException extends Exception {
    public OptolinkException(Throwable t) {
        super(t);
    }

    public OptolinkException(String msg) {
        super(msg);
    }

    public OptolinkException(String msg, Throwable t) {
        super(msg, t);
    }
}
