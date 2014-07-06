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
package se.ivankrizsan.messagecowboy.services.transport;

import java.io.IOException;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.services.transport.exceptions.TransportException;

/**
 * Service that transports messages to endpoints using different kinds
 * of protocols; i.e. JMS, file, HTTP etc etc.
 *
 * @author Ivan Krizsan
 */
public interface TransportService {

    /**
     * Starts the service.<br/>
     * Perform any necessary initialization of the service that needs to be
     * done after the service has been configured but before the service
     * is taken into use.
     */
    abstract void start();

    /**
     * Stops the service.<br/>
     * Performs any necessary clean-up when the service is no longer to be used.
     */
    abstract void stop();

    /**
     * Dispatches the supplied message to the endpoint with the supplied URI.
     * The message dispatch is asynchronous, disregarding any response message
     * that the endpoint may return.
     *
     * @param inMessage Message to dispatch.
     * @param inEndpointURI URI of endpoint to which to dispatch message to.
     * @throws TransportException If an error occurred dispatching message.
     */
    @SuppressWarnings("rawtypes")
    abstract void dispatch(
        final MoverMessage inMessage, final String inEndpointURI)
        throws TransportException;

    /**
     * Receives a message from the endpoint with the supplied URI, timing
     * out after the supplied timeout-time.
     *
     * @param inEndpointURI URI of endpoint from which to receive message.
     * @return Received message, or null if receiving timed out.
     * @throws TransportException If an error occurred receiving message.
     */
    @SuppressWarnings("rawtypes")
    abstract MoverMessage receive(
        final String inEndpointURI, final long inTimeout)
        throws TransportException;

    /**
     * Refreshes the connectors-configuration for the service by, if necessary,
     * re-reading the connector definition resources.
     *
     * @throws IOException If error occurs accessing configuration resource.
     */
    abstract void refreshConnectors() throws IOException;
}
