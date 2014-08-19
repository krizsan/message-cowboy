/*
 * This file is part of Message Cowboy.
 * Copyright 2014 Ivan A Krizsan. All Rights Reserved.
 * Message Cowboy is free software:
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package se.ivankrizsan.messagecowboy.domain.entities.impl;

import org.apache.camel.Exchange;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;

/**
 * Implementation of {@link MoverMessage} for Apache Camel implementation.
 * Please refer to {@link se.ivankrizsan.messagecowboy.services.transport.CamelTransportService}.
 * 
 * Implements the Message abstraction using a Camel {@link Exchange}.
 * 
 * @author Petter Nordlander
 *
 */
public class CamelMoverMessage implements MoverMessage<Exchange>{

	protected Exchange mExchange;
	
	public CamelMoverMessage(){
		
	}
	
	public CamelMoverMessage(Exchange inExchange){
		mExchange = inExchange;
	}
	
	@Override
	public Exchange getMessage() {
		return mExchange;
	}

	@Override
	public void setMessage(Exchange inMessage) {
		mExchange = inMessage;
	}

}
