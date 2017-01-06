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
package se.ivankrizsan.messagecowboy.services.taskexecutionstatus;

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
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusError;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusNoMessageReceived;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusSuccess;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationServiceConfiguration;
import se.ivankrizsan.messagecowboy.testconfig.PersistenceTestConfiguration;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

import java.util.Calendar;
import java.util.List;

/**
 * Tests the {@code TaskExecutionStatusService}.
 *
 * @author Ivan Krizsan
 */
@Features("Task Execution Status")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PersistenceTestConfiguration.class, TaskExecutionStatusServiceConfiguration.class,
    TaskConfigurationServiceConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TaskExecutionStatusServiceTest {
    /* Constant(s): */

    /* Instance variable(s): */
    @Autowired
    private TaskExecutionStatusService mServiceUnderTest;
    @Autowired
    private TaskExecutionStatusRepository mTaskExecutionStatusRepository;
    @Autowired
    private TaskConfigurationService mTaskConfigurationService;
    private String mTestTaskConfigurationName;

    /**
     * Performs test preparations.
     *
     * @throws Exception If error occurs setting up for the test.
     */
    @Before
    public void setUp() throws Exception {
        Calendar theCalendar = Calendar.getInstance();
        MessageCowboySchedulableTaskConfig theTaskConfiguration = AbstractTestBaseClass.createOneTaskConfiguration();

        /* Save name in order to query for the task configuration later. */
        mTestTaskConfigurationName = theTaskConfiguration.getName();

        /* Create an execution status that is five days old. */
        theCalendar.add(Calendar.DAY_OF_YEAR, -5);
        TaskExecutionStatus theTaskExecutionStatus =
            new TaskExecutionStatusSuccess(theTaskConfiguration, "Success 1", theCalendar.getTime());
        theTaskConfiguration.addTaskExecutionStatus(theTaskExecutionStatus);

        /* Create an execution status that is two days old. */
        theCalendar = Calendar.getInstance();
        theCalendar.add(Calendar.DAY_OF_YEAR, -2);
        theTaskExecutionStatus = new TaskExecutionStatusError(theTaskConfiguration, "Failure 1", theCalendar.getTime());
        theTaskConfiguration.addTaskExecutionStatus(theTaskExecutionStatus);

        /* Create an execution status for an execution that just finished. */
        theCalendar = Calendar.getInstance();
        theTaskExecutionStatus =
            new TaskExecutionStatusNoMessageReceived(theTaskConfiguration, "No Msg Received 1", theCalendar.getTime());
        theTaskConfiguration.addTaskExecutionStatus(theTaskExecutionStatus);

        mTaskConfigurationService.save(theTaskConfiguration);
    }

    /**
     * Tests deleting task execution status entries older than a certain number of days.
     * Expected result: One entry should be deleted, two should remain.
     */
    @Test
    public void testDeleteIfOlderThanDays() {
        mServiceUnderTest.deleteIfOlderThanDays(3);
        final List<TaskExecutionStatus> theRemainingTaskExecutionStatuses = mTaskExecutionStatusRepository.findAll();

        Assert.assertEquals("One task execution status should have been removed", 2,
            theRemainingTaskExecutionStatuses.size());
    }

    /**
     * Tests deleting task execution status entries older tha a certain number of days and then querying for
     * the task configuration containing the execution status entries and verify the remaining number of entries.
     * Expected result: One entry should be deleted, two should remain.
     */
    @Test
    public void testReadTaskConfigurationWithDeletedEntries() {
        mServiceUnderTest.deleteIfOlderThanDays(3);
        final MessageCowboySchedulableTaskConfig theTaskConfig =
            mTaskConfigurationService.find(mTestTaskConfigurationName);
        final List<TaskExecutionStatus> theRemainingTaskExecutionStatuses = theTaskConfig.getTaskExecutionStatuses();

        Assert.assertEquals("One task execution status should have been removed", 2,
            theRemainingTaskExecutionStatuses.size());
    }
}
