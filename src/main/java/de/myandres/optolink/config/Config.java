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
package de.myandres.optolink.config;

import de.myandres.optolink.entity.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * Contains Data from xml-File 
 * This Data will be static only - dynamic data are stored in DataStore
 */


public class Config {

	private static Logger log = LoggerFactory.getLogger(Config.class);

	private String adapterID="TEST"; 
	private String tty;
	private String ttyIP;
	private Integer ttyPort;
	private String ttyType;
	private int ttyTimeOut = 2000;      //default
	private int port = 31113;           // default: unassigned Port. See: http://www.iana.org
	private String deviceType;
	private String protocol;
	private List<Thing> thingList;



	public Config(String fileName) {
		thingList = new ArrayList<>();
		// create XMLReader
		XMLReader xmlReader;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			log.error("XML reader can not be instanciated",e);
			throw new ConfigRuntimeException(e);
		}

		log.debug("Try to open File {}", fileName);
		// Pfad tho XML Datei
		FileReader reader;
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			log.error("Config File not found",e);
			throw new ConfigRuntimeException(e);
		}
		InputSource inputSource = new InputSource(reader);

		log.info("File {} open for parsing", fileName);
		

		// set ContentHandler
		xmlReader.setContentHandler(new ConfigXMLHandler(this));

		// start parser
		log.debug("Start parsing");
		try {
			xmlReader.parse(inputSource);
		} catch (IOException e) {
			log.error("Config File could not be readied",e);
			throw new ConfigRuntimeException(e);
		} catch (SAXException e) {
			log.error("Config File could not be parsed",e);
			throw new ConfigRuntimeException(e);
		}
		log.info("{} Things are parsed", thingList.size());
	}
	
	
	public List<Thing> getThingList() {
		return thingList;
	}

	public Optional<Thing> getThing(String id) {
		log.trace("get thing id: {}", id);
		for (Thing thing : thingList) {
			if(thing.getId().equals(id)) return Optional.of(thing);
		}
		log.error("Add thing id: {} not found", id);
		return Optional.empty();
	}
	

	public String getAdapterID() {
		return adapterID;
	}

	public String getTTY() {
		return tty;
	}

	public String getTTYType() {
		return ttyType;
	}

	public String getTTYIP() {
		return ttyIP;
	}

	public int getTTYPort() {
		return ttyPort;
	}

	public int getPort() {
		return port;
	}

	public int getTtyTimeOut() {
		return ttyTimeOut;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getProtocol() {
		return protocol;
	}

	void setDeviceType(String s){
		deviceType = s;
		log.info("Set deviceType: {}", deviceType);
	}

	void setProtocol(String s){
		protocol = s;
		log.info("Set protocol: {}", protocol);
	}

	void setAdapterID(String s) {
		adapterID = s;
		log.info("Set adapterID: {}", adapterID);
	}

	void setTTY(String s) {
		tty = s;
		log.info("Set tty: {}", tty);
	}

	void setTTYType(String s) {
		ttyType = s;
		log.info("Set ttyType: {}", ttyType);
	}

	void setTtyTimeOut(String s) {
		try {
			ttyTimeOut = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for TTY Timeout: {}", s);
		}
		log.info("Set TTY Timeout: {} Milliseconds", ttyTimeOut);
	}

	void setTTYIP (String s) {
		ttyIP = s;
		log.info("Set ttyIP: {}", ttyIP);
	}

	void setTTYPort(String s) {
		try {
			ttyPort = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for Port: {}", s);
		}
		log.info("Set TTY Port: {}", ttyPort);
	}

	void setPort(String s) {
		try {
			port = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			log.error("Wrong Format for Port: {}", s);
		}
		log.info("Set Socket Port: {}", port);
	}

}
