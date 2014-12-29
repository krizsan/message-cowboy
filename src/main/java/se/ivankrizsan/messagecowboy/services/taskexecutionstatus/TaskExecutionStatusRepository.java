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

import java.util.Date;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;

/**
 * Defines the properties of a repository that contains
 * {@code TaskExecutionStatus} entities.
 * Spring Data JPA is used to create the repository implementation.
 *
 * @author Ivan Krizsan
 */
interface TaskExecutionStatusRepository extends JpaRepository<TaskExecutionStatus, String> {

    /**
     * Deletes task execution status entries that are older than the supplied point in time.
     *
     * @param inMaxAge Maximum age of task execution status entries that will be retained.
     */
    @Modifying
    @Transactional
    @Query("delete from TaskExecutionStatus s where s.taskExecutionTime < :maxAge")
    void deleteOlderThan(@Param("maxAge") final Date inMaxAge);

}