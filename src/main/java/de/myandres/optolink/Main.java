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
package de.myandres.optolink;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.optointerface.OptolinkInterface;
import de.myandres.optolink.optointerface.OptolinkInterfaceFactory;
import de.myandres.optolink.socket.SocketHandler;
import de.myandres.optolink.viessmann.ViessmannHandler;
import de.myandres.optolink.viessmann.ViessmannHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static Logger log = LoggerFactory.getLogger(Main.class);

	// Central Classes, singular only!!
	private static Config config;

	public static void main(String[] args) {
		log.info("Programm gestartet");

		try {
			config = new Config("conf/optolink.xml");
		} catch (Exception e) {
			log.error("Something is wrong not init", e);
			System.exit(1);
		}

		try (
				OptolinkInterface optolinkInterface = OptolinkInterfaceFactory.getOptolinkInterface(config);
				ViessmannHandler viessmannHandler = ViessmannHandlerFactory.getViessmannHandler(config, optolinkInterface)
		) {
			// Install catcher for Kill Signal
			Runtime.getRuntime().addShutdownHook(
					new Thread(() -> {
						try {
							viessmannHandler.close();
						} catch (Exception e) {
							log.error("Closure ViessmannHandler failed", e);
						}
						try {
							optolinkInterface.close();
						} catch (Exception e) {
							log.error("Closure OptolinkInterface failed", e);
						}
						log.info("Programm normal terminated by Signal (Kill)");
					}));

			// Start SocketHandler
			SocketHandler socketHandler = new SocketHandler(config, viessmannHandler);
			socketHandler.start();

		} catch (Exception e) {
			log.error("Programm abnormal terminated.", e);
		}

	}

}
