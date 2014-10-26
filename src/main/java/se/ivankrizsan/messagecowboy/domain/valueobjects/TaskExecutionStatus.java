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
package se.ivankrizsan.messagecowboy.domain.valueobjects;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Base class for value objects holding a status message describing the
 * outcome of a task execution.
 * Instances of this class and its subclasses are immutable.
 *
 * @author Ivan Krizsan
 */
@Entity
@Table(name = "TaskExecutionStatuses")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resultStatus", discriminatorType = DiscriminatorType.STRING, length = 15)
public abstract class TaskExecutionStatus implements Serializable {
    /* Constant(s): */
    private static final long serialVersionUID = -6104099860536900092L;

    /* Instance variable(s): */
    /** Generated numeric id of the entity. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long id;
    /** Task configuration for which this is a execution status. */
    @ManyToOne(fetch = FetchType.LAZY)
    protected MessageCowboySchedulableTaskConfig taskConfiguration;
    /** Message conveying additional status information. */
    @Column(nullable = true)
    protected String statusMessage;
    /** Time task execution for which this is the status. */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date taskExecutionTime;

    /**
     * Creates an instance setting the status message to the empty string.
     */
    public TaskExecutionStatus() {
        statusMessage = "";
    }

    /**
     * Creates an instance representing a status for the supplied task
     * configuration having the supplied status message.
     * Sets the task execution time to the supplied time.
     *
     * @param inTaskConfiguration Task configuration for which to create
     * execution status.
     * @param inStatusMessage Status message.
     * @param inTaskExecutionTime Task execution time.
     */
    public TaskExecutionStatus(final MessageCowboySchedulableTaskConfig inTaskConfiguration,
        final String inStatusMessage, final Date inTaskExecutionTime) {
        statusMessage = inStatusMessage;
        taskExecutionTime = inTaskExecutionTime;
    }

    /**
     * Retrieves the outcome of the execution of a task.
     *
     * @return True if task was executed successfully, false otherwise.
     */
    public abstract boolean getTaskSuccessfulFlag();

    /**
     * Retrieves the additional status message of the execution status.
     *
     * @return Additional status message.
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Retrieves the task execution time for which this object represent
     * the execution outcome.
     *
     * @return Task execution time.
     */
    public Date getTaskExecutionTime() {
        return taskExecutionTime;
    }

    /**
     * Retrieves the task configuration for which this object represent
     * an execution outcome.
     *
     * @return Task configuration.
     */
    public MessageCowboySchedulableTaskConfig getTaskConfiguration() {
        return taskConfiguration;
    }

    /**
     * Retrieves the generated id for this execution status object.
     *
     * @return Persistence id of the execution status object.
     */
    public Long getId() {
        return id;
    }
}
