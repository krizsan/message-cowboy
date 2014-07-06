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
package se.ivankrizsan.messagecowboy.domain.entities;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.entities.impl.MuleMoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.impl.QuartzMuleTaskJob;
import se.ivankrizsan.messagecowboy.services.transport.TransportService;
import se.ivankrizsan.messagecowboy.services.transport.exceptions.TransportException;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

/**
 * Tests the {@link QuartzMuleTaskJob} class.
 *
 * @author Ivan Krizsan
 */
public class QuartzMuleTaskJobTest extends AbstractTestBaseClass {
    /* Constant(s): */
    static final Logger LOGGER = LoggerFactory
        .getLogger(QuartzMuleTaskJobTest.class);

    /* Instance variable(s): */
    private TransportService mTransportService;
    /** Contains configuration for test-task. */
    private MessageCowboySchedulableTaskConfig mMoverTask;
    /** Task job under test. */
    private QuartzMuleTaskJob mTaskJobUnderTest;
    /** Quartz job execution context used in tests. */
    private JobExecutionContext mJobExecContext;

    /**
     * Sets up before each test.
     *
     * @throws Exception If error occurred during setup.
     */
    @Before
    public void setUp() throws Exception {
        /* Create transport service mock. */
        mTransportService = Mockito.mock(TransportService.class);
        Mockito.when(
            mTransportService.receive(Mockito.anyString(), Mockito.anyLong()))
            .thenReturn(new MuleMoverMessage());

        mMoverTask =
            createFileMoverTask("/SomeInputDir/", "/SomeDestinationDir/");
        mTaskJobUnderTest = new QuartzMuleTaskJob();
        mJobExecContext = createJobExecutionContextWithMoverTask(mMoverTask);
    }

    /**
     * Creates a task for moving files from one director to another
     * directory in the file system.
     *
     * @param inInputDirPath Path to input directory.
     * @param inDestDirPath Path to destination directory.
     * @return New mover task.
     */
    private MessageCowboySchedulableTaskConfig createFileMoverTask(
        final String inInputDirPath, final String inDestDirPath) {
        final MessageCowboySchedulableTaskConfig theFileMoverTask =
            new MessageCowboySchedulableTaskConfig();
        theFileMoverTask.setName("QuartzMuleTaskJobTest");
        theFileMoverTask.setTaskGroupName("TestGroup");
        /* None of the timing parameters are used in these tests. */
        theFileMoverTask.setCronExpression("0 0/5 * * * ?");
        theFileMoverTask.setInboundTimeout(1000);
        /* 
         * None of the URIs are used in these tests, since the transport
         * service is mocked.
         */
        theFileMoverTask.setInboundEndpointURI(inInputDirPath);
        theFileMoverTask.setOutboundEndpoint(inDestDirPath);

        return theFileMoverTask;
    }

    /**
     * Creates a Quartz job execution context configured to be executed by a
     * {@code QuartzMuleTaskJob} with supplied mover task.
     *
     * @param inMoverTask Mover task to insert into the job execution context.
     * @return Quartz job execution context.
     */
    private JobExecutionContext createJobExecutionContextWithMoverTask(
        final MessageCowboySchedulableTaskConfig inMoverTask) {
        final JobKey theJobKey =
            new JobKey("MoverTaskJobTest_Job", "MoverTaskJobTest_Group");

        /* Create job detail mock that returns our job key and job data map. */
        final JobDetail theJobDetail = Mockito.mock(JobDetail.class);
        Mockito.when(theJobDetail.getKey()).thenReturn(theJobKey);
        final JobDataMap theJobDataMap = new JobDataMap();
        theJobDataMap.put(QuartzMuleTaskJob.TASK_CONFIGURATION_JOB_DATA_KEY,
            inMoverTask);
        theJobDataMap.put(QuartzMuleTaskJob.TRANSPORT_SERVICE_JOB_DATA_KEY,
            mTransportService);
        Mockito.when(theJobDetail.getJobDataMap()).thenReturn(theJobDataMap);

        /* Create a job execution context that returns our job detail mock. */
        final JobExecutionContext theJobExecContext =
            Mockito.mock(JobExecutionContext.class);
        Mockito.when(theJobExecContext.getJobDetail()).thenReturn(theJobDetail);

        return theJobExecContext;
    }

    /**
     * Tests executing a job which should perform a request for a message
     * and dispatch the received message.<br/>
     * Expected result:<br/>
     * A message should be requested and dispatched by the transport service.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testSuccessfullyExecuteTaskJob() throws Exception {
        mTaskJobUnderTest.execute(mJobExecContext);

        /* 
         * Ensure that a message was received and dispatched by the transport
         * service.
         */
        Mockito.verify(mTransportService).receive(Mockito.anyString(),
            Mockito.anyLong());
        Mockito.verify(mTransportService).dispatch(
            Mockito.any(MoverMessage.class), Mockito.anyString());
    }

    /**
     * Tests executing a job which should perform a request for a message
     * and attept to dispatch the received message.<br/>
     * Expected result:<br/>
     * A message should be requested by the transport service.<br/>
     * An exception should be thrown (and caught by the test).
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testExecuteTaskJobFailDispatch() throws Exception {
        /* 
         * Mock transport service will throw exception when asked to dispatch
         * a message.
         */
        Mockito
            .doThrow(
                new TransportException("transport service mock threw exception"))
            .when(mTransportService)
            .dispatch(Mockito.any(MoverMessage.class), Mockito.anyString());

        /*
         * Execute job which should fail to dispatch message.
         * Catch the expected exception.
         */
        boolean theExceptionThrownFlag = false;
        try {
            mTaskJobUnderTest.execute(mJobExecContext);
        } catch (final JobExecutionException theException) {
            theExceptionThrownFlag = true;
        }

        /* Verify that a message has been requested. */
        Mockito.verify(mTransportService).receive(Mockito.anyString(),
            Mockito.anyLong());
        /* Verify that an exception was thrown. */
        Assert.assertTrue("An exception should have been thrown",
            theExceptionThrownFlag);
    }

    /**
     * Tests executing a job which should attempt to perform a request for
     * a message.<br/>
     * Expected result:<br/>
     * An exception should be thrown (and caught by the test).
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testExecuteTaskJobFailRequest() throws Exception {
        /* 
         * Mock transport service will throw exception when asked to dispatch
         * a message.
         */
        Mockito
            .doThrow(
                new TransportException("transport service mock threw exception"))
            .when(mTransportService)
            .receive(Mockito.anyString(), Mockito.anyLong());

        /*
         * Execute job which should fail to dispatch message.
         * Catch the expected exception.
         */
        boolean theExceptionThrownFlag = false;
        try {
            mTaskJobUnderTest.execute(mJobExecContext);
        } catch (final JobExecutionException theException) {
            theExceptionThrownFlag = true;
        }

        /* Verify that an exception was thrown. */
        Assert.assertTrue("An exception should have been thrown",
            theExceptionThrownFlag);
    }
}
