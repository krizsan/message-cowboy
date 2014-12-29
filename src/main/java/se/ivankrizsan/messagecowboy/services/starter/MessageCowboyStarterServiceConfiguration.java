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
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;

/**
 * Spring configuration class for the Message Cowboy starter service.
 *
 * @author Ivan Krizsan
 */
@Configuration
public class MessageCowboyStarterServiceConfiguration {
    /* Constant(s): */
    /** Task group name of message cowboy system task. */
    public static final String MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME = "MessageCowboySystemTasks";
    /** Task name of task scheduling update task. */
    public static final String MESSAGECOWBOY_RESCHEDULING_TASK_NAME = "MessageCowboyReschedulingTask";
    /** Task name of task scheduling transport service configuration refresh. */
    public static final String MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME =
        "MessageCowboyTransportServiceRefreshTask";
    /** Task name of task scheduling transport service configuration refresh. */
    public static final String MESSAGECOWBOY_TASK_EXECUTION_STATUS_CLEANUP_TASK_NAME =
        "MessageCowboyTaskExecutionStatusCleanupTask";
    /* Property value(s): */
    @Value("${TASK_EXECUTION_STATUS_DAYS_TO_KEEP}")
    private String taskExecutionStatusDaysToKeep;

    /**
     * Service that starts and stops the Message Cowboy application.
     *
     * @return Service instance.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MessageCowboyStarterService starterService() {
        final MessageCowboyStarterServiceImpl theService = new MessageCowboyStarterServiceImpl();
        /* Set a default task refresh interval. */
        theService.setTaskReschedulingCronExpression("0 0/2 * * * ?");
        theService.setTasksNotToUnschedule(retrieveTaskNotToUnscheduleTaskKeys());
        /* Set a default transport service configuration refresh interval. */
        theService.setTransportServiceConfigurationRefreshCronExpression("0 0/2 * * * ?");
        /* Set cleanup interval and number of days to retain reports for task execution status reports cleanup. */
        theService.setTaskExecutionStatusCleanupCronExpression("* 0/2 * * * ?");
        theService.setTaskExecutionStatusMaxAgeInDays(Integer.parseInt(taskExecutionStatusDaysToKeep));
        return theService;
    }

    /**
     * Retrieves a list of periodic tasks that are not to be unscheduled when
     * Message Cowboy refreshes schedules of other scheduled tasks.
     * These are typically tasks that perform housekeeping etc in the Message Cowboy.
     *
     * @return List of tasks that are not to be unscheduled.
     */
    public List<TaskKey> retrieveTaskNotToUnscheduleTaskKeys() {
        final List<TaskKey> theNotToRescheduleTasks = new ArrayList<TaskKey>();
        /* Task that updates schedules of Message Cowboy schedulable tasks. */
        TaskKey theDoNotUnscheduleTasksKey =
            new TaskKey(MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME, MESSAGECOWBOY_RESCHEDULING_TASK_NAME);
        theNotToRescheduleTasks.add(theDoNotUnscheduleTasksKey);
        /* Task that refreshes configuration of transport services. */
        theDoNotUnscheduleTasksKey =
            new TaskKey(MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME, MESSAGECOWBOY_TRANSPORTSERVICE_CONFIGREFRESH_TASK_NAME);
        theNotToRescheduleTasks.add(theDoNotUnscheduleTasksKey);
        /* Task that cleans up old task execution status reports. */
        theDoNotUnscheduleTasksKey =
            new TaskKey(MESSAGECOWBOY_SYSTEMTASKS_GROUPNAME, MESSAGECOWBOY_TASK_EXECUTION_STATUS_CLEANUP_TASK_NAME);
        theNotToRescheduleTasks.add(theDoNotUnscheduleTasksKey);

        return theNotToRescheduleTasks;
    }
}
