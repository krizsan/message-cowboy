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
package se.ivankrizsan.messagecowboy.integrationtest.taskexecutionstatuscleanup;

import org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.annotation.*;
import se.ivankrizsan.messagecowboy.MessageCowboyConfiguration;
import se.ivankrizsan.messagecowboy.testconfig.PersistenceTestConfiguration;
import se.ivankrizsan.messagecowboy.testutils.InvocationLoggerMethodInterceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Spring configuration class for the {@code TaskExecutionStatusCleanupTest}
 * integration test.
 *
 * @author Ivan Krizsan
 */
@Configuration
@Import({MessageCowboyConfiguration.class, PersistenceTestConfiguration.class})
public class TaskExecutionStatusCleanupTestConfiguration {

    /**
     * Override.
     * Location of connector and transport service configuration files
     * for the Mule implementation of the transport service.<br/>
     * Do not include the file containing JMS connector(s), since
     * the tests will use an embedded ActiveMQ broker with an associated
     * JMS connector defined in a special file.
     *
     * @return List of locations where the Mule transport service is to
     * search for configuration files.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public List<String> muleTransportServiceConfigLocations() {
        final List<String> theLocationsList = new ArrayList<>();
        theLocationsList.add("classpath:connectors/mule/file-connectors.xml");
        return theLocationsList;
    }

    @Bean
    public InvocationLoggerMethodInterceptor invocationLoggerInterceptor() {
        final InvocationLoggerMethodInterceptor theInterceptor = new InvocationLoggerMethodInterceptor();
        return theInterceptor;
    }

    /**
     * Creates a proxy for the task execution status service that logs method invocation on the service.
     *
     * @return Task execution status service proxy creator.
     */
    @Bean
    public BeanNameAutoProxyCreator taskExecutionStatusServiceProxyCreator() {
        final BeanNameAutoProxyCreator theProxyCreator = new BeanNameAutoProxyCreator();
        final String[] theProxiedBeanNames = new String[1];
        theProxiedBeanNames[0] = "taskExecutionStatusService";
        theProxyCreator.setBeanNames(theProxiedBeanNames);

        final String[] theInterceptorNames = new String[1];
        theInterceptorNames[0] = "invocationLoggerInterceptor";
        theProxyCreator.setInterceptorNames(theInterceptorNames);

        return theProxyCreator;
    }

    /**
     * Overrides properties configured on beans.
     */
    @Bean()
    @Lazy(false)
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public static BeanFactoryPostProcessor propertyOverrideConfigurer() {
        PropertyOverrideConfigurer theOverrideConfigurer = new PropertyOverrideConfigurer();

        final Properties thePropertiesHolder = new Properties();
        /* Task refresh interval. */
        thePropertiesHolder.put("starterService.taskReschedulingCronExpression", "* 4/30 * * * ?");
        /* Transport service configuration refresh interval. */
        thePropertiesHolder.put("starterService.transportServiceConfigurationRefreshCronExpression", "* 5/30 * * * ?");
        /* Task execution status reports cleanup interval. */
        thePropertiesHolder.put("starterService.taskExecutionStatusCleanupCronExpression", "0/5 * * * * ?");

        theOverrideConfigurer.setProperties(thePropertiesHolder);
        theOverrideConfigurer.setIgnoreInvalidKeys(false);
        theOverrideConfigurer.setIgnoreResourceNotFound(false);
        theOverrideConfigurer.setOrder(0);
        return theOverrideConfigurer;
    }
}
