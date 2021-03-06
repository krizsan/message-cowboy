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
package se.ivankrizsan.messagecowboy.services.starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import se.ivankrizsan.messagecowboy.domain.entities.SchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.entities.impl.QuartzTaskJob;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;
import se.ivankrizsan.messagecowboy.services.scheduling.SchedulingService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.services.taskexecutionstatus.TaskExecutionStatusService;
import se.ivankrizsan.messagecowboy.services.transport.TransportService;

/**
 * Implements the component responsible for starting and stopping
 * the Message Cowboy application.<br/>
 * This service also schedules the tasks available from the task configuration
 * service and, periodically, refreshes the scheduled tasks.
 *
 * @author Ivan Krizsan
 */
@Service
class MessageCowboyStarterServiceImpl implements MessageCowboyStarterService {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCowboyStarterServiceImpl.class);

    /* Instance variable(s): */
    @Autowired
    protected SchedulingService mSchedulingService;
    @Autowired
    protected TransportService mTransportService;
    @Autowired
    protected TaskConfigurationService mTaskConfigurationService;
    @Autowired
    protected TaskExecutionStatusService mTaskExecutionStatusService;
    /** Cron expression determining when Message Cowboy tasks will be refreshed. */
    protected String mTaskReschedulingCronExpression;
    /** List of tasks that are not to be unscheduled when refreshing task schedules. */
    protected List<TaskKey> mTasksNotToUnschedule = new ArrayList<TaskKey>();
    /** Cron expression determining when configurations of the transport service will be refreshed. */
    protected String mTransportServiceConfigurationRefreshCronExpression;
    /** Cron expression determining when the task execution status cleanup will be performed. */
    protected String mTaskExecutionStatusCleanupCronExpression;
    /** Maximum age of task execution status reports that are to be retained. */
    protected int mTaskExecutionStatusMaxAgeInDays;

    @Override
    public void start() {
        LOGGER.info("Message Cowboy starting...");
        scheduleTasks();
        scheduleSystemTasks();
        LOGGER.info("Message Cowboy started");
    }

    /**
     * Schedules the Message Cowboy system tasks.
     */
    protected void scheduleSystemTasks() {
        /* Schedule task that refreshes the scheduled Message Cowboy tasks. */
        mSchedulingService.scheduleMethodInvocation(this, "scheduleTasks", null, mTaskReschedulingCronExpression,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_RESCHEDULING_TASK_NAME,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME);

        /* Schedule task that refreshes transport service configurations. */
        mSchedulingService.scheduleMethodInvocation(mTransportService, "refreshConnectors", null,
            mTransportServiceConfigurationRefreshCronExpression,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME);

        /* Schedule task execution status periodic cleanup. */
        final Object[] theDeleteIfOlderThanDaysParameter = new Object[1];
        theDeleteIfOlderThanDaysParameter[0] = mTaskExecutionStatusMaxAgeInDays;
        mSchedulingService.scheduleMethodInvocation(mTaskExecutionStatusService, "deleteIfOlderThanDays",
            theDeleteIfOlderThanDaysParameter, mTaskExecutionStatusCleanupCronExpression,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_TASK_EXECUTION_STATUS_CLEANUP_TASK_NAME,
            MessageCowboyStarterServiceConfiguration.MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME);
    }

    @Override
    public void stop() {
        mSchedulingService.unscheduleAllTasks();

        LOGGER.info("Message Cowboy stopped");
    }

    @Override
    public void scheduleTasks() {
        LOGGER.info("Starting to (re)schedule Message Cowboy tasks");
        /* First unschedule all non-system tasks, in case tasks have been scheduled earlier. */
        mSchedulingService.unscheduleOtherTasks(mTasksNotToUnschedule);
        LOGGER.info("Existing tasks unscheduled");

        /* Read all current task configurations from database. */
        List<MessageCowboySchedulableTaskConfig> theTaskConfigurations = mTaskConfigurationService.findAll();
        LOGGER.debug("Found {} number of tasks", theTaskConfigurations.size());

        /* Schedule all enabled tasks. */
        for (SchedulableTaskConfig theTaskConfiguration : theTaskConfigurations) {
            if (theTaskConfiguration.getTaskEnabledFlag()) {
                /*
                 * Create job data map and fill it with the data and
                 * references to the services needed when the task is
                 * executed.
                 */
                final Map<String, Object> theJobDataMap = new HashMap<String, Object>();
                theJobDataMap.put(QuartzTaskJob.TASK_CONFIGURATION_JOB_DATA_KEY, theTaskConfiguration);
                theJobDataMap.put(QuartzTaskJob.TRANSPORT_SERVICE_JOB_DATA_KEY, mTransportService);
                theJobDataMap.put(QuartzTaskJob.TASK_CONFIGURATION_SERVICE_JOB_DATA_KEY, mTaskConfigurationService);

                mSchedulingService.scheduleTask(theTaskConfiguration, theJobDataMap);

                LOGGER.debug("Scheduled task {} in group {}", theTaskConfiguration.getName(),
                    theTaskConfiguration.getTaskGroupName());
            } else {
                LOGGER.debug("Task {} in group {} is disabled and thus not scheduled", theTaskConfiguration.getName(),
                    theTaskConfiguration.getTaskGroupName());
            }
        }

        LOGGER.info("Successfully (re)scheduled Message Cowboy tasks");
    }

    public String getTaskReschedulingCronExpression() {
        return mTaskReschedulingCronExpression;
    }

    @Required
    public void setTaskReschedulingCronExpression(final String inTaskReschedulingCronExpression) {
        mTaskReschedulingCronExpression = inTaskReschedulingCronExpression;
    }

    public String getTransportServiceConfigurationRefreshCronExpression() {
        return mTransportServiceConfigurationRefreshCronExpression;
    }

    @Required
    public void setTransportServiceConfigurationRefreshCronExpression(
        final String inTransportServiceConfigurationRefreshCronExpression) {
        mTransportServiceConfigurationRefreshCronExpression = inTransportServiceConfigurationRefreshCronExpression;
    }

    public void setTasksNotToUnschedule(final List<TaskKey> inTasksNotToUnschedule) {
        mTasksNotToUnschedule = inTasksNotToUnschedule;
    }

    public String getTaskExecutionStatusCleanupCronExpression() {
        return mTaskExecutionStatusCleanupCronExpression;
    }

    @Required
    public void setTaskExecutionStatusCleanupCronExpression(final String inTaskExecutionStatusCleanupCronExpression) {
        mTaskExecutionStatusCleanupCronExpression = inTaskExecutionStatusCleanupCronExpression;
    }

    public int getTaskExecutionStatusMaxAgeInDays() {
        return mTaskExecutionStatusMaxAgeInDays;
    }

    @Required
    public void setTaskExecutionStatusMaxAgeInDays(final int inTaskExecutionStatusMaxAgeInDays) {
        mTaskExecutionStatusMaxAgeInDays = inTaskExecutionStatusMaxAgeInDays;
    }
}
