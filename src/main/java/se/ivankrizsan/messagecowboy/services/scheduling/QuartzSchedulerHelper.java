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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;

/**
 * Helper class that simplifies the programmatic creation and management of
 * scheduled Quartz jobs.
 *
 * @author Ivan Krizsan
 */
class QuartzSchedulerHelper {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerHelper.class);

    /* Instance variable(s): */
    /** Quartz scheduler. */
    @Autowired
    protected Scheduler mTaskScheduler;
    /** Flag indicating whether to wait for running jobs to complete when shutting down. */
    protected boolean mWaitForRunningJobsToCompleteAtShutdown;

    /**
     * Schedules a new task using supplied cron expression.
     *
     * @param inJobName Name of new task.
     * @param inTriggerName Name of trigger for the new task.
     * @param inGroupName Name of group to which new task and trigger is to belong.
     * @param inJobClass The job class that will be executed when the new task triggers.
     * @param inCronExpression Cron expression specifying when task is to be
     * executed.
     * @param inJobDataMap Map holding information to be passed to the
     * job being executed. Contents of map will be copied.
     * @param inStartDate Point in time after which task is to start being
     * scheduled for execution, or null if task is to be scheduled now.
     * @param inEndDate Point in time after which the task will no longer
     * be scheduled for execution, or null if task is to be scheduled
     * indefinitely.
     * @throws Exception If error occurs scheduling task.
     */
    public void scheduleCronTask(final String inJobName, final String inTriggerName, final String inGroupName,
        final Class<? extends Job> inJobClass, final String inCronExpression, final Map<String, Object> inJobDataMap,
        final Date inStartDate, final Date inEndDate) throws Exception {
        final JobDetail theJob = JobBuilder.newJob(inJobClass).withIdentity(inJobName, inGroupName).build();

        /* Copy job data from the supplied map to the actual job data map. */
        theJob.getJobDataMap().putAll(inJobDataMap);

        /*
         * Create builder that will create a schedule for the new task using
         * the supplied cron expression.
         */
        final CronScheduleBuilder theScheduleBuilder = CronScheduleBuilder.cronSchedule(inCronExpression);

        /*
         * Create a trigger builder using the schedule builder,
         * the start and end dates, if supplied.
         */
        TriggerBuilder<CronTrigger> theTriggerBuilder =
            TriggerBuilder.newTrigger().withIdentity(inTriggerName, inGroupName).withSchedule(theScheduleBuilder);
        /* If no start date supplied, start now, else start at start date. */
        if (inStartDate == null) {
            theTriggerBuilder = theTriggerBuilder.startNow();
        } else {
            theTriggerBuilder = theTriggerBuilder.startAt(inStartDate);
        }
        /*  If an end date supplied end at that date, otherwise continue indefinitely. */
        theTriggerBuilder = theTriggerBuilder.endAt(inEndDate);

        /* Schedule the new task. */
        mTaskScheduler.scheduleJob(theJob, theTriggerBuilder.build());

        LOGGER.debug("Scheduled task {} in group {}", inJobName, inGroupName);
    }

    /**
     * Unschedules the trigger with supplied trigger name in the group with
     * the supplied group name.
     * If no such trigger exists, does nothing and returns false.
     *
     * @param inGroupName Name of group of trigger to unschedule.
     * @param inTriggerName Name of trigger to unschedule.
     * @return True if task unscheduled, false otherwise.
     * @throws Exception If error occurs unscheduling task.
     */
    public boolean unscheduleTask(final String inGroupName, final String inTriggerName) throws Exception {
        final TriggerKey theTriggerKey = new TriggerKey(inTriggerName, inGroupName);
        final boolean theUnscheduledFlag = mTaskScheduler.unscheduleJob(theTriggerKey);

        if (theUnscheduledFlag) {
            LOGGER.debug("Unscheduled task {} in group {}", inTriggerName, inGroupName);
        } else {
            LOGGER.debug("Failed to unschedule task {} in group {}", inTriggerName, inGroupName);
        }

        return theUnscheduledFlag;
    }

    /**
     * Unschedules all tasks.<br/>
     * Any exceptions occurring when attempting to unschedule a single task
     * will be caught and logged. Unscheduling of subsequent tasks will
     * continue after such an exception.
     *
     * @throws Exception If error occurs retrieving all current tasks.
     */
    public void unscheduleAllTasks() throws Exception {
        Set<TriggerKey> theTriggerKeys = mTaskScheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        for (TriggerKey theTriggerKey : theTriggerKeys) {
            try {
                mTaskScheduler.unscheduleJob(theTriggerKey);

                LOGGER.debug("Unscheduled task {} in group {}", theTriggerKey.getName(), theTriggerKey.getGroup());
            } catch (final Exception theException) {
                LOGGER.warn("An error occurred unscheduling trigger {} in group {}", theTriggerKey.getName(),
                    theTriggerKey.getGroup());
                LOGGER.warn("Exception:", theException);
            }
        }
    }

    /**
     * Unschedules all tasks except those which group and task names occur
     * in the supplied list.<br/>
     * Any exceptions occurring when attempting to unschedule a single task
     * will be caught and logged. Unscheduling of subsequent tasks will
     * continue after such an exception.
     *
     * @throws Exception If error occurs retrieving all current tasks.
     */
    public void unscheduleOtherTasks(final List<TaskKey> inTasksNotToUnschedule) throws Exception {
        Set<TriggerKey> theTriggerKeys = mTaskScheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup());
        for (TriggerKey theTriggerKey : theTriggerKeys) {
            final boolean theUnscheduleFlag = shallUnscheduleTask(theTriggerKey, inTasksNotToUnschedule);
            if (theUnscheduleFlag) {
                try {
                    mTaskScheduler.unscheduleJob(theTriggerKey);

                    LOGGER.debug("Unscheduled task {} in group {}", theTriggerKey.getName(), theTriggerKey.getGroup());
                } catch (final Exception theException) {
                    LOGGER.warn("An error occurred unscheduling trigger {} in group {}", theTriggerKey.getName(),
                        theTriggerKey.getGroup());
                    LOGGER.warn("Exception:", theException);
                }
            }
        }
    }

    /**
     * Determines whether the task with the supplied trigger key shall be
     * unscheduled or not by ensuring that it is not included in the supplied
     * list of tasks not to unschedule.
     *
     * @param inTaskTriggerKey Trigger key of task to determine whether to
     * unscheduled or not.
     * @param inTasksNotToUnschedule List of tasks that are not to be
     * unscheduled.
     * @return True if task with the supplied trigger key is to be unscheduled,
     * false otherwise.
     */
    protected boolean
    shallUnscheduleTask(final TriggerKey inTaskTriggerKey, final List<TaskKey> inTasksNotToUnschedule) {
        boolean theUnscheduleFlag = true;
        for (TaskKey theTaskKey : inTasksNotToUnschedule) {
            if (inTaskTriggerKey.getGroup().equals(theTaskKey.getTaskGroupName())) {
                if (inTaskTriggerKey.getName().equals(theTaskKey.getTaskName())) {
                    theUnscheduleFlag = false;
                }
            }
        }
        return theUnscheduleFlag;
    }

    /**
     * Finds the trigger with the supplied name in the group with the supplied name.
     *
     * @param inGroupName Group name.
     * @param inTriggerName Trigger name.
     * @return Trigger, or null if no matching trigger.
     * @throws SchedulerException If an error occurred retrieving trigger.
     */
    public Trigger findTrigger(final String inGroupName, final String inTriggerName) throws SchedulerException {
        final TriggerKey theTriggerKey = new TriggerKey(inGroupName, inTriggerName);
        final Trigger theTrigger = mTaskScheduler.getTrigger(theTriggerKey);
        return theTrigger;
    }

    /**
     * Finds the job detail with the supplied name in the group with the supplied name.
     *
     * @param inGroupName Group name.
     * @param inJobName Job name.
     * @return Job, or null if no matching job.
     * @throws SchedulerException If an error occurred retrieving job.
     */
    public JobDetail findJobDetail(final String inGroupName, final String inJobName) throws SchedulerException {
        final JobKey theJobKey = new JobKey(inJobName, inGroupName);
        final JobDetail theJobDetail = mTaskScheduler.getJobDetail(theJobKey);
        return theJobDetail;
    }

    /**
     * Finds the job data map for the job with the supplied name in the group with the supplied name.
     *
     * @param inGroupName Group name.
     * @param inJobName Job name.
     * @return Job data map, or null if no matching job.
     * @throws SchedulerException If an error occurred retrieving job.
     */
    public Map<String, Object> findJobDataMap(final String inGroupName, final String inJobName)
        throws SchedulerException {
        Map<String, Object> theJobDataMap = null;
        final JobDetail theJobDetail = findJobDetail(inGroupName, inJobName);

        if (theJobDetail != null) {
            theJobDataMap = theJobDetail.getJobDataMap();
        }
        return theJobDataMap;
    }

    public boolean isWaitForRunningJobsToCompleteAtShutdown() {
        return mWaitForRunningJobsToCompleteAtShutdown;
    }

    public void setWaitForRunningJobsToCompleteAtShutdown(final boolean inWaitForRunningJobsToCompleteAtShutdown) {
        mWaitForRunningJobsToCompleteAtShutdown = inWaitForRunningJobsToCompleteAtShutdown;
    }
}
