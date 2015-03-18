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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.StartupListener;
import org.apache.camel.spring.SpringCamelContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Service;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.impl.CamelMoverMessage;
import se.ivankrizsan.messagecowboy.services.transport.exceptions.TransportException;

/**
 * Transport service implementation that uses Apache Camel to perform requests
 * to endpoints using different kinds of transports/components.
 * <br/><br/>
 *
 * The configuration files are plain spring xml files.
 *
 * You may configure any component required. The id of the component will
 * be the transport prefix to be used in the endpoint URIs.
 *
 * example:
 * <pre>{@code
 * <bean id="jms" class="org.apache.activemq.camel.component.ActiveMQComponent">
 *	  <property name="brokerURL" value="tcp://localhost:61616"/>
 * </bean>
 * }</pre>
 *
 * You can also fire up Camel routes to do complex routing, filtering and
 * even transformation tasks.
 *
 * If you have a lot of routes, you may want to make camel the default
 * namespace prefix. If you intend to communicate from the job tasks, you
 * can use direct-vm or vm components communicating between camelContexts.
 *
 * <pre>{@code
 * <camel:camelContext errorHandlerRef="errorHandler"
 *						xmlns="http://camel.apache.org/schema/spring">
 *     <camel:route id="myFilteringRoute>
 *         <camel:from uri="vm:inputEndpoint"/>
 *         <camel:filter>
 *             <camel:xpath>$foo = 'bar'</xpath>
 *             <camel:to uri="file:/foo/bar"/>
 *         </camel:filter>
 *     </camel:route>
 * </camel:camelContext>
 * }</pre>
 *
 * Methods accessing the {@link ProducerTemplate} and {@link ConsumerTemplate}
 * instance variables are synchronized in order to prevent any changes to
 * the camel context while dispatching and receiving.
 *
 * @author Petter Nordlander
 */
@Service
public class CamelTransportService extends AbstractXmlConfigurerdTransportService {

    private final static Logger LOGGER = LoggerFactory.getLogger(CamelTransportService.class);

    protected SpringCamelContext mCamelContext;
    protected ProducerTemplate mProducerTemplate;
    protected ConsumerTemplate mConsumerTemplate;

    protected FileSystemXmlApplicationContext mCamelSpringContext;

    @Override
    public synchronized void start() {
        LOGGER.info("Starting Camel transport service");
        try {
            refreshConnectors();
        } catch (final Exception theException) {
            final String theErrorMsg = "Error creating Mule client";
            LOGGER.error(theErrorMsg, theException);
            throw new Error(theErrorMsg, theException);
        }
        LOGGER.info("Camel transport service started.");
    }

