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

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring configuration for persistence in Message Cowboy.
 * Includes configuration for a database data source as well as an
 * optional embedded HSQLDB database server.
 * 
 * @author Ivan Krizsan
 */
@Configuration
public class PersistenceConfiguration {
    /* Property value(s): */
    @Value("${DATASOURCE_DRIVER_CLASS_NAME}")
    private String dataSourceDriverClassName;
    @Value("${DATASOURCE_URL}")
    private String dataSourceUrl;
    @Value("${DATASOURCE_USER_NAME}")
    private String dataSourceUserName;
    @Value("${DATASOURCE_PASSWORD}")
    private String dataSourcePassword;
    @Value("${DATABASE_USE_EMBEDDED_FLAG}")
    private boolean databaseUseEmbeddedFlag;
    @Value("${DATABASE_DIRECTORY_PATH}")
    private String databaseDirectoryPath;
    @Value("${DATABASE_FILENAME}")
    private String databaseFileName;
    @Value("${DATABASE_DBNAME}")
    private String databaseDbName;
    @Value("${DATABASE_PORT}")
    private int databasePort;

    /**
     * Datasource used by the Message Cowboy application.
     */
    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource theDataSource =
            new DriverManagerDataSource();
        theDataSource.setDriverClassName(dataSourceDriverClassName);
        theDataSource.setUrl(dataSourceUrl);
        theDataSource.setUsername(dataSourceUserName);
        theDataSource.setPassword(dataSourcePassword);
        return theDataSource;
    }

    /**
     * JPA entity manager factory bean.
     * Depends on the hsqlDbServer bean, in order to create the embedded
     * database, if one is to be used, before the entity manager factory.
     */
    @Bean
    @DependsOn("hsqlDbServer")
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
     * Post-processor applying exception-translation to all repositories.
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
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

    /**
     * Embedded HSQLDB database server which can be accessed from outside
     * of the Message Cowboy.
     * Only started if the relevant configuration property set to true.
     */
    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Server hsqlDbServer() {
        Server theHsqldbServer = null;

        if (databaseUseEmbeddedFlag) {
            try {
                HsqlProperties theHsqldbProperties = new HsqlProperties();
                theHsqldbProperties.setProperty("server.database.0", "file:"
                    + databaseDirectoryPath + databaseFileName);
                theHsqldbProperties.setProperty("server.dbname.0",
                    databaseDbName);
                theHsqldbProperties.setProperty("server.port", databasePort);
                theHsqldbServer = new Server();
                theHsqldbServer.setProperties(theHsqldbProperties);

            } catch (final Exception theException) {
                throw new Error("Error starting database server", theException);
            }
        }

        return theHsqldbServer;
    }
}