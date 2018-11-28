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
package de.myandres.optolink.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telegram {

	private static Logger log = LoggerFactory.getLogger(Telegram.class);
	
	// Types of Viessmann 
	public static final byte BOOLEAN =   1; // 1 Byte -> boolean
	public static final byte BYTE    =   2; // 1 Byte -> short
	public static final byte UBYTE   =   3; // 1 Byte -> short
	public static final byte SHORT  =    4; // 2 Byte -> int
	public static final byte USHORT =    5; // 2 Byte -> int
	public static final byte INT =       6; // 4 byte -> long
    public static final byte UINT =      7; // 4 Byte -> long
    public static final byte DATE =      8; // 8 Byte -> date
    public static final byte TIMER =     9; // 8 Byte -> timer
    public static final byte DUMP =      99; // Dump for unknown Telegram-Type

    
	private int address; 
	private byte type;
	private short length;
	private short divider; 

	
	public Telegram() {
    	address = 0;
		type = Telegram.DUMP;
		length = 0;
		divider = 1; 
	}
	
	public Telegram(String address, String type, String divider) {

    	setAddress(address);
		setType(type);
		setDivider(divider);
	}
	
	public Telegram(Telegram telegram) {

		this.address = telegram.address; 
		this.length = telegram.length;
		this.type = telegram.type;
		this.divider = telegram.divider; 
		
	}

 
	private void setAddress(String address) {
		log.trace("----------------------------------------");
		
		if (address==null) {
			log.error("Telegram Address not set") ;
			this.address=0;
		} else {
			try {
				this.address = Integer.parseInt(address,16);
			} catch (NumberFormatException e) {
				log.error("Invalid  Address format: {}", address);
				this.address=0;
			}
		} 
    	log.trace("Set Adress to {}({})", address, this.address);	
	}

	

	private void setType(String type) {
		if (type == null)
			log.error("Telegram Type not set");
		else {
			switch (type.toLowerCase()) {
			case "boolean":
				this.type = Telegram.BOOLEAN;
				length=1;
				break;
			case "byte":
				this.type = Telegram.BYTE;
				length=1;
				break;
			case "ubyte":
				this.type = Telegram.UBYTE;
				length=1;
				break;
			case "short":
				this.type = Telegram.SHORT;
				length=2;
				break;
			case "ushort":
				this.type = Telegram.USHORT;
				length=2;
				break;
			case "int":
				this.type = Telegram.INT;
				length=4;
				break;
			case "uint":
				this.type = Telegram.UINT;
				length=4;
				break;
			case "date":
				this.type = Telegram.DATE;
				length=8;
				break;
			case "timer":
				this.type = Telegram.TIMER;
				length=8;
				break;
			default:
				log.error("Unknown Type: {}", type);
				this.type = Telegram.DUMP;
				length=0;
			}
		}
		log.trace("Set Type to {}({}) length={}", type, this.type, length);
	}
	
	private void setDivider(String divider) {
		if (divider==null) {
			log.debug("divider not set - set to default: 1"); 
		    this.divider=1;
		} else {
		  try {
			this.divider=Short.parseShort(divider);
			} 
			catch (NumberFormatException e) {
				log.error("Invalid  divider format: {} - set to default: 1", divider);
				this.divider=1;
			}
		}
		log.trace("Set dividerider to {}", this.divider);
	}
	

	public int getAddress() {
		return address;
	}
	
	public int getLength() {
		return length;
	}

	public short getDivider() {
		return divider;
	}

	public byte getType() {
		return type;
	}

}
