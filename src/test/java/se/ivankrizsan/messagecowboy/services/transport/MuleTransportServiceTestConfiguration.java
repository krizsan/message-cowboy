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

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring configuration class for the {@code MuleTransportServiceTest} test.
 *
 * @author Ivan Krizsan
 */
@Configuration
public class MuleTransportServiceTestConfiguration {

    /**
     * Transport service, Mule implementation.
     * In this test configuration, the transport service is not started
     * and stopped automatically.
     *
     * @return Service instance.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TransportService transportService() {
        final MuleTransportService theService = new MuleTransportService();
        theService.setConnectorsResourcesLocationPattern(muleTransportServiceConfigLocations());

        return theService;
    }

    /**
     * Override.
     * Location of connector and transport service configuration files
     * for the Mule implementation of the transport service.<br/>
     * Test configuration.
     *
     * @return List of locations where the Mule transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> muleTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<>();
        theLocationsList.add("classpath:connectors/mule/file-connectors.xml");
        theLocationsList.add("classpath:connectors/mule/jms-connector.xml");
        theLocationsList.add("classpath*:transport-service-configurations/*.xml");

        return theLocationsList;
    }
}
