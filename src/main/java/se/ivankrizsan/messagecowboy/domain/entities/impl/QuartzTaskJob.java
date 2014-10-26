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

import java.util.Date;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;
import se.ivankrizsan.messagecowboy.domain.entities.TaskJob;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusError;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusNoMessageReceived;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusSuccess;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.services.transport.TransportService;

/**
 * Implements a task job that moves messages from a source endpoint to a
 * destination endpoint.<br/>
 * The task job is implemented to use the Quartz scheduler and available {@link TransportService}.
 *
 * @author Ivan Krizsan
 */
public class QuartzTaskJob implements Job, TaskJob {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(QuartzTaskJob.class);
    /** Key used to locate task configuration in Quartz job data map. */
    public static final String TASK_CONFIGURATION_JOB_DATA_KEY = "qMoverTask";
    /** Key used to locate transport service in Quartz job data map. */
    public static final String TRANSPORT_SERVICE_JOB_DATA_KEY =
        "qTransportService";
    /** Key used to locate task configuration service in Quartz job data map. */
    public static final String TASK_CONFIGURATION_SERVICE_JOB_DATA_KEY =
        "qTaskConfigurationService";

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
        final TaskConfigurationService theTaskConfigurationService =
            findTaskConfigurationServiceInJobdata(inJobExecutionContext);

        if (theMoverTaskConfig != null && theTransportService != null
            && theTaskConfigurationService != null) {
            executeMoverTaskJob(theMoverTaskConfig, theTransportService,
                theTaskConfigurationService);
        } else {
            if (theMoverTaskConfig == null) {
                LOGGER
                .error("Job data map did not contain mover task configuration");
            }
            if (theTransportService == null) {
                LOGGER.error("Job data map did not contain transport service");
            }
            if (theTaskConfigurationService == null) {
                LOGGER
                .error("Job data map did not contain task configuration service");
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
     * Finds the task configuration service object in the job data of the
     * supplied job execution context.
     *
     * @param inJobExecutionContext Job execution context in which to look for
     * task configuration service..
     * @return Taskj configuration service, or null if no object found.
     */
    protected TaskConfigurationService findTaskConfigurationServiceInJobdata(
        final JobExecutionContext inJobExecutionContext) {
        TaskConfigurationService theTransportService = null;
        final Object theObject =
            inJobExecutionContext.getJobDetail().getJobDataMap().get(
                TASK_CONFIGURATION_SERVICE_JOB_DATA_KEY);
        if (theObject != null && theObject instanceof TaskConfigurationService) {
            theTransportService = (TaskConfigurationService) theObject;
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
     * @param inTransportService Transport service used to request and
     * dispatch messages when execution task.
     * @param inTaskConfigurationService Task configuration service used
     * to update task status after task execution.
     * @throws JobExecutionException If error occurs executing job.
     */
    protected void executeMoverTaskJob(
        final MessageCowboySchedulableTaskConfig inMoverTask,
        final TransportService inTransportService,
        final TaskConfigurationService inTaskConfigurationService)
            throws JobExecutionException {
        @SuppressWarnings("rawtypes")
        MoverMessage theInboundMessage;
        JobExecutionException theJobExecutionException = null;
        final long theTaskStartTime = System.currentTimeMillis();

        LOGGER.debug("Executing mover task job {}", inMoverTask.getName());

        try {
            theInboundMessage =
                requestInboundMessage(inTransportService, inMoverTask);

            LOGGER.debug("Message received from {}: {}", inMoverTask
                .getInboundEndpointURI(), theInboundMessage);

            if (theInboundMessage != null) {
                /* Received a message. Now try to dispatch it. */
                LOGGER.debug("Dispatching message to {}", inMoverTask
                    .getOutboundEndpointURI());
                dispatchOutboundMessage(inTransportService, inMoverTask,
                    theInboundMessage);

                addTaskExecutionSuccessToTask(inMoverTask, theTaskStartTime);
            } else {
                /* No message received, nothing to dispatch. */
                addTaskExecutionNoMessageReceivedToTask(inMoverTask);
            }

        } catch (final JobExecutionException theException) {
            /* Error occurred during task execution. */
            theJobExecutionException = theException;

            addTaskExecutionErrorToTask(inMoverTask, theException);
        }

        inTaskConfigurationService.save(inMoverTask);

        /* Re-throw any exceptions thrown during execution of task. */
        if (theJobExecutionException != null) {
            throw theJobExecutionException;
        }
    }

    /**
     * Adds a task execution status to the supplied task indicating that no
     * message was received during the last execution of the task.
     *
     * @param inMoverTask Task to add execution status to.
     */
    protected void addTaskExecutionNoMessageReceivedToTask(
        final MessageCowboySchedulableTaskConfig inMoverTask) {
        final TaskExecutionStatus theTaskStatus =
            new TaskExecutionStatusNoMessageReceived(inMoverTask, "",
                new Date());
        inMoverTask.addTaskExecutionStatus(theTaskStatus);
    }

    /**
     * Adds a task execution status to the supplied task indicating that
     * an error occurred during the last execution of the task.
     *
     * @param inMoverTask Task to add execution status to.
     * @param inException Exception that occurred during task execution, or
     * null if no exception occurred.
     */
    protected void addTaskExecutionErrorToTask(
        final MessageCowboySchedulableTaskConfig inMoverTask,
        final JobExecutionException inException) {
        String theTaskStatusMsg = "";
        if (inException != null) {
            theTaskStatusMsg = inException.getLocalizedMessage();
        }

        final TaskExecutionStatus theTaskStatus =
            new TaskExecutionStatusError(inMoverTask, theTaskStatusMsg,
                new Date());
        inMoverTask.addTaskExecutionStatus(theTaskStatus);
    }

    /**
     * Adds a task execution status to the supplied task indicating that the
     * last execution of the task had a successful outcome.
     *
     * @param inMoverTask Task to add execution status to.
     * @param inTaskStartTime Task execution start time in milliseconds.
     */
    protected void addTaskExecutionSuccessToTask(
        final MessageCowboySchedulableTaskConfig inMoverTask,
        final long inTaskStartTime) {
        final long theTaskEndTime = System.currentTimeMillis();
        final long theTaskExecutionTime = theTaskEndTime - inTaskStartTime;
        final String theTaskStatusMsg =
            "Executed in " + theTaskExecutionTime + " milliseconds";

        final TaskExecutionStatus theTaskStatus =
            new TaskExecutionStatusSuccess(inMoverTask, theTaskStatusMsg,
                new Date());
        inMoverTask.addTaskExecutionStatus(theTaskStatus);
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
        @SuppressWarnings("rawtypes") final MoverMessage inOutboundMessage)
            throws JobExecutionException {
        try {
            inTransportService.dispatch(inOutboundMessage, inMoverTask
                .getOutboundEndpointURI());
        } catch (final Throwable theException) {
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
    @SuppressWarnings("rawtypes")
    protected MoverMessage requestInboundMessage(
        final TransportService inTransportService,
        final MessageCowboySchedulableTaskConfig inMoverTask)
            throws JobExecutionException {
        MoverMessage theInboundMessage;
        try {
            theInboundMessage =
                inTransportService.receive(inMoverTask.getInboundEndpointURI(),
                    inMoverTask.getInboundTimeout());
        } catch (final Throwable theException) {
            LOGGER.error("An error occurred when the task {} in group {} "
                + "requested an inbound message", inMoverTask.getName(),
                inMoverTask.getTaskGroupName());

            throw new JobExecutionException(theException);
        }

        return theInboundMessage;
    }
}
