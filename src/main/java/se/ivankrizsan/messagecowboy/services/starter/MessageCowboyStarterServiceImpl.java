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
    private static final Logger LOGGER = LoggerFactory
        .getLogger(MessageCowboyStarterServiceImpl.class);
    /** Task group name of task scheduling update task. */
    protected static final String MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME =
        "MessageCowboySystemTasks";
    /** Task name of task scheduling update task. */
    protected static final String MESSAGECOWBOY_RESCHEDULING_TASK_NAME =
        "MessageCowboyReschedulingTask";
    /** Task name of task scheduling transport service configuration refresh. */
    protected static final String MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME =
        "MessageCowboyTransportServiceRefreshTask";

    /* Instance variable(s): */
    @Autowired
    protected SchedulingService mSchedulingService;
    @Autowired
    protected TransportService mTransportService;
    @Autowired
    protected TaskConfigurationService mTaskConfigurationService;
    /**
     * Cron expression determining when Message Cowboy tasks will be
     * refreshed.
     */
    protected String mTaskReschedulingCronExpression;
    /**
     * List of tasks that are not to be unscheduled when refreshing task
     * schedules.
     */
    protected List<TaskKey> mTasksNotToUnschedule = new ArrayList<TaskKey>();
    /**
     * Cron expression determining when configurations of the transport service
     * will be refreshed.
     */
    protected String mTransportServiceConfigurationRefreshCronExpression;

    @Override
    public void start() {
        LOGGER.info("Message Cowboy starting...");

        scheduleTasks();

        schedulePeriodicRefreshes();

        /*
         * Fill up list of tasks that are not to be unscheduled when
         * refreshing task schedules.
         */
        TaskKey theDoNotUnscheduleTasksKey =
            new TaskKey(MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME,
                MESSAGECOWBOY_RESCHEDULING_TASK_NAME);
        mTasksNotToUnschedule.add(theDoNotUnscheduleTasksKey);
        theDoNotUnscheduleTasksKey =
            new TaskKey(MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME,
                MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME);
        mTasksNotToUnschedule.add(theDoNotUnscheduleTasksKey);

        LOGGER.info("Message Cowboy started");
    }

    /**
     * Schedules task that will refresh the Message Cowboy tasks.
     */
    protected void schedulePeriodicRefreshes() {
        /* Schedule task that refreshes the scheduled Message Cowboy tasks. */
        mSchedulingService.scheduleMethodInvocation(this, "scheduleTasks",
            null, mTaskReschedulingCronExpression,
            MESSAGECOWBOY_RESCHEDULING_TASK_NAME,
            MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME);

        /* Schedule task that refreshes transport service configurations. */
        mSchedulingService.scheduleMethodInvocation(mTransportService,
            "refreshConnectors", null,
            mTransportServiceConfigurationRefreshCronExpression,
            MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME,
            MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME);
    }

    @Override
    public void stop() {
        mSchedulingService.unscheduleAllTasks();

        LOGGER.info("Message Cowboy stopped");
    }

    @Override
    public void scheduleTasks() {
        LOGGER.info("Starting to (re)schedule Message Cowboy tasks");
        /*
         * First unschedule all non-system tasks, in case tasks have been
         * scheduled earlier.
         */
        mSchedulingService.unscheduleOtherTasks(mTasksNotToUnschedule);
        LOGGER.info("Existing tasks unscheduled");

        /* Read all current task configurations from database. */
        List<MessageCowboySchedulableTaskConfig> theTaskConfigurations =
            mTaskConfigurationService.findAll();
        LOGGER.debug("Found {} number of tasks", theTaskConfigurations.size());

        /* Schedule all enabled tasks. */
        for (SchedulableTaskConfig theTaskConfiguration : theTaskConfigurations) {
            if (theTaskConfiguration.getTaskEnabledFlag()) {
                final Map<String, Object> theJobDataMap =
                    new HashMap<String, Object>();
                theJobDataMap.put(
                    QuartzTaskJob.TASK_CONFIGURATION_JOB_DATA_KEY,
                    theTaskConfiguration);
                theJobDataMap.put(
                    QuartzTaskJob.TRANSPORT_SERVICE_JOB_DATA_KEY,
                    mTransportService);

                mSchedulingService.scheduleTask(theTaskConfiguration,
                    theJobDataMap);

                LOGGER.debug("Scheduled task {} in group {}",
                    theTaskConfiguration.getName(),
                    theTaskConfiguration.getTaskGroupName());
            } else {
                LOGGER.debug(
                    "Task {} in group {} is disabled and thus not scheduled",
                    theTaskConfiguration.getName(),
                    theTaskConfiguration.getTaskGroupName());
            }
        }

        LOGGER.info("Successfully (re)scheduled Message Cowboy tasks");
    }

    public String getTaskReschedulingCronExpression() {
        return mTaskReschedulingCronExpression;
    }

    @Required
    public void setTaskReschedulingCronExpression(
        final String inTaskReschedulingCronExpression) {
        mTaskReschedulingCronExpression = inTaskReschedulingCronExpression;
    }

    public String getTransportServiceConfigurationRefreshCronExpression() {
        return mTransportServiceConfigurationRefreshCronExpression;
    }

    @Required
    public void setTransportServiceConfigurationRefreshCronExpression(
        final String inTransportServiceConfigurationRefreshCronExpression) {
        mTransportServiceConfigurationRefreshCronExpression =
            inTransportServiceConfigurationRefreshCronExpression;
    }
}
