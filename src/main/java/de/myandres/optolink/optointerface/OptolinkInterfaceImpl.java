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
import gnu.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OptolinkInterfaceImpl extends AbstractOptolinkInterfaceImpl {

    private static Logger log = LoggerFactory.getLogger(OptolinkInterfaceImpl.class);

    private CommPort commPort;

    OptolinkInterfaceImpl(Config config) throws OptolinkException {

        // constructor with implicit open
        super(config);
        log.debug("Open TTY {} ...", this.config.getTTY());
        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(this.config.getTTY());
        } catch (NoSuchPortException e) {
            throw new OptolinkException(e);
        }

        if (portIdentifier.isCurrentlyOwned()) {
            String error = String.format("TTY %s in use.", this.config.getTTY());
            log.error(error);
            throw new OptolinkException(error);
        }
        try {
            commPort = portIdentifier.open(this.getClass().getName(), this.config.getTtyTimeOut());
            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(4800, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_2, SerialPort.PARITY_EVEN);

                input = serialPort.getInputStream();
                output = serialPort.getOutputStream();
                commPort.enableReceiveTimeout(this.config.getTtyTimeOut()); // Reading Time-Out
            }
        } catch (PortInUseException | UnsupportedCommOperationException | IOException e) {
            log.error(e.getMessage());
            throw new OptolinkException(e.getMessage(), e);
        }
        log.debug("TTY {} opened", this.config.getTTY());
    }

    @Override
    public synchronized void close() {
        log.debug("Close TTY {} ....", this.config.getTTY());
        commPort.close();
        log.debug("TTY {} closed", this.config.getTTY());
    }

    @Override
    public void open() {
        throw new UnsupportedOperationException("Unsuported Operation, the oppenning is realized in constructor");
    }
}
