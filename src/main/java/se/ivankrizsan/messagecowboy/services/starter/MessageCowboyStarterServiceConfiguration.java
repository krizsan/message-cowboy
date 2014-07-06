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
package se.ivankrizsan.messagecowboy.services.starter;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring configuration class for the Message Cowboy starter service.
 *
 * @author Ivan Krizsan
 */
@Configuration
public class MessageCowboyStarterServiceConfiguration {

    /**
     * Service that starts and stops the Message Cowboy application.
     *
     * @return Service instance.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public MessageCowboyStarterService starterService() {
        final MessageCowboyStarterServiceImpl theService =
            new MessageCowboyStarterServiceImpl();
        /* Set a default task refresh interval - every 5 minutes. */
        theService.setTaskReschedulingCronExpression("0 0/5 * * * ?");
        /* 
         * Set a default transport service configuration refresh interval:
         * Every 5 minutes.
         */
        theService
            .setTransportServiceConfigurationRefreshCronExpression("0 0/5 * * * ?");

        return theService;
    }
}
