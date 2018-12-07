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
import java.net.Socket;

public class OptolinkInterfaceURLImpl extends AbstractOptolinkInterfaceImpl {

    private static Logger log = LoggerFactory.getLogger(OptolinkInterfaceURLImpl.class);

    private Socket socket = null;

    OptolinkInterfaceURLImpl(Config config) throws OptolinkException {
        // constructor with implicit open
        super(config);
        open();
        close();
        log.debug("TTY type URL is present");
    }

    @Override
    public synchronized void close() {
        log.debug("Close TTY type URL {} ....", this.config.getTTY());
        if (socket != null) {
            try {
                socket.close();
                log.debug("TTY type URL {} closed", this.config.getTTY());
            } catch (IOException e) {
                log.debug("TTY type URL {} can't be closed", this.config.getTTY());
            }
        }
    }

    public synchronized void open() throws OptolinkException {
        log.debug("Open TTY type URL {}", this.config.getTTY());
        try {
            socket = new Socket(this.config.getTTYIP(), this.config.getTTYPort());
            socket.setSoTimeout(this.config.getTtyTimeOut());
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new OptolinkException(e.getMessage(), e);
        }
        log.debug("TTY type URL is open");
    }

    @Override
    public synchronized void flush() {
        // Flush input Buffer
        // We have to wait a certain time. It seems that input.available always has the count 0
        // right after connecting
        boolean timeout = true;
        try {
            while (input.available() == 0 && timeout) {
                wait(30); // 10 ms is too low. 30 ms chosen for a certain fail safe distance
                timeout = false;
            }
        } catch (IOException e) {
            log.error("Error while sleeping to wait for buffer flush", e);
        } catch (InterruptedException e) {
            log.error("Error while sleeping to wait for buffer flush", e);
            Thread.currentThread().interrupt();
        }
        super.flush();
    }
}
