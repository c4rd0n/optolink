/*******************************************************************************
 * Copyright (c) 2015,  Stefan Andres.  All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *******************************************************************************/
package de.myandres.optolink.optointerface;

import de.myandres.optolink.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

public abstract class AbstractOptolinkInterfaceImpl implements OptolinkInterface {

    private static Logger log = LoggerFactory.getLogger(AbstractOptolinkInterfaceImpl.class);
    protected Config config;
    OutputStream output;
    InputStream input;

    AbstractOptolinkInterfaceImpl(Config config) {
        // constructor with implicit open
        this.config = config;
    }

    @Override
    public synchronized void flush() {
        // Flush input Buffer
        try {
            input.skip(input.available());
            log.debug("Input Buffer flushed");
        } catch (IOException e) {
            log.error("Can't flush TTY: {}", this.config.getTTY(), e);
        }
    }

    @Override
    public synchronized void write(int data) {
        if (log.isTraceEnabled())
            log.trace("TxD: {}", String.format("%02X", (byte) data));
        try {
            output.write((byte) data);
        } catch (IOException e) {
            log.error("Can't write Data to TTY {}", this.config.getTTY(), e);
        }
    }

    @Override
    public synchronized int read() {
        int data = -1;
        try {
            data = input.read();
            if (log.isTraceEnabled()) {
                log.trace("RxD: {}", String.format("%02X", data));
            }
            if (data == -1) log.trace("Timeout from TTY {}", this.config.getTTY());
            return data;
        } catch (SocketTimeoutException e) {
            log.trace("Timeout from TTY {}", this.config.getTTY());
            return data;
        } catch (Exception e) {
            log.error("Can't read Data from TTY {}", this.config.getTTY(), e);
        }
        return -1; // Ups

    }

    @Override
    public String getDeviceName() {
        return this.config.getDeviceType();
    }

}
