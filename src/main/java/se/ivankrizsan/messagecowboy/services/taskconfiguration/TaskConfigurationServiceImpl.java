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

import org.springframework.stereotype.Service;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Implements service storing and retrieving task configuration data.
 * 
 * @author Ivan Krizsan
 */
@Service
public class TaskConfigurationServiceImpl implements TaskConfigurationService {
    /* Constant(s): */

    /* Instance variable(s): */
    protected SchedulableTaskConfigurationRepository mTaskConfigurationRepository;

    @Override
    public List<MessageCowboySchedulableTaskConfig> findAll() {
        final List<MessageCowboySchedulableTaskConfig> theTaskConfigurations =
            mTaskConfigurationRepository.findAll();
        return theTaskConfigurations;
    }

    @Override
    public List<MessageCowboySchedulableTaskConfig> findAllEnabled() {
        final List<MessageCowboySchedulableTaskConfig> theTaskConfigurations;
        theTaskConfigurations = mTaskConfigurationRepository.findAll();
        return theTaskConfigurations;
    }

    /**
     * Retrieves the task configuration repository used by the service to store
     * and retrieve persisted task configurations.
     * 
     * @return Task configuration repository.
     */
    public SchedulableTaskConfigurationRepository
        getTaskConfigurationRepository() {
        return mTaskConfigurationRepository;
    }

    /**
     * Sets the task configuration repository used by the service to store
     * and retrieve persisted task configurations.
     * 
     * @param inTaskConfigurationRepository Task configuration repository.
     */
    public
        void
        setTaskConfigurationRepository(
            final SchedulableTaskConfigurationRepository inTaskConfigurationRepository) {
        mTaskConfigurationRepository = inTaskConfigurationRepository;
    }
}
