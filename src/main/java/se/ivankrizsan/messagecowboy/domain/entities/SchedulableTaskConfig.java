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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TransportProperty;

/**
 * Holds the configuration for a schedulable task.<br/>
 * Such a task is scheduled using a cron expression, an optional start date
 * and an optional end date.
 *
 * @author Ivan Krizsan
 */
public interface SchedulableTaskConfig extends Serializable {
    /**
     * Retrieves the name of the task.
     *
     * @return Task name.
     */
    abstract String getName();

    /**
     * Sets the name of the task to supplied name.
     *
     * @param inName New task name.
     */
    abstract void setName(String inName);

    /**
     * Retrieves the name of the group, if any, the task belongs to.
     *
     * @return Task group name, or null if the task does not belong to a group.
     */
    abstract String getTaskGroupName();

    /**
     * Sets the name of the group, if any, the task belongs to.
     *
     * @param inTaskGroupName Task group name, or null if the task does not belong to a group.
     */
    abstract void setTaskGroupName(String inTaskGroupName);

    /**
     * Retrieves the cron expression that determines when the task will run.
     *
     * @return Cron expression.
     */
    abstract String getCronExpression();

    /**
     * Sets the cron expression that determines when the task will run.
     *
     * @param inCronExpression Cron expression.
     */
    abstract void setCronExpression(String inCronExpression);

    /**
     * Retrieves the optional start date after which the task will be scheduled
     * for execution.<br/>
     * If null, then the task will be started immediately when scheduled.
     *
     * @return Task start date.
     */
    abstract Date getStartDate();

    /**
     * Sets the optional start date after which the task will be scheduled
     * for execution.
     *
     * @param inStartDate Task start date, or null if task is to start
     * immediately when scheduled.
     */
    abstract void setStartDate(Date inStartDate);

    /**
     * Retrieves the optional end date after which the task no longer will be
     * scheduled for execution.<br/>
     * If null, then the task will be scheduled for execution indefinitely.
     *
     * @return Task end date.
     */
    abstract Date getEndDate();

    /**
     * Sets the optional end date after which the task no longer will be
     * scheduled for execution.
     *
     * @param inEndDate Task end date, or null if task is to be scheduled for
     * execution indefinitely.
     */
    abstract void setEndDate(Date inEndDate);

    /**
     * Retrieves the flag indicating whether the task is enabled.<br/>
     * A disabled task will not be scheduled for execution.
     *
     * @return Task enabled flag.
     */
    abstract boolean getTaskEnabledFlag();

    /**
     * Sets the flag indicating whether the task is enabled.<br/>
     * A disabled task will not be scheduled for execution.
     *
     * @param inTaskEnabledFlag Task enabled flag.
     */
    abstract void setTaskEnabledFlag(final boolean inTaskEnabledFlag);

    /**
     * Retrieves the type of the job to be executed at the interval specified
     * by this task.
     *
     * @return Task job type.
     */
    abstract Class<? extends TaskJob> getTaskJobType();

    /**
     * Retrieves the transport properties of the task.
     *
     * @return Task's transport properties.
     */
    abstract List<TransportProperty> getTransportProperties();

    /**
     * Sets the transport properties of the task.
     * May not be null.
     *
     * @param inTransportProperties Transport properties of the task.
     */
    abstract void setTransportProperties(
        final List<TransportProperty> inTransportProperties);

    /**
     * Retrieves the task execution status objects related to the task.
     *
     * @return Task's execution status objects. Empty collection if
     * task has never been executed.
     */
    abstract List<TaskExecutionStatus> getTaskExecutionStatuses();

    /**
     * Sets the task execution status objects related to the task.
     *
     * @param inTaskExecutionStatuses Task's execution status objects.
     */
    abstract void setTaskExecutionStatuses(
        List<TaskExecutionStatus> inTaskExecutionStatuses);

    /**
     * Adds the supplied task execution status object to those associated
     * with the task.
     *
     * @param inNewTaskExecutionStatus Task execution status to add.
     */
    abstract void addTaskExecutionStatus(
        TaskExecutionStatus inNewTaskExecutionStatus);
}