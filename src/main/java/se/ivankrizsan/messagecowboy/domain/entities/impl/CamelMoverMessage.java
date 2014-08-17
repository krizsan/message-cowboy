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
