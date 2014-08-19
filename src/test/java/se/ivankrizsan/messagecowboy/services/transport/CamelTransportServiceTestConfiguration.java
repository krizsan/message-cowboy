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
package se.ivankrizsan.messagecowboy.services.transport;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Spring config for {@link CamelTransportServiceTest}.
 * @author Petter Nordlander
 *
 */
@Configuration
public class CamelTransportServiceTestConfiguration {

    /**
     * Transport service, Camel implementation.
     * In this test configuration, the transport service is not started
     * and stopped automatically.
     * 
     * @return Service instance.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TransportService transportService() {
        final CamelTransportService theService = new CamelTransportService();
        theService
            .setConnectorsResourcesLocationPattern(camelTransportServiceConfigLocations());

        return theService;
    }

    /**
     * Override.
     * Location of connector and transport service configuration files
     * for the Camel implementation of the transport service.<br/>
     * Test configuration.
     * 
     * @return List of locations where the Camel transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> camelTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<String>();
        theLocationsList
            .add("classpath:connectors/camel/jms-connector-with-embedded-amq.xml");

        return theLocationsList;
    }
}
