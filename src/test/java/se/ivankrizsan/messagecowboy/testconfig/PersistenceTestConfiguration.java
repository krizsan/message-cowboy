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
package se.ivankrizsan.messagecowboy.testconfig;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.mule.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring configuration for persistence data source used in tests.
 * 
 * @author Ivan Krizsan
 */
@Configuration
public class PersistenceTestConfiguration {

    /**
     * HSQLDB data source for the in-memory test database.
     */
    @Bean(destroyMethod = "shutdown")
    public DataSource dataSource() {
        final DataSource theDataSource =
            new EmbeddedDatabaseBuilder().setName(UUID.getUUID()).build();
        return theDataSource;
    }

    /**
     * JPA entity manager factory bean.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        /* JPA entity manager factory. */
        final LocalContainerEntityManagerFactoryBean theJpaEntityManagerFactory =
            new LocalContainerEntityManagerFactoryBean();
        theJpaEntityManagerFactory.setDataSource(dataSource());
        theJpaEntityManagerFactory.setPersistenceUnitName("message-cowboy");
        theJpaEntityManagerFactory.setJpaProperties(jpaProperties());

        /* JPA vendor adapter. */
        final EclipseLinkJpaVendorAdapter theJpaVendorAdapter =
            new EclipseLinkJpaVendorAdapter();
        theJpaVendorAdapter.setShowSql(true);

        theJpaEntityManagerFactory.setJpaVendorAdapter(theJpaVendorAdapter);

        return theJpaEntityManagerFactory;
    }

    /**
     * JPA transaction manager bean.
     * 
     * @param inEntityManagerFactory Supplied by container using the JPA entity
     * manager factory bean defined above.
     */
    @Bean
    public PlatformTransactionManager transactionManager(
        final EntityManagerFactory inEntityManagerFactory) {
        JpaTransactionManager theTransactionManager =
            new JpaTransactionManager();
        theTransactionManager.setEntityManagerFactory(inEntityManagerFactory);

        return theTransactionManager;
    }

    /**
     * Additional JPA properties.
     */
    Properties jpaProperties() {
        final Properties theJpaProperties = new Properties();
        theJpaProperties.setProperty("eclipselink.ddl-generation",
            "create-tables");
        theJpaProperties.setProperty("eclipselink.ddl-generation.output-mode",
            "database");
        theJpaProperties.setProperty("eclipselink.logging.level", "INFO");
        theJpaProperties.setProperty("eclipselink.weaving", "false");

        return theJpaProperties;
    }
}