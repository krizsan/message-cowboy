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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

import java.io.File;
import java.util.Date;

/**
 * Integration test testing startup, scheduling and execution of one task which
 * configuration is modified between startup and the execution of the task
 * with the Mule transport service.
 *
 * @author Ivan Krizsan
 */
@Features("Mule")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MuleTaskConfigRefreshTestConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class MuleTaskConfigRefreshTest extends AbstractTestBaseClass {
    /* Constant(s): */
    private final static String TEST_TASK_CONFIG_NAME = "FileToFileTwo";

    /* Instance variable(s): */
    @Autowired
    private TaskConfigurationService mTaskConfigurationService;
    @Autowired
    private MessageCowboyStarterService mMessageCowboyService;

    /**
     * Performs preparations before each test.
     *
     * @throws Exception If error occurs.
     */
    @Before
    public void setUp() throws Exception {
        createTestDestinationDirectory();
        createTestFileWithContent();

        /* Create endpoint URIs for the destination directory. */
        final String theDestDirPath =
            mTestDestinationDirectory.getAbsolutePath().replaceAll("\\" + File.separator, "/");

        /* Original configuration contains a non-existing inbound directory. */
        final String theInboundFileEndpointUri = "file://no-such-directory/?connector=nonStreamingFileConnectorInbound";
        final String theOutboundFileEndpointUri =
            "file://" + theDestDirPath + "?connector=nonStreamingFileConnectorOutbound";

        /* Insert task configuration into database. */
        MessageCowboySchedulableTaskConfig theTask = new MessageCowboySchedulableTaskConfig();
        theTask.setName(TEST_TASK_CONFIG_NAME);
        theTask.setTaskGroupName("TestTasksGroup");
        theTask.setCronExpression("* * * * * ?");
        /* Task start date: Two seconds from now. */
        final Date theStartDate = new Date(System.currentTimeMillis() + 2000L);
        theTask.setStartDate(theStartDate);
        theTask.setInboundEndpointURI(theInboundFileEndpointUri);
        theTask.setOutboundEndpoint(theOutboundFileEndpointUri);
        theTask.setTaskEnabledFlag(true);

        mTaskConfigurationService.save(theTask);

        mMessageCowboyService.scheduleTasks();
    }

    /**
     * Tests modifying the task configuration before moving the file is
     * performed.
     *
     * @throws Exception If error occurs during test. Indicates test failure.
     */
    @Test
    @Description("Tests modification of a scheduled task that uses Mule")
    public void testModifyTaskConfigurationBeforeFileMove() throws Exception {
        /* Modify the task configuration before task is executed. */
        final MessageCowboySchedulableTaskConfig theTaskConfigToModify =
            mTaskConfigurationService.find(TEST_TASK_CONFIG_NAME);
        final String theInputDirPath = mTestFile.getAbsolutePath().replaceAll("\\" + File.separator, "/");
        final String theInboundFileEndpointUri =
            "file://" + theInputDirPath + "?connector=nonStreamingFileConnectorInbound";
        theTaskConfigToModify.setInboundEndpointURI(theInboundFileEndpointUri);
        mTaskConfigurationService.save(theTaskConfigToModify);

        /* Simulate scheduled refresh of tasks. */
        mMessageCowboyService.scheduleTasks();

        /* Just need to wait for the task to execute as scheduled. */
        delay(2000L);

        verifySuccessfulFileMove();
    }
}
