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
package se.ivankrizsan.messagecowboy.services.taskconfiguration;

import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.testconfig.PersistenceTestConfiguration;

/**
 * Tests the {@link JPASchedulableTaskConfigurationRepository}.
 *
 * @author Ivan Krizsan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceTestConfiguration.class,
    TaskConfigurationServiceConfiguration.class })
@TransactionConfiguration(transactionManager = "transactionManager",
    defaultRollback = true)
@Transactional
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class JPASchedulableTaskConfigurationRepositoryTest extends
    AbstractTransactionalJUnit4SpringContextTests {
    /* Constant(s): */
    private final static String ORIGINAL_CRON_EXPRESSION = "* * * * * *";
    private final static String MODIFIED_CRON_EXPRESSION = "0/15 * * * * *";

    /* Instance variable(s): */
    @Autowired
    private SchedulableTaskConfigurationRepository mRepository;
    @Autowired
    private EntityManagerFactory mJpaEntitManagerFactory;

    /**
     * Prepares for tests by inserting some data into the database.
     */
    @Before
    public void insertTestData() {
        MessageCowboySchedulableTaskConfig theTask =
            new MessageCowboySchedulableTaskConfig();
        theTask.setName("FileToFileOne");
        theTask.setTaskGroupName("TestTaskGroup");
        theTask.setCronExpression(ORIGINAL_CRON_EXPRESSION);
        theTask.setInboundEndpointURI("file://inbox1");
        theTask.setOutboundEndpoint("file://outbox1");
        theTask.setTaskEnabledFlag(true);

        mRepository.save(theTask);

        theTask = new MessageCowboySchedulableTaskConfig();
        theTask.setName("FileToFileTwo");
        theTask.setTaskGroupName("TestTaskGroup");
        theTask.setCronExpression("* * * * * *");
        theTask.setInboundEndpointURI("file://inbox2");
        theTask.setOutboundEndpoint("file://outbox2");
        theTask.setTaskEnabledFlag(false);

        mRepository.save(theTask);
    }

    /**
     * Tests retrieval of a single task configuration in the repository.
     */
    @Test
    public void testFind() {
        final MessageCowboySchedulableTaskConfig theResult =
            mRepository.findOne("FileToFileOne");
        Assert.assertNotNull(theResult);
    }

    /**
     * Tests attempt to retrieve single task configuration that does not
     * exist in the repository.
     */
    @Test
    public void testFindNonExisting() {
        final MessageCowboySchedulableTaskConfig theResult =
            mRepository.findOne("FileToFileFour");
        Assert.assertNull(theResult);
    }

    /**
     * Tests retrieval of all task configurations in the repository.
     */
    @Test
    public void testFindAll() {
        final List<MessageCowboySchedulableTaskConfig> theResultList =
            mRepository.findAll();
        Assert.assertEquals(2, theResultList.size());
    }

    /**
     * Tests retrieval of all enabled task configurations in the repository.
     */
    @Test
    public void testFindAllEnabled() {
        final List<MessageCowboySchedulableTaskConfig> theResultList =
            mRepository.findAllEnabled();
        Assert.assertEquals(1, theResultList.size());
    }

    /**
     * Tests updating a task configuration in the repository.
     */
    @Test
    public void testUpdate() {
        final List<MessageCowboySchedulableTaskConfig> theResultList =
            mRepository.findAll();

        MessageCowboySchedulableTaskConfig theTask = theResultList.get(0);
        final String theTaskName = theTask.getName();
        theTask.setCronExpression(MODIFIED_CRON_EXPRESSION);

        mRepository.save(theTask);

        theTask = null;

        theTask = mRepository.findOne(theTaskName);

        Assert.assertNotNull(theTask);
        Assert.assertEquals("The cron expression should have been updated",
            MODIFIED_CRON_EXPRESSION, theTask.getCronExpression());
    }
}
