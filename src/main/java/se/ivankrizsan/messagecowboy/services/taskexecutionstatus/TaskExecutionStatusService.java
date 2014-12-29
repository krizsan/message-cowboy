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
package se.ivankrizsan.messagecowboy.services.taskexecutionstatus;

/**
 * Defines public interface for a service managing task execution status reports.
 *
 * @author Ivan Krizsan
 */
public interface TaskExecutionStatusService {

    /**
     * Deletes all task execution status reports that are older than the supplied number of days.
     *
     * @param inMaxAgeInDays Maximum age in days of task execution status reports to retain.
     */
    abstract void deleteIfOlderThanDays(final int inMaxAgeInDays);

}
