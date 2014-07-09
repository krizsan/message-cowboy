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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;

/**
 * Implements test of the {@code TaskConfigurationServiceImpl} class.
 * 
 * @author Ivan Krizsan
 */
public class TaskConfigurationServiceTest {
    /* Constant(s): */

    /* Instance variable(s): */
    private TaskConfigurationService mServiceUnderTest;

    /**
     * Sets up before each test by creating an instance of the service under
     * test and a mock repository.
     */
    @Before
    public void setUp() {
        final TaskConfigurationServiceImpl theServiceUnderTest =
            new TaskConfigurationServiceImpl();
        /* Create enabled task configuration mock. */
        final MessageCowboySchedulableTaskConfig theEnabledTaskConfig =
            Mockito.mock(MessageCowboySchedulableTaskConfig.class);
        Mockito.when(theEnabledTaskConfig.getTaskEnabledFlag())
            .thenReturn(true);

        /* Create disabled task configuration mock. */
        final MessageCowboySchedulableTaskConfig theDisabledTaskConfig =
            Mockito.mock(MessageCowboySchedulableTaskConfig.class);
        Mockito.when(theDisabledTaskConfig.getTaskEnabledFlag()).thenReturn(
            false);

        /* List holding enabled and disabled task configurations. */
        final List<MessageCowboySchedulableTaskConfig> theAllTaskConfigs =
            new ArrayList<MessageCowboySchedulableTaskConfig>();
        theAllTaskConfigs.add(theDisabledTaskConfig);
        theAllTaskConfigs.add(theEnabledTaskConfig);

        /* List holding enabled task configurations. */
        final List<MessageCowboySchedulableTaskConfig> theEnabledTaskConfigs =
            new ArrayList<MessageCowboySchedulableTaskConfig>();
        theEnabledTaskConfigs.add(theEnabledTaskConfig);

        /* Create and configure mock repository. */
        final SchedulableTaskConfigurationRepository theMockRepository =
            Mockito.mock(SchedulableTaskConfigurationRepository.class);
        /* findAll returns enabled and disabled task configurations. */
        Mockito.when(theMockRepository.findAll()).thenReturn(theAllTaskConfigs);
        /* findAllEnabled returns only enabled task configurations. */
        Mockito.when(theMockRepository.findAllEnabled()).thenReturn(
            theEnabledTaskConfigs);
        /* find returns null regardless of argument. */
        Mockito.when(theMockRepository.findOne(Mockito.anyString()))
            .thenReturn(null);

        theServiceUnderTest.setTaskConfigurationRepository(theMockRepository);
        mServiceUnderTest = theServiceUnderTest;
    }

    /**
     * Tests successful retrieval of all task configurations.
     */
    @Test
    public void testFindAll() {
        final List<MessageCowboySchedulableTaskConfig> theResultList =
            mServiceUnderTest.findAll();
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
}
