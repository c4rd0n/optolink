package de.myandres.optolink.optointerface;

public interface OptolinkInterface extends AutoCloseable {

    void open() throws OptolinkException;

    @Override
    void close();

    void flush();

    void write(int data);

    int read();

    String getDeviceName();

}