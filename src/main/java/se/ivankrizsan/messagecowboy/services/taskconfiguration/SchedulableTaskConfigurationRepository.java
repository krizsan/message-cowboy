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
package se.ivankrizsan.messagecowboy.services.taskconfiguration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Defines the properties of a repository that contains
 * {@code MessageCowboySchedulableTaskConfig} entities.
 * Spring Data JPA is used to create the repository implementation.
 *
 * @author Ivan Krizsan
 */
interface SchedulableTaskConfigurationRepository extends
    JpaRepository<MessageCowboySchedulableTaskConfig, String> {

    /**
     * Finds all enabled task configurations.
     *
     * @return All persisted enabled task configurations.
     */
    @Query("select t from MessageCowboySchedulableTaskConfig t where t.taskEnabledFlag = 'true'")
    abstract
        List<MessageCowboySchedulableTaskConfig> findAllEnabled();

}