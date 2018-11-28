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
package de.myandres.optolink.viessmann;

import de.myandres.optolink.config.Config;
import de.myandres.optolink.entity.Telegram;
import de.myandres.optolink.optointerface.OptolinkException;
import de.myandres.optolink.optointerface.OptolinkInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViessmannHandler {
	private static Logger log = LoggerFactory.getLogger(ViessmannHandler.class);
	private ViessmannProtocol viessmannProtocol;
	private OptolinkInterface optolinkInterface;
	private Config config;

	public ViessmannHandler(Config config, OptolinkInterface optolinkInterface) {

		log.debug("Init Handler for Protokoll {} ...", optolinkInterface);
		this.config = config;
		this.optolinkInterface = optolinkInterface;
		String interfaceProtocol = config.getProtocol();
		switch (interfaceProtocol) {
		case "300":
			viessmannProtocol = new Viessmann300(optolinkInterface);
			break;
		case "KW":
			viessmannProtocol = new ViessmannKW(optolinkInterface);
			break;
		default:
			log.error("Unknown Protokol: {}", interfaceProtocol);
			throw new ViessmannHandlerRuntimeException();
		}
		log.trace("Handler for Class {} initalisiert", viessmannProtocol.getClass().getName());

		log.info("Handler for Protocol {} initalisiert", interfaceProtocol);
	}

	public synchronized void close() {
		viessmannProtocol.close();
	}


	public synchronized Optional<String> setValue(Telegram telegram, String value) {
		byte [] buffer = new byte[16];
		int locValue;

		switch (telegram.getType()) {
		case Telegram.BOOLEAN:
			locValue = "ON".equals(value) ? 1 : 0;
			break;

		case Telegram.DATE:
			log.error("Update of Date not implemented");
			return Optional.empty()	;
		case Telegram.TIMER: // Receiving a string of format: "On:--:--Off:--:--On:--:--Off:--:--On:--:--Off:--:--On:--:-- Off:--:--"
			String[] switchTimes = new String[8];
			String[] timeParts;
			int hr;
			int min;
			int switchTimesLength = 0;
			Pattern pattern = Pattern.compile("(\\d{1,2}:\\d{2})");
			Matcher matcher = pattern.matcher(value);

			while (matcher.find()) {
				switchTimes[switchTimesLength++] = matcher.group(1);
			}

			if ((switchTimesLength % 2) == 0) {
				for (int i = 0; i<switchTimesLength; i++) {
					timeParts = switchTimes[i].split(":");
					hr = Integer.parseInt(timeParts[0]);
					min = Integer.parseInt(timeParts[1]);
					if (hr > 23 || hr < 0 || min > 59 || min < 0) {
						log.error("Invalid time. Hour {} has to between 0 and 23 and Minute {} between 0 and 59", hr,min);
						return Optional.empty();
					}
					hr = hr << 3;
					min = min/10;
					buffer[i] = (byte) (hr | min);
				}
				for (int i = switchTimesLength; i < 8; i++) {
					buffer[i] = (byte) 0xff;
				}
				for (int i = 0; i < 8; i+=2) {
					if ((buffer[i] & 0xff) > (buffer[i+1] & 0xff)) {
						if(log.isErrorEnabled())
							log.error(String.format("Invalid time pair. On time %02x if bigger than Off time %02x", buffer[i],buffer[i+1]));
						return Optional.empty();
					}
				}
			} else {
				log.error("Error! SwitchTime has to be in on/off pairs");
				return Optional.empty();
			}
			locValue = 9;
			break;
		default : float fl = (new Float(value)) * telegram.getDivider(); // all other writable channels are byte or ubyte
			locValue = (byte) fl;
			break;
		}
		if (this.config.getTTYType().matches("URL")) {
			try {
				optolinkInterface.open();
			} catch (OptolinkException e) {
				log.error("Opening TTY type URL failed");
				optolinkInterface.close();
				return Optional.empty();
			}
		}
		int resultLength = viessmannProtocol.setData(buffer, telegram.getAddress() , telegram.getLength(), locValue);
		if (this.config.getTTYType().matches("URL")) {
			optolinkInterface.close();
		}

		if (resultLength == 0) return Optional.empty();
        else return Optional.of(formatValue(buffer, telegram.getType(), telegram.getDivider()));

	}



	public synchronized Optional<String> getValue(Telegram telegram)  {
		byte [] buffer = new byte[16];

		if (this.config.getTTYType().matches("URL")) {
			try {
				optolinkInterface.open();
			} catch (Exception e) {
				log.error("Opening TTY type URL failed");
				optolinkInterface.close();
				return Optional.empty();
			}
		}
		int resultLength=viessmannProtocol.getData(buffer,telegram.getAddress(), telegram.getLength());
		if (log.isTraceEnabled()) {
	    	log.trace("Number of Bytes: {}", resultLength);
	    	for (int i=0; i<resultLength; i++) log.trace("[{}] {} ",i,buffer[i]);
		}
		if (this.config.getTTYType().matches("URL")) {
			optolinkInterface.close();
		}
		return Optional.of(formatValue(buffer, telegram.getType(), telegram.getDivider()));

	}

	private String formatValue(byte[] buffer, byte type, short divider) {

		log.trace("Formating....");
		long result;
		StringBuilder timer = new StringBuilder();
        switch (type) {
            case Telegram.BOOLEAN:
                if (buffer[0] == 0) return "OFF";
                return "ON";
            case Telegram.TIMER:
                for (int i=0; i<8; i+=2) {
                    if (buffer[i] == -1) { // -1 equals 0xFF
                        timer.append("On:--:--Off:--:--");
                    } else {
                        timer.append(String.format("On:%02d:%02dOff:%02d:%02d",
                                (buffer[i] & 0xF8)>>3,(buffer[i] & 7)*10,	(buffer[i+1] & 0xF8)>>3,(buffer[i+1] & 7)*10));
                    }
                }
                return timer.toString();
            case Telegram.DATE:
                return String.format("%02x%02x-%02x-%02xT%02x:%02x:%02x",
                        buffer[0],buffer[1],buffer[2],buffer[3],buffer[5],buffer[6],buffer[7])	;
            case Telegram.BYTE:
                result = buffer[0];
                break;
            case Telegram.UBYTE:
                result = 0xFF & buffer[0];
                break;
            case Telegram.SHORT:
                result = ((long)(buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
                break;
            case Telegram.USHORT:
                result = ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
                break;
            case Telegram.INT:
                result = ((long)(buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
                break;
            case Telegram.UINT:
                result = ((long)(0xFF & buffer[3]))*0x1000000  + ((long)(0xFF & buffer[2]))*0x10000  + ((long)(0xFF & buffer[1]))*0x100  + (long)(0xFF & buffer[0]);
                break;
            default:
                result = buffer[0];
                break;
        }
        if (divider !=1 )
            return String.format(Locale.US,"%.2f", (float)result / divider);
        else return String.format("%d", result);
    }
}
