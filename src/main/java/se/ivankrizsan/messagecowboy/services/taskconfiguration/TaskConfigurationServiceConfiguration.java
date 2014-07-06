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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring configuration class for the task configuration service.
 * 
 * @author Ivan Krizsan
 */
@Configuration
@EnableJpaRepositories(
    basePackages = { "se.ivankrizsan.messagecowboy.services.taskconfiguration" })
public class TaskConfigurationServiceConfiguration {
    @Autowired
    protected SchedulableTaskConfigurationRepository taskConfigurationRepository;

    /**
     * Service that stores and retrieves task configurations.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TaskConfigurationService taskConfigurationService() {
        final TaskConfigurationServiceImpl theService =
            new TaskConfigurationServiceImpl();
        theService.setTaskConfigurationRepository(taskConfigurationRepository);

        return theService;
    }
}
