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

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
@ContextConfiguration(classes = { CamelTestSuccessfulOneTaskConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class CamelSuccessfulOneTaskTest extends AbstractTestBaseClass {
	/* Constant(s): */

	/* Instance variable(s): */
	@Autowired
	private TaskConfigurationService mTaskConfigurationService;
	@Autowired
	private MessageCowboyStarterService mMessageCowboyService;
	@Rule
	public TemporaryFolder mTemporaryFolder = new TemporaryFolder();
	
	/**
	 * Setup pre-test-class static settings.
	 * Sets property to choose Camel as transport service.
	 */
	@BeforeClass
	public static void staticSetup(){
		System.setProperty("messagecowboy.transport", "camel");
	}

	/**
	 * Sub sequent tests may not want the transport to be set to Camel.
	 */
	@AfterClass
	public static void staticTearDown(){
		System.clearProperty("messagecowboy.transport");
	}
	
	/**
	 * Performs preparations before each test.
	 *
	 * @throws Exception If error occurs.
	 */
	@Before
	public void setUp() throws Exception {
		mTestDestinationDirectory = mTemporaryFolder.newFolder("destination");
        File theInputFolder = mTemporaryFolder.newFolder("input");
        mTestFile = new File(theInputFolder,"inputfile.txt");
        FileUtils.writeStringToFile(mTestFile, TEST_FILE_CONTENTS);

        /* Create endpoint URIs for the destination and source test directories. */
        final String theInputDirPath = theInputFolder.getAbsolutePath();
        final String theDestDirPath = mTestDestinationDirectory.getAbsolutePath();

        /*
         * Using non standard file component prefix - to verify that
         * the component was configured from .xml file.
         */
        String theInboundFileEndpointUri = "phile://" + theInputDirPath;
        String theOutboundFileEndpointUri = "phile://" + theDestDirPath;


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
	 * Tests the successful completion of a scheduled job that is to move a
	 * file from one directory to another.
	 * 
	 * @throws Exception If error occurs during test. Indicates test failure.
	 */
	@Test
	public void testSuccessfulFileMove() throws Exception {
		/* Just need to wait for the task to execute as scheduled. */
		try {
			Thread.sleep(3000);
		} catch (final InterruptedException theException) {
			theException.printStackTrace();
		}

		verifySuccessfulFileMove();
	}
}
