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
package se.ivankrizsan.messagecowboy.integrationtest.taskexecutionstatuscleanup;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.yandex.qatools.allure.annotations.Features;
import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.services.taskexecutionstatus.TaskExecutionStatusService;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;
import se.ivankrizsan.messagecowboy.testutils.InvocationLoggerMethodInterceptor;
import se.ivankrizsan.messagecowboy.testutils.InvocationLoggerMethodInterceptor.InvocationLogEntry;

import java.util.List;

/**
 * Tests task execution status cleanup scheduling.
 * This tests originates from having observed strange behaviour of the
 * scheduling of the scheduled job in question.
 *
 * @author Ivan Krizsan
 */
@Features("Task Execution Status")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TaskExecutionStatusCleanupTestConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TaskExecutionStatusCleanupTest extends AbstractTestBaseClass {
    /* Constant(s): */

    /* Instance variable(s): */
    @Autowired
    private TaskConfigurationService mTaskConfigurationService;
    @Autowired
    private MessageCowboyStarterService mMessageCowboyService;
    @Autowired
    private TaskExecutionStatusService mTaskExecutionStatusService;
    @Autowired
    private InvocationLoggerMethodInterceptor mTaskExecutionStatusServiceInvocationLogger;

    /**
     * Tests the scheduling of task execution status cleanup.
     * The task is configured to execute with a certain interval. This test ascertains that
     * two subsequent executions of task execution status cleanup are executed with the
     * proper interval.
     *
     * @throws Exception If error occurs during test. Indicates test failure.
     */
    @Test
    public void testTaskExecutionStatusCleanupScheduling() throws Exception {
        mMessageCowboyService.scheduleTasks();
        final List<InvocationLogEntry> theInvocationLogEntries =
            mTaskExecutionStatusServiceInvocationLogger.getInvocationLogEntries();

        /* Wait until there are at least two invocations. */
        while (theInvocationLogEntries.size() < 2) {
            delay(300L);
        }

        /* Verify the invocation interval. */
        final InvocationLogEntry theInvocationLogEntryOne = theInvocationLogEntries.get(0);
        final InvocationLogEntry theInvocationLogEntryTwo = theInvocationLogEntries.get(1);
        final long theInvocationTimeDiff =
            theInvocationLogEntryTwo.getInvocationTime().getTime()
                - theInvocationLogEntryOne.getInvocationTime().getTime();

        /* Job is configured to execute with 5 second intervals*/
        Assert.assertTrue("At least 4 seconds between job executions", theInvocationTimeDiff > 4000);
    }
}
