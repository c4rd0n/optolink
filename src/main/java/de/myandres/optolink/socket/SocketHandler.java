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
package de.myandres.optolink.socket;

/*
 * Install a Socked Handler for ip communication
 *
 * Server can found via Broadcast
 * Server API Client can connect via TCP
 *
 */

import de.myandres.optolink.config.Config;
import de.myandres.optolink.viessmann.ViessmannHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketHandler {

	private static Logger log = LoggerFactory.getLogger(SocketHandler.class);

	private Config config;
	private ServerSocket server;
	private ViessmannHandler viessmannHandler;

	public SocketHandler(Config config, ViessmannHandler viessmannHandler) throws IOException {

		this.config = config;
		this.viessmannHandler = viessmannHandler;

		server = new ServerSocket(config.getPort());
	}

    public void start() {

        BroadcastListner broadcastListner = new BroadcastListner(config.getPort(), config.getAdapterID());

        // Put broadcast listner in background

        Thread broadcastListnerThread = new Thread(broadcastListner);
        broadcastListnerThread.setName("BcListner");
        broadcastListnerThread.start();

        // Wait connection

        while (true) {
            try {
                log.info("Listen on port {} for connection", config.getPort());
                Socket socket = server.accept();
                log.info("Connection on port {} accept. Remote host {}", config.getPort(),
                        socket.getRemoteSocketAddress());
				new SocketSession(socket, config, viessmannHandler).start();
			} catch (IOException e) {
				log.info("Connection on Socket {} rejected or closed by client", config.getPort());
			} catch (RuntimeException e) {
				log.error("Error on Socket {} : {}", config.getPort(), e.getMessage(), e);
				break;
			}
		}
	}

}
