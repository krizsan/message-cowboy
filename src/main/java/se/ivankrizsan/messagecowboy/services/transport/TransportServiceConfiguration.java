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
 * Spring configuration class for the transport service.
 * 
 * @author Ivan Krizsan
 */
@Configuration
public class TransportServiceConfiguration {

    /**
     * Transport service, Mule implementation.
     * 
     * @return Service instance.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public TransportService transportService() {
        final MuleTransportService theService = new MuleTransportService();
        theService
            .setConnectorsResourcesLocationPattern(muleTransportServiceConfigLocations());

        return theService;
    }

    /**
     * Location of connector and transport service configuration files
     * for the Mule implementation of the transport service.<br/>
     * This bean should be overridden and appropriate configuration locations
     * should be provided.
     * 
     * @return List of locations where the Mule transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> muleTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<String>();

        return theLocationsList;
    }
}
