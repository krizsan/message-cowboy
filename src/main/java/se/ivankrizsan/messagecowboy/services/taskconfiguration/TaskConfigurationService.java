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
import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Defines public interface for service storing and retrieving task
 * configuration data.
 * 
 * @author Ivan Krizsan
 */
public interface TaskConfigurationService {

    /**
     * Retrieves one named task configuration.
     * 
     * @return Task configuration with matching name, or null if none found.
     */
    abstract MessageCowboySchedulableTaskConfig find(String inName);

    /**
     * Retrieves all stored task configurations.
     * 
     * @return All task configurations.
     */
    abstract List<MessageCowboySchedulableTaskConfig> findAll();

    /**
     * Retrieves all stored task configurations that are enabled.
     * 
     * @return All enabled task configurations.
     */
    abstract List<MessageCowboySchedulableTaskConfig> findAllEnabled();

    /**
     * Saves the supplied task configuration, replacing any existing task
     * configuration with the same name.
     * 
     * @param inTaskConfig Task configuration to save.
     * @return Saved task configuration. May differ from the original task
     * configuration if modified when persisted.
     */
    abstract MessageCowboySchedulableTaskConfig save(
        MessageCowboySchedulableTaskConfig inTaskConfig);
}
