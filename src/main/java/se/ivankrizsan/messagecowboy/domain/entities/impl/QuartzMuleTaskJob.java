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

import org.mule.api.MuleMessage;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.TaskJob;
import se.ivankrizsan.messagecowboy.services.transport.TransportService;
import se.ivankrizsan.messagecowboy.services.transport.exceptions.TransportException;

/**
 * Implements a task job that moves messages from a source endpoint to a
 * destination endpoint.<br/>
 * The task job is implemented to use the Quartz scheduler and Mule.
 *
 * @author Ivan Krizsan
 */
public class QuartzMuleTaskJob implements Job, TaskJob {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(QuartzMuleTaskJob.class);
    /** Key used to locate task configuration in Quartz job data map. */
    public static final String TASK_CONFIGURATION_JOB_DATA_KEY = "qMoverTask";
    /** Key used to locate transport service in Quartz job data map. */
    public static final String TRANSPORT_SERVICE_JOB_DATA_KEY =
        "qTransportService";

    /* Instance variable(s): */

    @Override
    public void execute(final JobExecutionContext inJobExecutionContext)
        throws JobExecutionException {
        final String theJobName =
            inJobExecutionContext.getJobDetail().getKey().getName();
        final String theJobGroup =
            inJobExecutionContext.getJobDetail().getKey().getGroup();

        LOGGER.debug("Started executing job {} in group {}", theJobName,
            theJobGroup);

        /* Proceed only if there is a mover task in the job data map. */
        final MessageCowboySchedulableTaskConfig theMoverTaskConfig =
            findMoverTaskConfigInJobdata(inJobExecutionContext);
        final TransportService theTransportService =
            findTransportServiceInJobdata(inJobExecutionContext);

        if (theMoverTaskConfig != null && theTransportService != null) {
            executeMoverTaskJob(theMoverTaskConfig, theTransportService);
        } else {
            if (theMoverTaskConfig == null) {
                LOGGER
                .error("Job data map did not contain mover task configuration");
            }
            if (theTransportService == null) {
                LOGGER.error("Job data map did not contain transport service");
            }
        }
    }

    /**
     * Finds the transport service object in the job data of the supplied
     * job execution context.
     *
     * @param inJobExecutionContext Job execution context in which to look for
     * transport service..
     * @return Transport service, or null if no object found.
     */
    protected TransportService findTransportServiceInJobdata(
        final JobExecutionContext inJobExecutionContext) {
        TransportService theTransportService = null;
        final Object theObject =
            inJobExecutionContext.getJobDetail().getJobDataMap().get(
                TRANSPORT_SERVICE_JOB_DATA_KEY);
        if (theObject != null && theObject instanceof TransportService) {
            theTransportService = (TransportService) theObject;
        }
        return theTransportService;
    }

    /**
     * Finds the mover task configuration object in the job data of the supplied
     * job execution context.
     *
     * @param inJobExecutionContext Job execution context in which to look for
     * mover task configuration.
     * @return Mover task configuration, or null if no object found.
     */
    protected MessageCowboySchedulableTaskConfig findMoverTaskConfigInJobdata(
        final JobExecutionContext inJobExecutionContext) {
        MessageCowboySchedulableTaskConfig theMoverTask = null;
        final Object theConfigObject =
            inJobExecutionContext.getJobDetail().getJobDataMap().get(
                TASK_CONFIGURATION_JOB_DATA_KEY);
        if (theConfigObject != null
            && theConfigObject instanceof MessageCowboySchedulableTaskConfig) {
            theMoverTask = (MessageCowboySchedulableTaskConfig) theConfigObject;
        }
        return theMoverTask;
    }

    /**
     * Executes a mover task job with the supplied mover task configuration.
     *
     * @param inMoverTask Mover task configuration.
     * @throws JobExecutionException If error occurs executing job.
     */
    protected void executeMoverTaskJob(
        final MessageCowboySchedulableTaskConfig inMoverTask,
        final TransportService inTransportService) throws JobExecutionException {
        MoverMessage<MuleMessage> theInboundMessage;

        LOGGER.debug("Executing mover task job {}", inMoverTask.getName());

        theInboundMessage =
            requestInboundMessage(inTransportService, inMoverTask);

        LOGGER.debug("Message received from {}: {}", inMoverTask
            .getInboundEndpointURI(), theInboundMessage);

        if (theInboundMessage != null) {
            LOGGER.debug("Dispatching message to {}", inMoverTask
                .getOutboundEndpointURI());
            dispatchOutboundMessage(inTransportService, inMoverTask,
                theInboundMessage);
        }
    }

    /**
     * Dispatches supplied outbound message using supplied transport service
     * to the outbound endpoint in supplied task configuration.
     *
     * @param inTransportService Transport service to dispatch message.
     * @param inMoverTask Task configuration holding dispatch parameters.
     * @param inOutboundMessage Message to dispatch.
     * @throws JobExecutionException If error occurs dispatching message.
     */
    protected void dispatchOutboundMessage(
        final TransportService inTransportService,
        final MessageCowboySchedulableTaskConfig inMoverTask,
        final MoverMessage<MuleMessage> inOutboundMessage)
            throws JobExecutionException {
        try {
            inTransportService.dispatch(inOutboundMessage, inMoverTask
                .getOutboundEndpointURI());
        } catch (final TransportException theException) {
            LOGGER.error("An error occurred when the task {} in group {} "
                + "dispatched an outbound message", inMoverTask.getName(),
                inMoverTask.getTaskGroupName());

            throw new JobExecutionException(theException);
        }
    }

    /**
     * Requests a message using supplied transport service from the inbound
     * endpoint in supplied task configuration.
     * The request will timeout after the amount of time specified in the
     * supplied task configuration.
     *
     * @param inTransportService Transport service to request message.
     * @param inMoverTask Task configuration holding request parameters.
     * @return Received message, or null if request timed out.
     * @throws JobExecutionException If error occurs receiving message.
     */
    @SuppressWarnings("unchecked")
    protected MoverMessage<MuleMessage> requestInboundMessage(
        final TransportService inTransportService,
        final MessageCowboySchedulableTaskConfig inMoverTask)
            throws JobExecutionException {
        MoverMessage<MuleMessage> theInboundMessage;
        try {
            theInboundMessage =
                inTransportService.receive(inMoverTask.getInboundEndpointURI(),
                    inMoverTask.getInboundTimeout());
        } catch (final TransportException theException) {
            LOGGER.error("An error occurred when the task {} in group {} "
                + "requested an inbound message", inMoverTask.getName(),
                inMoverTask.getTaskGroupName());

            throw new JobExecutionException(theException);
        }

        return theInboundMessage;
    }
}
