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

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implements service managing task execution status data.
 *
 * @author Ivan Krizsan
 */
@Service
class TaskExecutionStatusServiceImpl implements TaskExecutionStatusService {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutionStatusServiceImpl.class);

    /* Instance variable(s): */
    protected TaskExecutionStatusRepository mTaskExecutionStatusRepository;

    @Override
    public synchronized void deleteIfOlderThanDays(final int inMaxAgeInDays) {
        final Calendar theCalendar = Calendar.getInstance();
        theCalendar.add(Calendar.DAY_OF_YEAR, -inMaxAgeInDays);

        LOGGER.debug("Deleting task execution status entries from earlier than {}", theCalendar.getTime());

        mTaskExecutionStatusRepository.deleteOlderThan(theCalendar.getTime());
        mTaskExecutionStatusRepository.flush();
    }

    public TaskExecutionStatusRepository getTaskExecutionStatusRepository() {
        return mTaskExecutionStatusRepository;
    }

    public void setTaskExecutionStatusRepository(final TaskExecutionStatusRepository inTaskExecutionStatusRepository) {
        mTaskExecutionStatusRepository = inTaskExecutionStatusRepository;
    }
}