    @Override
    public synchronized void stop() {
        try {
            mCamelContext.stop();
        } catch (Exception e) {
            LOGGER.error("Cannot stop Camel", e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized void dispatch(final MoverMessage inMessage, final String inEndpointURI)
        throws TransportException {
        try {
            @SuppressWarnings("unchecked")
            final MoverMessage<Exchange> theCamelMoverMessage = inMessage;
            // TODO if endpoint is JMS, use spring JMS tx manager
            mProducerTemplate.send(inEndpointURI, theCamelMoverMessage.getMessage());
            LOGGER.debug("Sent message: {}", inMessage);
        } catch (Exception e) {
            throw new TransportException("Error occurred sending message", e);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public synchronized MoverMessage receive(final String inEndpointURI, final long inTimeout)
        throws TransportException {

        Exchange theReceivedExchange = null;
        MoverMessage<Exchange> theMoverMessage = null;
        try {
            // TODO if endpoint is JMS, use spring JMS Tx manager .
            theReceivedExchange = mConsumerTemplate.receive(inEndpointURI, inTimeout);
            LOGGER.debug("Received message: {}", theReceivedExchange);
        } catch (Exception e) {
            throw new TransportException("Error occurred receiving message", e);
        }

        if (theReceivedExchange != null) {
            theMoverMessage = new CamelMoverMessage(theReceivedExchange);
        }
        return theMoverMessage;
    }

    @Override
    public synchronized void refreshConnectors() throws IOException {

        final boolean theConfigRsrcChangedFlag = hasConfigurationResourceBeenModified();

        if (theConfigRsrcChangedFlag) {
            LOGGER.debug("Refreshing Camel configuration");
            try {
                // Tear down previous Camel and Spring context in order.
                killCamelInstance();
            } catch (Exception e) {
                LOGGER.warn("Failed to stop Camel context to refresh configuration.", e);
            }

            // Create a new Spring context for Camel to use.
            LOGGER.debug("Creating a Camel Spring Context with the following files {}",
                StringUtils.join(mConfigResourcesLocationPatterns, ","));
            mCamelSpringContext =
                new FileSystemXmlApplicationContext(
                    mConfigResourcesLocationPatterns.toArray(new String[mConfigResourcesLocationPatterns.size()]));
            mCamelSpringContext.start();
            mCamelSpringContext.registerShutdownHook(); // close with JVM.
            mCamelContext = new SpringCamelContext(mCamelSpringContext);

            try {
                // Since Camel is used in MC mainly for externally triggered tasks,
                // we need to get around the standard non-blocking startup behavior.
                // Starting the Camel Context and wait for it to finish.
                BlockingCamelStarter theBlockingCamelStarter = new BlockingCamelStarter(mCamelContext);
                theBlockingCamelStarter.get();
                // Create Camel "clients".
                mConsumerTemplate = mCamelContext.createConsumerTemplate();
                mProducerTemplate = mCamelContext.createProducerTemplate();

            } catch (Exception e) {
                LOGGER.error("Failed to start camel", e);
            }
            LOGGER.info("Message Cowboy Transport using Apache Camel {} - Status: {}", mCamelContext.getVersion(),
                mCamelContext.getStatus().toString());
            LOGGER.debug("The following Camel components are available: {}",
                StringUtils.join(mCamelContext.getComponentNames(), ","));
        } else {
            LOGGER.debug("No changes in configuration resources, skips refresh");
        }
    }

    /**
     * Disposes the Camel context.
     * First, it closes the templates, then the Camel context.
     * Finally, it also closes the applications context with Components.
     * @throws Exception thrown if there is an issue closing Camel.
     */
    protected void killCamelInstance() throws Exception {
        if (mProducerTemplate != null) {
            mProducerTemplate.stop();
        }
        if (mConsumerTemplate != null) {
            mConsumerTemplate.stop();
        }
        if (mCamelContext != null && !mCamelContext.isStopped()) {
            mCamelContext.stop();
        }
        if (mCamelSpringContext != null) {
            mCamelSpringContext.close(); // also unregisters shutdown hook.
        }
    }

    /**
     * Starts a {@link CamelContext} in a blocking fashion.
     * The call {@link #get()} or {@link #get(long, TimeUnit)} to start Camel.
     * @author Petter Nordlander
     *
     */
    class BlockingCamelStarter implements Future<Boolean>, StartupListener {

        private CamelContext mCamelContext;
        private CountDownLatch mCountDownLatch;

        /**
         * Create a Blocking Camel Starter.
         * The constructor does not start Camel, to do that invoke:
         * {@link #get()} or {@link #get(long, TimeUnit)}.
         * @param inCamelContext the Camel context to start.
         */
        public BlockingCamelStarter(final CamelContext inCamelContext) {
            mCamelContext = inCamelContext;
            mCountDownLatch = new CountDownLatch(1);
        }

        private void start() throws Exception {
            mCamelContext.addStartupListener(this);
            mCamelContext.start();
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            // TODO TO be implemented.
            return false;
        }

        @Override
        public boolean isCancelled() {
            if (isDone()) {
                return false;
            } else {
                mCountDownLatch.countDown();
                return !isDone();
            }
        }

        @Override
        public boolean isDone() {
            return mCountDownLatch.getCount() == 0;
        }

        @Override
        public Boolean get() throws InterruptedException, ExecutionException {
            try {
                start();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
            mCountDownLatch.await();
            return true;
        }

        @Override
        public Boolean get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException {
            try {
                start();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
            return mCountDownLatch.await(timeout, unit);
        }

        @Override
        public void onCamelContextStarted(final CamelContext context, final boolean alreadyStarted) throws Exception {
            mCountDownLatch.countDown();
        }
    }

    public SpringCamelContext getCamelContext() {
        return mCamelContext;
    }
}
