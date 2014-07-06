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
package se.ivankrizsan.messagecowboy.services.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import se.ivankrizsan.messagecowboy.domain.entities.SchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;
import se.ivankrizsan.messagecowboy.services.scheduling.exceptions.SchedulingException;

/**
 * Scheduling service implementation using the Quartz scheduler.<br/>
 * Information on how to construct Quartz cron expressions can be found here:
 * http://www.quartz-scheduler.org/documentation/quartz-2.2.x/tutorials/crontrigger
 *
 * @author Ivan Krizsan
 */
@Service
class QuartzSchedulingService implements SchedulingService {
    /* Constant(s): */

    /* Instance variable(s): */
    /** Helper object that schedules tasks using the Quartz scheduler. */
    protected QuartzSchedulerHelper mQuartzSchedulerHelper;

    /**
     * Schedules a task according to supplied task configuration,
     * passing in supplied job data map, using the Quartz scheduler.<br/>
     * If the supplied task configuration indicates that the task is disabled,
     * then the task will not be scheduled.
     *
     * @param inTaskConfiguration Configuration of task to schedule.
     * Must also implement the Quartz{@code Job} interface when used with
     * this service implementation.
     * @param inJobDataMap Map holding information to be passed to the
     * job being executed. Contents of map will be copied.
     */
    @Override
    public void scheduleTask(
        final SchedulableTaskConfig inTaskConfiguration,
        final Map<String, Object> inJobDataMap) {
        if (inTaskConfiguration.getTaskEnabledFlag()) {
            /*
             * Scheduling a task using Quartz require the job class to implement
             * the Quartz Job interface.
             */
            final boolean theGotJobInterfaceFlag =
                Job.class
                .isAssignableFrom(inTaskConfiguration.getTaskJobType());
            if (theGotJobInterfaceFlag == false) {
                throw new IllegalArgumentException(
                    "Job class must implement the Quartz Job interface");
            }
            @SuppressWarnings("unchecked")
            final Class<? extends Job> theQuartzJobClass =
            (Class<? extends Job>) inTaskConfiguration.getTaskJobType();

            /* Validate cron expression before trying to schedule the task. */
            if (!CronExpression.isValidExpression(inTaskConfiguration
                .getCronExpression())) {
                throw new SchedulingException("'"
                    + inTaskConfiguration.getCronExpression()
                    + "'  is not a valid cron expression. "
                    + "Unable to schedule task "
                    + inTaskConfiguration.getName() + " in group "
                    + inTaskConfiguration.getTaskGroupName());
            }

            try {
                /*
                 * Note that task name is used both as job and as trigger name
                 * when scheduling the task.
                 */
                mQuartzSchedulerHelper.scheduleCronTask(
                    inTaskConfiguration.getName(),
                    inTaskConfiguration.getName(),
                    inTaskConfiguration.getTaskGroupName(), theQuartzJobClass,
                    inTaskConfiguration.getCronExpression(), inJobDataMap,
                    inTaskConfiguration.getStartDate(),
                    inTaskConfiguration.getEndDate());

            } catch (final Exception theException) {
                throw new SchedulingException(
                    "An error occurred trying to schedule a task", theException);
            }
        }
    }

    @Override
    public void scheduleMethodInvocation(final Object inTargetObject,
        final String inTargetMethodName,
        final Object[] inTargetMethodParameters, final String inCronExpression,
        final String inTaskName, final String inTaskGroupName) {
        /* Validate cron expression before trying to schedule the task. */
        if (!CronExpression.isValidExpression(inCronExpression)) {
            throw new SchedulingException("'"
                + inCronExpression
                + "'  is not a valid cron expression. "
                + "Unable to schedule task "
                + inTaskName + " in group "
                + inTaskGroupName);
        }

        final Map<String, Object> theJobDataMap = new HashMap<String, Object>();
        theJobDataMap.put(MethodInvokingJob.TARGET_OBJECT_KEY, inTargetObject);
        theJobDataMap.put(MethodInvokingJob.TARGET_METHOD_KEY, inTargetMethodName);
        theJobDataMap.put(MethodInvokingJob.TARGET_METHOD_PARAMETERS_KEY, inTargetMethodParameters);

        try {
            /*
             * Note that task name is used both as job and as trigger name
             * when scheduling the task.
             */
            mQuartzSchedulerHelper.scheduleCronTask(
                inTaskName,
                inTaskName,
                inTaskGroupName, MethodInvokingJob.class,
                inCronExpression, theJobDataMap,
                null, null);

        } catch (final Exception theException) {
            throw new SchedulingException(
                "An error occurred trying to schedule a task", theException);
        }
    }

    public QuartzSchedulerHelper getQuartzSchedulerHelper() {
        return mQuartzSchedulerHelper;
    }

    public void setQuartzSchedulerHelper(
        final QuartzSchedulerHelper inQuartzSchedulerHelper) {
        mQuartzSchedulerHelper = inQuartzSchedulerHelper;
    }

    @Override
    public Map<String, Object> findJobDataMap(
        final String inGroupName, final String inJobName) {
        Map<String, Object> theJobDataMap = null;
        try {
            theJobDataMap =
                mQuartzSchedulerHelper.findJobDataMap(inGroupName, inJobName);
        } catch (final SchedulerException theException) {
            throw new SchedulingException(
                "An error occurred trying to retrieve the job data map for the job "
                    + inJobName + " in the group " + inGroupName, theException);
        }
        return theJobDataMap;
    }

    @Override
    public boolean unscheduleTask(
        final String inTaskGroupName, final String inTaskName) {
        boolean theTaskUnscheduledFlag = false;

        try {
            /*
             * Task name is used both as job and trigger name with Quartz,
             * so the task name is used as trigger name here.
             */
            theTaskUnscheduledFlag =
                mQuartzSchedulerHelper.unscheduleTask(inTaskGroupName,
                    inTaskName);
        } catch (final Exception theException) {
            throw new SchedulingException(
                "An error occurred trying to unschedule the task " + inTaskName
                + " in the group " + inTaskGroupName, theException);
        }

        return theTaskUnscheduledFlag;
    }

    @Override
    public void unscheduleAllTasks() {
        try {
            mQuartzSchedulerHelper.unscheduleAllTasks();
        } catch (final Exception theException) {
            throw new SchedulingException(
                "An error occurred trying to unschedule all tasks",
                theException);
        }
    }

    @Override
    public void unscheduleOtherTasks(
        final List<TaskKey> inTasksNotToUnschedule) {
        try {
            mQuartzSchedulerHelper.unscheduleOtherTasks(inTasksNotToUnschedule);
        } catch (final Exception theException) {
            throw new SchedulingException(
                "An error occurred trying to unschedule all tasks",
                theException);
        }
    }
}
