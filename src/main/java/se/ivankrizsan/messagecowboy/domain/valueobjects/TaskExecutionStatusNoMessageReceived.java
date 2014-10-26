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

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Task execution status value object that indicates no message was received
 * when a task was executed.
 *
 * @author Ivan Krizsan
 */
@Entity
@DiscriminatorValue("No Incoming")
public class TaskExecutionStatusNoMessageReceived extends TaskExecutionStatus {
    /* Constant(s): */
    private static final long serialVersionUID = 4803402581625086388L;

    /**
     * Creates an instance with the message set to the empty string.
     */
    public TaskExecutionStatusNoMessageReceived() {
        super();
    }

    /**
     * Creates an instance representing the no message received outcome of an
     * execution of the supplied task having the supplied configuration.
     * Sets the additional status message to the supplied status message.
     * Sets the task execution time to the current time as default.
     *
     * @param inTaskConfiguration Task configuration for which to create
     * execution status.
     * @param inStatusMessage Status message.
     * @param inTaskExecutionTime Task execution time.
     */
    public TaskExecutionStatusNoMessageReceived(final MessageCowboySchedulableTaskConfig inTaskConfiguration,
        final String inStatusMessage, final Date inTaskExecutionTime) {
        statusMessage = inStatusMessage;
        taskExecutionTime = inTaskExecutionTime;
    }

    @Override
    public boolean getTaskSuccessfulFlag() {
        return true;
    }
}
