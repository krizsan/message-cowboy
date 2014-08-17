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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultExchange;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.impl.CamelMoverMessage;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

/**
 * Test {@link CamelTransportService}.
 * @author Petter Nordlander
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CamelTransportServiceTestConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CamelTransportServiceTest extends AbstractTestBaseClass {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CamelTransportServiceTest.class);
    private static final String TEST_MESSAGE_PAYLOAD = "some payload åäöÅÄÖ";

    @Autowired
    private CamelTransportService mServiceUnderTest;
    private String mInboundFileEndpointUri;
    private String mOutboundFileEndpointUri;
    
    /**
     * Create and Auto delete a base folder for files for each test method.
     */
    @Rule
    public TemporaryFolder mTemporaryFolder = new TemporaryFolder();

    /**
     * Performs preparations before each test.
     *
     * @throws Exception If error occurs creating instance of service under
     * test.
     */
    @Before
    public void setUp() throws Exception {
        mTestDestinationDirectory = mTemporaryFolder.newFolder("destination");
        File theInputFolder = mTemporaryFolder.newFolder("input");
        mTestFile = new File(theInputFolder,"inputfile.txt");
        FileUtils.writeStringToFile(mTestFile, TEST_FILE_CONTENTS);

        /* Create endpoint URIs for the destination and source test directories. */
        final String theInputDirPath =
        	theInputFolder.getAbsolutePath().replaceAll("\\" + File.separator, "/");
        final String theDestDirPath =
            mTestDestinationDirectory.getAbsolutePath().replaceAll(
                "\\" + File.separator, "/");

        mInboundFileEndpointUri = "file://" + theInputDirPath;
        mOutboundFileEndpointUri = "file://" + theDestDirPath;
    }

    /**
     * Cleans up after each test.
     */
    @After
    public void tearDown() {
        /* Stop the Camel transport service if it exists. */
        if (mServiceUnderTest != null) {
            mServiceUnderTest.stop();
        }
    }

    /**
     * Tests dispatching of a message to a file endpoint.
     */
    @Test
    public void testSendToFileEndpoint() {
        /* Start the Camel transport service. */
        mServiceUnderTest.start();
        
        Exchange theExchange = new DefaultExchange(mServiceUnderTest.mCamelContext);
        Message theMessage = theExchange.getIn();
        theMessage.setBody(TEST_MESSAGE_PAYLOAD);
        
        /* Set name of file to be written to destination directory. */
        theMessage.setHeader(Exchange.FILE_NAME, "testfile.txt");

        final MoverMessage<Exchange> theMoverMessage = new CamelMoverMessage(theExchange);
        mServiceUnderTest.dispatch(theMoverMessage, mOutboundFileEndpointUri);

        delay(1000);

        /* Verify outcome. */
        final File[] theDestDirFiles = mTestDestinationDirectory.listFiles();
        Assert.assertEquals(
            "There should be one file in the destination directory", 1,
            theDestDirFiles.length);
    }

    /**
     * Tests receiving a message from a file endpoint.
     */
    @Test
    public void testReceiveFromFileEndpoint() {
        /* Start the Camel transport service. */
        mServiceUnderTest.start();
        LOGGER.info("Trying to receive a file from {}",mInboundFileEndpointUri);
        @SuppressWarnings("unchecked")
		final MoverMessage<Exchange> theReceivedMessage =
            mServiceUnderTest.receive(mInboundFileEndpointUri, 5000);

        /* Verify outcome. */
        Assert.assertNotNull("A message should have been received",
            theReceivedMessage);
        final Exchange theReceivedExchange =
            theReceivedMessage.getMessage();
        Assert.assertNotNull("The received message should contain a payload",
            theReceivedMessage.getMessage());
        Assert.assertNotNull("The received message should contain a payload",
            theReceivedExchange.getIn().getBody());
    }

    /**
     * Tests sending a message to and receiving a message from a JMS queue.
     */
    @Test
    public void testSendAndReceiveJms() {
        /* Start the Camel transport service. */
        mServiceUnderTest.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException theException) {
            /* Ignore exceptions. */
        }

        performAndVerifyJmsTest();
    }

    private void performAndVerifyJmsTest() {
        final String theJmsEndpointUri =
            "jms://queue:cameltransportservice.queue";
        
        final Exchange theExchange = new DefaultExchange(mServiceUnderTest.getCamelContext());
        theExchange.getIn().setBody(TEST_MESSAGE_PAYLOAD);
        final CamelMoverMessage theMoverMessage = new CamelMoverMessage(theExchange);
        mServiceUnderTest.dispatch(theMoverMessage, theJmsEndpointUri);

        delay(1000);

        @SuppressWarnings("unchecked")
		final MoverMessage<Exchange> theReceivedMessage =
            mServiceUnderTest.receive(theJmsEndpointUri, 5000);

        /* Verify outcome. */
        Assert.assertNotNull("A message should have been received",
            theReceivedMessage);
        final Exchange theReceivedExchange =
            theReceivedMessage.getMessage();
        Assert.assertNotNull("The received message should contain a payload",
            theReceivedMessage.getMessage());
        Assert.assertEquals("Payload of message should be unaltered",
            TEST_MESSAGE_PAYLOAD, theReceivedExchange.getIn().getBody());
    }

    /**
     * Tests restarting the Mule transport service after it has been stopped.
     */
    @Test
    public void testRestartService() {
        mServiceUnderTest.start();

        mServiceUnderTest.stop();

        mServiceUnderTest.start();
    }

    /**
     * Tests refresh of connector resources.
     *
     * @throws IOException If error occurs refreshing connectors. Indicates test failure.
     */
  //  @Test
    public void testConnectorResourcesRefresh() throws IOException {
        /* Set initial list of connector resources to file connector only. */
        final List<String> theLocationsList = new ArrayList<String>();
        theLocationsList.add("classpath:connectors/file-connectors.xml");
        mServiceUnderTest
        .setConnectorsResourcesLocationPattern(theLocationsList);

        mServiceUnderTest.start();

        /* Add the JMS connector after the service has been started. */
        theLocationsList
        .add("classpath:connectors/camel/jms-connector-with-embedded-amq.xml");
        mServiceUnderTest
        .setConnectorsResourcesLocationPattern(theLocationsList);

        mServiceUnderTest.refreshConnectors();

        performAndVerifyJmsTest();
    }
}
