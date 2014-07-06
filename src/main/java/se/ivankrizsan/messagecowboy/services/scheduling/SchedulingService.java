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

import java.util.List;
import java.util.Map;

import se.ivankrizsan.messagecowboy.domain.entities.SchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;

/**
 * Interface specifying the properties of a scheduling service that can
 * schedule tasks to be performed at regular intervals.
 *
 * @author Ivan Krizsan
 */
public interface SchedulingService {

    /**
     * Schedules a task according to supplied task configuration,
     * passing in supplied job data map, using the Quartz scheduler.<br/>
     * If the supplied task configuration indicates that the task is disabled,
     * then the task will not be scheduled.
     *
     * @param inTaskConfiguration Configuration of task to schedule.
     * @param inJobDataMap Map holding information to be passed to the
     * job being executed. Contents of map will be copied.
     */
    abstract void scheduleTask(
        final SchedulableTaskConfig inTask,
        final Map<String, Object> inJobDataMap);


    /**
     * Schedules an invocation of a method with the supplied name on the supplied target object
     * with the supplied parameters according to the supplied cron expression.
     * The task created has the supplied task name and will belong to the task group with the
     * supplied name.
     *
     * @param inTargetObject Object on which the method is to be invoked.
     * @param inTargetMethodName Name of method to be invoked.
     * @param inTargetMethodParameters Parameter(s) that are to be passed to the method,
     * or null if the method does not take any parameters.
     * @param inCronExpression Cron expression specifying the method invocation schedule.
     * @param inTaskName Name of the task that is to be created.
     * @param inTaskGroupName Task group to which the task is to belong.
     */
    abstract void scheduleMethodInvocation(final Object inTargetObject,
        final String inTargetMethodName,
        final Object[] inTargetMethodParameters, final String inCronExpression,
        final String inTaskName, final String inTaskGroupName);

    /**
     * Finds the job data map for the job with the supplied name in the group
     * with supplied name.
     *
     * @param inGroupName Job group name.
     * @param inJobName Job name.
     * @return Map holding job data, or null if no matching job found.
     */
    abstract Map<String, Object> findJobDataMap(
        final String inGroupName, final String inJobName);

    /**
     * Unschedules the task with the supplied name in the group with supplied
     * name. Does nothing if the job does not exist.
     *
     * @param inTaskGroupName Task group name.
     * @param inTaskName Task name.
     * @return True if task unscheduled, false otherwise. False will be returned
     * if no matching task exists.
     */
    abstract boolean unscheduleTask(
        final String inTaskGroupName, final String inTaskName);

    /**
     * Unschedules all tasks. Does nothing if no tasks exists.
     */
    abstract void unscheduleAllTasks();

    /**
     * Unschedules all tasks except those specified in the supplied list.
     *
     * @param inTasksNotToUnschedule List of group and task name pairs of tasks
     * that not are to be unscheduled.
     */
    abstract void unscheduleOtherTasks(
        final List<TaskKey> inTasksNotToUnschedule);
}