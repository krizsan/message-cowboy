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
package se.ivankrizsan.messagecowboy.integrationtest.camel;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import se.ivankrizsan.messagecowboy.MessageCowboyConfiguration;
import se.ivankrizsan.messagecowboy.testconfig.PersistenceTestConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Spring configuration class for the {@code CamelSuccessfulOneTaskTest}
 * integration test.
 *
 * @author Petter Nordlander
 */
@Configuration
@Import({ MessageCowboyConfiguration.class, PersistenceTestConfiguration.class })
public class CamelTestSuccessfulOneTaskConfiguration {

    /**
     * Override.
     * Location of connector and transport service configuration files
     * for the Camel implementation of the transport service.<br/>
     * Do not include the file containing JMS connector(s), since
     * the tests will use an embedded ActiveMQ broker with an associated
     * JMS connector defined in a special file.
     *
     * @return List of locations where the Camel transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> camelTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<>();
        theLocationsList.add("classpath:connectors/camel/file-connectors.xml");
        return theLocationsList;
    }
}
