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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import de.myandres.optolink.Config;
import de.myandres.optolink.viessmann.ViessmannHandler;
import de.myandres.optolink.viessmann.ViessmannSemaphore;
import de.myandres.optolink.entity.Channel;
import de.myandres.optolink.entity.Telegram;
import de.myandres.optolink.entity.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketHandler {

	private static Logger log = LoggerFactory.getLogger(SocketHandler.class);

	private static final String DATA_OPEN = "<data>";
	private static final String DATA_CLOSE = "</data>";
	private static final String THING_OPEN = "  <thing id=\"";
	private static final String THING_CLOSE = "  </thing>";


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
                new ServerThread(socket).start();
            }

			catch (Exception e) {
				log.info("Connection on Socket {} rejected or closed by client", config.getPort());
			}
		}
	}

	private void exec(String command, String param1, String param2, PrintStream out) {

		try {
			ViessmannSemaphore.getInstance().acquire();
		} catch (InterruptedException e) {
			log.error("Viessmann is busy");
			Thread.currentThread().interrupt();
		}
		if (log.isTraceEnabled()) {
			log.trace("Queue Command: |{}|", command);
			log.trace("      param1 : |{}|", param1);
			log.trace("      param2 : |{}|", param2);
		}

		switch (command.toLowerCase()) {

			case "list":
				list(out);
				break;
			case "get":
				if (param2.equals(""))
					getThing(param1, out);
				else
					getThing(param1, param2, out);
				break;
			case "set":
				set(param1, param2, out);
				break;
			default:
				log.error("Unknown Client Command: {}", command);

				log.trace("Queue Command: |{}| done", command);

		}
		ViessmannSemaphore.getInstance().release();

	}

	private void set(String id, String value, PrintStream out) {
		// Format id = <thing>:<channel>

		String[] ids = id.trim().split(":");

		if (ids.length != 2) {
			log.error("Wrong format '{}' of id", id);
			return;
		}
		Telegram telegram = config.getThing(ids[0]).getChannel(ids[1]).getTelegram();
		if (telegram != null) {
			out.println(DATA_OPEN);
			out.println(THING_OPEN + ids[0] + "\">");
			printChannel(ids[1], viessmannHandler.setValue(telegram, value.toUpperCase()), out);
			out.println(THING_CLOSE);
			out.println(DATA_CLOSE);
		}

	}

	private void getThing(String id, PrintStream out) {
		log.debug("Try to get Thing for ID: {}", id);
		Thing thing = config.getThing(id);
		if (thing != null) {
			out.println(DATA_OPEN);
			out.println(THING_OPEN + thing.getId() + "\">");
			for (Channel channel : thing.getChannelMap()) {
				if (!channel.getId().startsWith("*")) {
					printChannel(channel.getId(), viessmannHandler.getValue(channel.getTelegram()), out);
				}
			}
			out.println(THING_CLOSE);
			out.println(DATA_CLOSE);
		}
	}

	private void getThing(String id, String channels, PrintStream out) {
		Channel channel;
		log.debug("Try to get Thing for ID: {} channels: {}", id, channels);
		String[] channelList = channels.split(",");
		Thing thing = config.getThing(id);
		if (thing != null) {
			out.println(DATA_OPEN);
			out.println(THING_OPEN + thing.getId() + "\">");
			for (String channelName : channelList) {
				channel = thing.getChannel(channelName);
				if (channel != null) {
					printChannel(channel.getId(), viessmannHandler.getValue(channel.getTelegram()), out);
				} else {
					log.error("Channel : {}.{} not define! ", id, channelName);
				}
			}
			out.println(THING_CLOSE);
			out.println(DATA_CLOSE);
		}
	}

	private void list(PrintStream out) {
		log.debug("List Things for ID");
		out.println("<define>");
		for (Thing thing : config.getThingList()) {

			if ((thing != null) && !thing.getId().startsWith("*")) {

				out.println(THING_OPEN + thing.getId() + "\" type=\"" + thing.getType() + "\">");
				for (Channel channel : thing.getChannelMap()) {
					if (!channel.getId().startsWith("*")) {
						printChannel(channel.getId(), out);
					}
				}
				out.println(THING_CLOSE);

			}
		}
		out.println("</define>");

	}

	private void printChannel(String id, PrintStream out) {
		printChannel(id, null, out);
	}

	private void printChannel(String id, String value, PrintStream out) {
		out.print("    <channel id=\"" + id + "\"");
		if (value != null) {
			out.print(" value=\"" + value + "\"");
		}
		out.println("/>");
	}

	public class ServerThread extends Thread {
		private Socket socket;

		ServerThread(Socket socket) {
			super("ServerThread");
			this.socket = socket;
		}

		@Override
		public void run() {
			try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				PrintStream out = new PrintStream(socket.getOutputStream());
				out.println("<!-- #Helo from viessmann -->");
				out.println("<optolink>");

				String inStr;

				while ((inStr = in.readLine()) != null) {
					if (inStr.toLowerCase().startsWith("exit"))
						break;
					new Thread(new CommandExec(inStr, out)).start();
				}
				out.println("<!-- #Bye from viessmann -->");
				socket.close();
			} catch (IOException e) {
				log.info("Connection on Socket {} rejected or closed by client", config.getPort());
			}
		}

		// Start a thread for each call command: No blocking of caller

		public class CommandExec implements Runnable {

			String param;
			PrintStream out;

			CommandExec(String param, PrintStream out) {
				this.param = param;
				this.out = out;
			}

			public void run() {
				String command;
				String param1;
				String param2;

				log.debug("Execute Thread for: '{}'", param);
				String[] inStr = param.trim().split(" +");
				command = inStr[0];
				if (inStr.length > 1)
					param1 = inStr[1];
				else
					param1 = "";
				if (inStr.length > 2)
					param2 = inStr[2];
				else
					param2 = "";
				exec(command, param1, param2, out);
				log.debug("Thread for: '{}' done", param);
			}
		}
	}
}
