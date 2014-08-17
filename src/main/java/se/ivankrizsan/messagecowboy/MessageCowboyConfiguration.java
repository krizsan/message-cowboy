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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import se.ivankrizsan.messagecowboy.services.scheduling.SchedulingServiceConfiguration;
import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterServiceConfiguration;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationServiceConfiguration;
import se.ivankrizsan.messagecowboy.services.transport.TransportServiceConfiguration;

/**
 * Spring configuration class for the standalone Message Cowboy application.
 * 
 * @author Ivan Krizsan
 */
@Configuration
@PropertySource("file:message-cowboy-configuration.properties")
@EnableTransactionManagement
@Import({ PersistenceConfiguration.class, EmbeddedActiveMQConfiguration.class,
    TransportServiceConfiguration.class, SchedulingServiceConfiguration.class,
    MessageCowboyStarterServiceConfiguration.class,
    TaskConfigurationServiceConfiguration.class })
public class MessageCowboyConfiguration {

    /**
     * This bean is required in order for the property values loaded by the
     * @PropertySource annotation to become available when injecting
     * property values using the @Value annotation.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer
        propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer thePropertyPlaceholderConfigurer 
        	= new PropertySourcesPlaceholderConfigurer();
        thePropertyPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return thePropertyPlaceholderConfigurer;
    }

    /**
     * Override.
     * Location of connector and transport service configuration files
     * for the Mule implementation of the transport service.<br/>
     * These values are the values that are to be used in a production
     * environment.
     * 
     * @return List of locations where the Mule transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> muleTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<String>();
        theLocationsList.add("file:production-configurations/connectors/*.xml");
        theLocationsList
            .add("file:production-configurations/transport-service-configurations/*.xml");
        return theLocationsList;
    }
    
    /**
     * Override.
     * Location of component configuration files used for transport in the 
     * Camel implementation of the transport service.<br/>
     * These are the values to be used in a production environment.
     * 
     * @return List of locations where the Camel transport service is to search
     * for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> camelTransportServiceConfigLocations(){
    	final List<String> theLocationsList = new ArrayList<String>();
    	theLocationsList.add("file:production-configurations/camel/*.xml");
//        theLocationsList
//            .add("file:production-configurations/transport-service-configurations/*.xml");
        return theLocationsList;	
    }
}
