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

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.impl.MuleMoverMessage;
import se.ivankrizsan.messagecowboy.services.transport.exceptions.TransportException;

/**
 * Transport service implementation that uses Mule to perform requests to
 * endpoints using different kinds of transports.
 * <br/><br/>
 * External Mule configurations may be of two different kinds:<br/>
 * The first kind is a Mule transport connector. Each connector must have a
 * name that is unique among the connectors used by the transport service.
 * The name of the connector is used in one or more endpoint URIs.
 * <br/><br/>
 * The second kind is a Mule flow. Such a flow handles a more complex scenario
 * which may involve delivering a message in a transaction, re-delivery of
 * failed messages etc.<br/>
 * Methods accessing the Mule client instance variable are synchronized in
 * order to prevent any changes to the Mule client while dispatching or
 * receiving a message and vice versa.
 *
 * @author Ivan Krizsan
 */
@Service
class MuleTransportService extends AbstractXmlConfigurerdTransportService {
    /* Constant(s): */
    /** Class logger. */
    static final Logger LOGGER = LoggerFactory
        .getLogger(MuleTransportService.class);

    /* Instance variable(s): */
    /** Mule client used to transfer messages. */
    protected MuleClient mMuleClient;
    @SuppressWarnings("rawtypes")
    @Override
    public synchronized void dispatch(final MoverMessage inMessage,
        final String inEndpointURI) throws TransportException {
        try {
            @SuppressWarnings("unchecked")
            final MoverMessage<MuleMessage> theMuleMoverMessage = inMessage;
            mMuleClient.dispatch(inEndpointURI, theMuleMoverMessage
                .getMessage());

            LOGGER.debug("Sent message: {}", inMessage);
        } catch (MuleException theException) {
            throw new TransportException("Error occurred sending message",
                theException);
        }
    }

    @Override
    public synchronized MoverMessage<MuleMessage> receive(
        final String inEndpointURI, final long inTimeout)
        throws TransportException {
        MuleMessage theReceivedMsg = null;
        MoverMessage<MuleMessage> theMoverMessage = null;
        try {
            theReceivedMsg = mMuleClient.request(inEndpointURI, inTimeout);

            LOGGER.debug("Received message: {}", theReceivedMsg);
        } catch (MuleException theException) {
            throw new TransportException("Error occurred receiving message",
                theException);
        }

        if (theReceivedMsg != null) {
            theMoverMessage = new MuleMoverMessage(theReceivedMsg);
        }
        return theMoverMessage;
    }

    /**
     * Starts the transport service.
     */
    @Override
    public void start() {
        LOGGER.info("Mule transport service starting...");
        try {
            refreshConnectors();
        } catch (final Exception theException) {
            final String theErrorMsg = "Error creating Mule client";
            LOGGER.error(theErrorMsg, theException);
            throw new Error(theErrorMsg, theException);
        }
        LOGGER.info("Mule transport service started.");
    }

    @Override
    public synchronized void stop() {
        LOGGER.info("Mule transport service stopping...");
        mMuleClient.dispose();
        LOGGER.info("Mule transport service stopped.");
    }

    @Override
    public synchronized void refreshConnectors() throws IOException {
        final boolean theConfigRsrcChangedFlag =
            hasConfigurationResourceBeenModified();

        if (theConfigRsrcChangedFlag) {
            LOGGER
                .debug("Detected change in configuration resources, refreshing");

            if (mMuleClient != null) {
                mMuleClient.dispose();
            }

            try {
                final String theMuleConfigResourcesString =
                    buildMuleConfigResourcesString();

                mMuleClient = new MuleClient(theMuleConfigResourcesString);
                mMuleClient.getMuleContext().start();

                LOGGER.debug("Mule client created and started");
            } catch (final Exception theException) {
                final String theErrorMsg = "Error creating Mule client";
                LOGGER.error(theErrorMsg, theException);
                throw new Error(theErrorMsg, theException);
            }
        } else {
            LOGGER
                .debug("No changes in configuration resources, skips refresh");
        }
    }

    /**
     * Builds a string containing the Mule configuration resources the transport
     * service is to be configured with.
     *
     * @return Configuration resources string, or empty string if no configuration
     * resources.
     * @throws IOException If error occurs discovering configuration resource.
     */
    protected String buildMuleConfigResourcesString() throws IOException {
        final StringBuffer theMuleConfigResource = new StringBuffer();
        final PathMatchingResourcePatternResolver theConnectorsResolver =
            new PathMatchingResourcePatternResolver();

        for (String theConfigRsrcsLocationPattern : mConfigResourcesLocationPatterns) {
            final Resource[] theConnectorsConfigurations =
                theConnectorsResolver
                    .getResources(theConfigRsrcsLocationPattern);

            LOGGER.debug(
                "Found {} connector configuration files using the pattern {}",
                theConnectorsConfigurations.length,
                theConfigRsrcsLocationPattern);

            if (theConnectorsConfigurations.length > 0) {
                for (Resource theResource : theConnectorsConfigurations) {
                    /* Only comma-separate if there already is an entry. */
                    if (theMuleConfigResource.length() > 0) {
                        theMuleConfigResource.append(",");
                    }
                    theMuleConfigResource.append(theResource.getURL());
                }
            }
        }
        return theMuleConfigResource.toString();
    }
    
    /**
     * Retrieves the Mule context.<br/>
     * For testing purposes only.
     *
     * @return Mule context of the started Mule transport service, or null
     * if no context is available.
     */
    synchronized MuleContext getMuleContext() {
        MuleContext theMuleContext = null;

        if (mMuleClient != null) {
            theMuleContext = mMuleClient.getMuleContext();
        }
        return theMuleContext;
    }
}
