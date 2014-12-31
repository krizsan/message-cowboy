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
package se.ivankrizsan.messagecowboy.integrationtest.mule;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusError;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatusSuccess;
import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

/**
 * Integration test testing startup, scheduling and execution of one task
 * with the Mule transport service.
 * The task is expected to move one single file without any errors occurring.
 *
 * @author Ivan Krizsan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MuleSuccessfulOneTaskTestConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class MuleSuccessfulOneTaskTest extends AbstractTestBaseClass {
    /* Constant(s): */
    private static final String EXISTING_STATUS_MESSAGE = "An old error";

    /* Instance variable(s): */
    @Autowired
    private TaskConfigurationService mTaskConfigurationService;
    @Autowired
    private MessageCowboyStarterService mMessageCowboyService;
    private String mTestTaskName;

    /**
     * Performs preparations before each test.
     *
     * @throws Exception If error occurs.
     */
    @Before
    public void setUp() throws Exception {
        createTestDestinationDirectory();
        createTestFileWithContent();

        /* Create endpoint URIs for the destination and source test directories. */
        final String theInputDirPath = mTestFile.getAbsolutePath().replaceAll("\\" + File.separator, "/");
        final String theDestDirPath =
            mTestDestinationDirectory.getAbsolutePath().replaceAll("\\" + File.separator, "/");

        final String theInboundFileEndpointUri =
            "file://" + theInputDirPath + "?connector=nonStreamingFileConnectorInbound";
        final String theOutboundFileEndpointUri =
            "file://" + theDestDirPath + "?connector=nonStreamingFileConnectorOutbound";

        /* Insert task configuration into database. */
        MessageCowboySchedulableTaskConfig theTask = createOneTaskConfiguration();
        theTask.setCronExpression("* * * * * ?");
        theTask.setInboundEndpointURI(theInboundFileEndpointUri);
        theTask.setOutboundEndpoint(theOutboundFileEndpointUri);
        theTask.setTaskEnabledFlag(true);
        mTestTaskName = theTask.getName();

        /* Insert a previous execution status. */
        final TaskExecutionStatus theTaskExecutionStatus =
            new TaskExecutionStatusError(theTask, EXISTING_STATUS_MESSAGE, new Date());
        theTask.addTaskExecutionStatus(theTaskExecutionStatus);

        mTaskConfigurationService.save(theTask);

        mMessageCowboyService.scheduleTasks();
    }

    /**
     * Tests the successful completion of a scheduled job that is to move a
     * file from one directory to another.
     *
     * @throws Exception If error occurs during test. Indicates test failure.
     */
    @Test
    public void testSuccessfulFileMove() throws Exception {
        /* Just need to wait for the task to execute as scheduled. */
        delay(1500L);

        verifySuccessfulFileMove();

        final MessageCowboySchedulableTaskConfig theTask = mTaskConfigurationService.find(mTestTaskName);

        final List<TaskExecutionStatus> theTaskExecutionStatuses = theTask.getTaskExecutionStatuses();

        /* Verify that at least one execution status has been added. */
        Assert.assertTrue("Task execution should have generated a status", theTaskExecutionStatuses.size() >= 2);

        /* Verify that the first execution status is the one inserted by the test. */
        Assert.assertTrue("Old execution status should have been retained",
            (theTaskExecutionStatuses.get(0) instanceof TaskExecutionStatusError));
        Assert.assertEquals("Old execution status should have been retained", EXISTING_STATUS_MESSAGE,
            theTaskExecutionStatuses.get(0).getStatusMessage());

        /* Verify that that there is one successful task execution. */
        int theSuccessCount = 0;
        for (TaskExecutionStatus theTaskExecutionStatus : theTaskExecutionStatuses) {
            if (theTaskExecutionStatus instanceof TaskExecutionStatusSuccess) {
                theSuccessCount++;
            }
        }
        Assert.assertTrue("One task execution should have succeeded", theSuccessCount == 1);
    }
}
