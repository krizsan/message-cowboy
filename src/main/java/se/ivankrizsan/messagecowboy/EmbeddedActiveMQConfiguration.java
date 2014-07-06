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

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * Spring configuration for an embedded ActiveMQ JMS broker in Message Cowboy.
 * 
 * @author Ivan Krizsan
 */
public class EmbeddedActiveMQConfiguration {
    /* Property value(s): */
    @Value("${ACTIVEMQ_USE_EMBEDDED_FLAG}")
    private boolean useEmbeddedActiveMqFlag;
    @Value("${ACTIVEMQ_URI}")
    private String activeMqUri;
    @Value("${ACTIVEMQ_JMX_ENABLED_FLAG}")
    private boolean activeMqJmxEnabledFlag;
    @Value("${ACTIVEMQ_PERSISTENCE_ENABLED_FLAG}")
    private boolean activeMqPersistenceEnabledFlag;

    /*
     * Starts an embedded ActiveMQ JMS broker if enabled in configuration
     * properties.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService embeddedActiveMqBroker() {
        BrokerService theActiveMqBroker = null;

        if (useEmbeddedActiveMqFlag) {
            try {
                theActiveMqBroker = new BrokerService();
                theActiveMqBroker.addConnector(activeMqUri);
                theActiveMqBroker.setUseJmx(activeMqJmxEnabledFlag);
                theActiveMqBroker.setPersistent(activeMqPersistenceEnabledFlag);
                theActiveMqBroker.setRestartAllowed(true);
                theActiveMqBroker.setUseShutdownHook(true);
                theActiveMqBroker.setStartAsync(false);
                theActiveMqBroker
                    .setDestinationPolicy(activeMqDestinationPolicyMap());
            } catch (Exception theException) {
                throw new Error(
                    "Error occurred starting embedded ActiveMQ broker",
                    theException);
            }
        }

        return theActiveMqBroker;
    }

    /*
     * ActiveMQ destination policy map.
     */
    @Bean
    public PolicyMap activeMqDestinationPolicyMap() {
        final PolicyMap theDestinationPolicyMap = new PolicyMap();
        final List<PolicyEntry> thePolicyEntries = new ArrayList<PolicyEntry>();

        PolicyEntry thePolicyEntry = new PolicyEntry();
        thePolicyEntry.setQueue(">");
        thePolicyEntry.setProducerFlowControl(true);
        /* Memory limit: 1 MB */
        thePolicyEntry.setMemoryLimit(1048576L);
        /* 
         * Dead letter strategy for the policy with individual
         * dead-letter-queues for each destination.
         */
        final IndividualDeadLetterStrategy theDeadLetterStrategy =
            new IndividualDeadLetterStrategy();
        theDeadLetterStrategy.setQueuePrefix("DLQ.");

        thePolicyEntry.setDeadLetterStrategy(theDeadLetterStrategy);

        thePolicyEntries.add(thePolicyEntry);

        theDestinationPolicyMap.setPolicyEntries(thePolicyEntries);
        return theDestinationPolicyMap;
    }
}
