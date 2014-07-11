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

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.services.starter.MessageCowboyStarterService;
import se.ivankrizsan.messagecowboy.services.taskconfiguration.TaskConfigurationService;
import se.ivankrizsan.messagecowboy.testutils.AbstractTestBaseClass;

/**
 * Integration test testing startup, scheduling and execution of one task.
 * The task is expected to move one single file without any errors occurring.
 *
 * @author Ivan Krizsan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestSuccessfulOneTaskConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SuccessfulOneTaskTest extends AbstractTestBaseClass {
	/* Constant(s): */

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

		/* Create endpoint URIs for the destination and source test directories. */
		final String theInputDirPath =
				mTestFile.getAbsolutePath().replaceAll("\\" + File.separator, "/");
		final String theDestDirPath =
				mTestDestinationDirectory.getAbsolutePath().replaceAll(
						"\\" + File.separator, "/");

		final String theInboundFileEndpointUri =
				"file://" + theInputDirPath
				+ "?connector=nonStreamingFileConnectorInbound";
		final String theOutboundFileEndpointUri =
				"file://" + theDestDirPath
				+ "?connector=nonStreamingFileConnectorOutbound";

		/* Insert task configuration into database. */
		MessageCowboySchedulableTaskConfig theTask =
				new MessageCowboySchedulableTaskConfig();
		theTask.setName("FileToFileOne");
		theTask.setTaskGroupName("TestTasksGroup");
		theTask.setCronExpression("* * * * * ?");
		theTask.setInboundEndpointURI(theInboundFileEndpointUri);
		theTask.setOutboundEndpoint(theOutboundFileEndpointUri);
		theTask.setTaskEnabledFlag(true);

		mTaskConfigurationService.save(theTask);

		mMessageCowboyService.scheduleTasks();
	}

	/**
	 * Cleans up after each test.
	 * 
	 * @throws Exception If error occurs.
	 */
	@After
	public void cleanUp() throws Exception {
		deleteTestFile();
		deleteTestDestinationDirectory();
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
		try {
			Thread.sleep(1500);
		} catch (final InterruptedException theException) {
			theException.printStackTrace();
		}

		verifySuccessfulFileMove();
	}
}
