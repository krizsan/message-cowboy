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
package se.ivankrizsan.messagecowboy;

import java.util.Properties;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * Bean property overrides for the production environment.
 * 
 * @author Ivan Krizsan
 */
@Configuration
public class ProductionPropertyOverrides {

    /**
     * Overrides properties configured on beans.
     */
    @Bean()
    @Lazy(false)
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public static BeanFactoryPostProcessor propertyOverrideConfigurer() {
        PropertyOverrideConfigurer theOverrideConfigurer =
            new PropertyOverrideConfigurer();

        final Properties thePropertiesHolder = new Properties();
        /* Task refresh interval: Every 20 seconds. */
        thePropertiesHolder.put(
            "starterService.taskReschedulingCronExpression", "0/20 * * * * ?");
        /* Transport service configuration refresh interval: Every 30 seconds. */
        thePropertiesHolder
            .put(
                "starterService.transportServiceConfigurationRefreshCronExpression",
                "0/30 * * * * ?");

        theOverrideConfigurer.setProperties(thePropertiesHolder);
        theOverrideConfigurer.setIgnoreInvalidKeys(false);
        theOverrideConfigurer.setIgnoreResourceNotFound(false);
        theOverrideConfigurer.setOrder(0);
        return theOverrideConfigurer;
    }
}
