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
package se.ivankrizsan.messagecowboy.testutils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.ivankrizsan.messagecowboy.domain.entities.impl.MessageCowboySchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskExecutionStatus;
import se.ivankrizsan.messagecowboy.domain.valueobjects.TransportProperty;

/**
 * Abstract base-class containing methods and data used in more than one test.
 * Also implements the output of the test-class and test-method names
 * prior to, and after, execution of each test-method.
 *
 * @author Ivan Krizsan
 */
public abstract class AbstractTestBaseClass {
    /* Constant(s): */
    static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestBaseClass.class);
    /** Contents of test-file. */
    protected final static String TEST_FILE_CONTENTS = "Some test file contents 1234 åäö";
    protected final static String METHOD_HEADER_LINE = "*************************************************"
        + "*************************************************";

    /* Instance variable(s): */
    /** Source file that can be requested. */
    protected File mTestFile;
    /** Destination directory to which files can be dispatched. */
    protected File mTestDestinationDirectory;
    /** Name of test method. JUnit require this instance variable to be public. */
    @Rule
    public TestName mJunitTestMethodName = new TestName();

    /**
     * Logs the name of the test-class and test-method before a test-method
     * is executed.
     */
    @Before
    public void printTestHeaderBefore() {
        final StringBuffer theLogMessage = new StringBuffer();
        theLogMessage.append("\n\n");
        theLogMessage.append(METHOD_HEADER_LINE);
        theLogMessage.append("\n");
        theLogMessage.append("* Running test: ");
        theLogMessage.append(this.getClass().getSimpleName());
        theLogMessage.append("::");
        theLogMessage.append(mJunitTestMethodName.getMethodName());
        theLogMessage.append("\n");
        theLogMessage.append(METHOD_HEADER_LINE);
        LOGGER.info(theLogMessage.toString());
    }

    /**
     * Logs the name of the test-class and test-method after a test-method
     * has finished executing.
     */
    @After
    public void printTestHeaderAfter() {
        final StringBuffer theLogMessage = new StringBuffer();
        theLogMessage.append("\n\n");
        theLogMessage.append(METHOD_HEADER_LINE);
        theLogMessage.append("\n");
        theLogMessage.append("* Test ended: ");
        theLogMessage.append(this.getClass().getSimpleName());
        theLogMessage.append("::");
        theLogMessage.append(mJunitTestMethodName.getMethodName());
        theLogMessage.append("\n");
        theLogMessage.append(METHOD_HEADER_LINE);
        LOGGER.info(theLogMessage.toString());
    }

    /**
     * Creates a test destination directory.<br/>
     * Any existing directory with the same name will be deleted.
     *
     * @return Destination directory file path.
     */
    protected String createTestDestinationDirectory() {
        mTestDestinationDirectory = new File("moverTaskJobTestDestDir" + UUID.randomUUID().toString() + File.separator);

        if (mTestDestinationDirectory.exists()) {
            try {
                FileUtils.deleteDirectory(mTestDestinationDirectory);
            } catch (final IOException theException) {
                LOGGER.error("Error deleting output directory", theException);
            }
        }

        final boolean theDirCreatedFlag = mTestDestinationDirectory.mkdir();
        if (!theDirCreatedFlag) {
            throw new Error("Destination directory not created!");
        }
        final String theDestDirPath = mTestDestinationDirectory.getAbsolutePath();

        LOGGER.info("Destination directory: {}", theDestDirPath);

        return theDestDirPath;
    }

    /**
     * Deletes the test destination directory.<br/>
     * If the directory does not exist, do nothing.
     */
    protected void deleteTestDestinationDirectory() {
        if (mTestDestinationDirectory != null) {
            try {
                FileUtils.deleteDirectory(mTestDestinationDirectory);
            } catch (final IOException theException) {
                LOGGER.debug("Error deleting test destination directory");
            }
        }
    }

    /**
     * Creates a test file in a new directory and write some contents to it.
     *
     * @return Path to directory containing input file.
     * @throws IOException If error occurs creating or writing to input file.
     */
    protected String createTestFileWithContent() throws IOException {
        mTestFile = new File("testInputDir" + UUID.randomUUID().toString(), "inputfile.txt");
        final boolean theDirCreatedFlag = mTestFile.getParentFile().mkdir();
        if (!theDirCreatedFlag) {
            throw new IOException("Test-file parent directory not created");
        }
        final boolean theTestFileCreatedFlag = mTestFile.createNewFile();
        if (!theTestFileCreatedFlag) {
            throw new IOException("Test-file not created");
        }
        final String theInputDirPath = mTestFile.getParentFile().getAbsolutePath();

        final FileWriter theInputFileWriter = new FileWriter(mTestFile);
        theInputFileWriter.write(TEST_FILE_CONTENTS);
        theInputFileWriter.close();

        LOGGER.info("Test file directory: {}", mTestFile.getParentFile().getAbsolutePath());

        return theInputDirPath;
    }

    /**
     * Deletes the test file and its parent directory.<br/>
     * If the test file does not exist, do nothing.
     */
    protected void deleteTestFile() {
        if (mTestFile != null) {
            try {
                FileUtils.deleteDirectory(mTestFile.getParentFile());
            } catch (final IOException theException) {
                LOGGER.debug("Error deleting input file and directory");
            }
        }
    }

    /**
     * Verifies the moving of a file from the source to the destination directory
     * that is expected to have been completed successfully.
     *
     * @throws IOException If error occurs accessing file. Indicates failure.
     */
    protected void verifySuccessfulFileMove() throws IOException {
        /* Verify contents of source and destination directories. */
        final File[] theDestDirFiles = mTestDestinationDirectory.listFiles();
        Assert.assertEquals("There should be one file in the destination directory", 1, theDestDirFiles.length);
        Assert.assertTrue("File should not be left in source directory", !mTestFile.exists());

        /* Verify name of moved file. */
        Assert
            .assertEquals("Original file name should be preserved", mTestFile.getName(), theDestDirFiles[0].getName());

        /* Verify contents of moved file. */
        final String theMovedFileContents = FileUtils.readFileToString(theDestDirFiles[0]);
        Assert.assertEquals("Contents of moved file should be preserved", TEST_FILE_CONTENTS, theMovedFileContents);
    }

    /**
     * Delays execution supplied number of milliseconds.
     *
     * @param inDelayTimeInMilliseconds Time to delay in milliseconds.
     */
    protected void delay(final long inDelayTimeInMilliseconds) {
        try {
            Thread.sleep(inDelayTimeInMilliseconds);
        } catch (final InterruptedException theException) {
            /* Ignore exceptions. */
        }
    }

    /**
     * Creates one task configuration object and sets some basic configuration on the new
     * configuration.
     *
     * @return New task configuration object.
     */
    public static MessageCowboySchedulableTaskConfig createOneTaskConfiguration() {
        final MessageCowboySchedulableTaskConfig theTaskConfiguration = new MessageCowboySchedulableTaskConfig();
        final Date theStartDate = new Date();
        /* End date is 24 hours later than the start date. */
        final Date theEndDate = new Date(theStartDate.getTime() + 86400000L);

        theTaskConfiguration.setName("Task name " + UUID.randomUUID().toString());
        theTaskConfiguration.setTaskGroupName("Test Tasks Group");
        theTaskConfiguration.setCronExpression("* * * * * ?");
        theTaskConfiguration.setStartDate(theStartDate);
        theTaskConfiguration.setEndDate(theEndDate);
        theTaskConfiguration.setInboundEndpointURI("http://www.ivankrizsan.se/inboundendpoint");
        theTaskConfiguration.setInboundTimeout(5000L);
        theTaskConfiguration.setOutboundEndpoint("http://www.ivankrizsan.se/outboundendpoint");
        theTaskConfiguration.setTaskEnabledFlag(true);
        theTaskConfiguration.setTaskExecutionStatuses(new ArrayList<TaskExecutionStatus>());
        theTaskConfiguration.setTransportProperties(new ArrayList<TransportProperty>());

        return theTaskConfiguration;
    }
}