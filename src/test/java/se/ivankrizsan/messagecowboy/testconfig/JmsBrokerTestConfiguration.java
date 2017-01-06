package se.ivankrizsan.messagecowboy.testconfig;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.jms.ConnectionFactory;

/**
 * Configuration of embedded ActiveMQ broker used in tests.
 *
 * @author Ivan Krizsan
 */
@Configuration
public class JmsBrokerTestConfiguration {
    /* Constant(s): */

    /* Configuration parameter(s): */
    protected String mJmsBrokerUrl = "vm://0.0.0.0";

    /**
     * Connection factory for the embedded ActiveMQ broker used in tests.
     *
     * @return JMS connection factory bean.
     */
    @Bean
    @DependsOn("embeddedTestActiveMqBroker")
    public ConnectionFactory amqConnectionFactory(
        final RedeliveryPolicy inRedeliveryPolicy) {
        final ActiveMQConnectionFactory theConnectionFactory =
            new ActiveMQConnectionFactory(mJmsBrokerUrl);
        theConnectionFactory.setRedeliveryPolicy(inRedeliveryPolicy);

        return theConnectionFactory;
    }

    /**
     * ActiveMQ redelivery policy.
     *
     * @return ActiveMQ redelivery policy bean.
     */
    @Bean
    public RedeliveryPolicy amqRedeliveryPolicy() {
        final RedeliveryPolicy theRedeliveryPolicy = new RedeliveryPolicy();
        theRedeliveryPolicy.setMaximumRedeliveries(0);
        return theRedeliveryPolicy;
    }

    /**
     * Embedded ActiveMQ broker for tests.
     *
     * @return Embedded ActiveMQ broker bean.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService embeddedTestActiveMqBroker() {
        BrokerService theAmqBroker;

        try {
            theAmqBroker = new BrokerService();
            theAmqBroker.setUseJmx(true);
            theAmqBroker.setPersistent(false);
            theAmqBroker.setUseShutdownHook(true);

            /* Limit broker memory usage during tests. */
            final MemoryUsage theAmqMemoryUsage = new MemoryUsage();
            theAmqMemoryUsage.setPercentOfJvmHeap(20);
            final SystemUsage theAmqSystemUsage = new SystemUsage();
            theAmqSystemUsage.setMemoryUsage(theAmqMemoryUsage);
            theAmqBroker.setSystemUsage(theAmqSystemUsage);
        } catch (final Exception theException) {
            throw new Error("Error occurred starting embedded ActiveMQ",
                theException);
        }

        return theAmqBroker;
    }
}
