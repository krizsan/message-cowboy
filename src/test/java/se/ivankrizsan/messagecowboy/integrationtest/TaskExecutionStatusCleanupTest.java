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
package se.ivankrizsan.messagecowboy.integrationtest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.services.taskexecutionstatus.TaskExecutionStatusService;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

/**
 * Tests task execution status cleanup scheduling.
 *
 * @author Ivan Krizsan
 */
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

    /**
     * Tests the scheduling of task execution status cleanup.
     *
     * @throws Exception If error occurs during test. Indicates test failure.
     */
    @Test
    public void testTaskExecutionStatusCleanupScheduling() throws Exception {
        mMessageCowboyService.scheduleTasks();
        /* Just need to wait for the task to execute as scheduled. */
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException theException) {
            theException.printStackTrace();
        }

        /* Verify that task execution status cleanup service has been called once. */
        Mockito.verify(mTaskExecutionStatusService, Mockito.times(1)).deleteIfOlderThanDays(Mockito.anyInt());
        ;
    }

}
