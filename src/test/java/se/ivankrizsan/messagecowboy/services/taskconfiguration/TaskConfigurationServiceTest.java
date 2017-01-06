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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.allure.annotations.Features;
import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.testconfig.PersistenceTestConfiguration;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

import java.util.List;

/**
 * Implements test of the {@code TaskConfigurationServiceImpl} class.
 * The value of this test may be questionable, since the task configuration
 * service merely wraps the task configuration repository, which code in turn
 * is generated.
 *
 * @author Ivan Krizsan
 */
@Features("Task Configuration")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceTestConfiguration.class,
    TaskConfigurationServiceConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskConfigurationServiceTest {
    /* Constant(s): */

    /* Instance variable(s): */
    @Autowired
    private TaskConfigurationService mServiceUnderTest;
    @Autowired
    private SchedulableTaskConfigurationRepository mTaskConfigurationRepository;

    /**
     * Sets up before each test by creating an instance of the service under
     * test and a mock repository.
     */
    @Before
    public void setUp() {
        /* Create enabled task configuration. */
        final MessageCowboySchedulableTaskConfig theEnabledTaskConfig =
            AbstractTestBaseClass.createOneTaskConfiguration();
        theEnabledTaskConfig.setName("config1");
        theEnabledTaskConfig.setTaskEnabledFlag(true);
        mTaskConfigurationRepository.save(theEnabledTaskConfig);

        /* Create disabled task configuration. */
        final MessageCowboySchedulableTaskConfig theDisabledTaskConfig =
            AbstractTestBaseClass.createOneTaskConfiguration();
        theDisabledTaskConfig.setName("config2");
        theDisabledTaskConfig.setTaskEnabledFlag(false);
        mTaskConfigurationRepository.save(theDisabledTaskConfig);
    }

    /**
     * Tests successful retrieval of all task configurations.
     */
    @Test
    public void testFindAll() {
        final List<MessageCowboySchedulableTaskConfig> theResultList = mServiceUnderTest.findAll();
        Assert.assertNotNull(theResultList);
        Assert.assertEquals("All tasks expected", 2, theResultList.size());
    }

    /**
     * Tests successful retrieval of all enabled task configurations.
     */
    @Test
    public void testFindAllEnabled() {
        final List<MessageCowboySchedulableTaskConfig> theResultList =
            mServiceUnderTest.findAllEnabled();
        Assert.assertNotNull(theResultList);
        Assert.assertEquals("Only enabled tasks expected", 1, theResultList.size());
    }

    /**
     * Tests saving of a task configuration.
     */
    @Test
    public void testSave() {
        final MessageCowboySchedulableTaskConfig theSaveTaskConfig =
            AbstractTestBaseClass.createOneTaskConfiguration();
        theSaveTaskConfig.setName("I was saved!");

        final MessageCowboySchedulableTaskConfig theSavedTaskConfig =
            mServiceUnderTest.save(theSaveTaskConfig);

        /* The name is the id and should stay the same. */
        Assert.assertEquals(theSaveTaskConfig.getName(), theSavedTaskConfig.getName());
    }
}
