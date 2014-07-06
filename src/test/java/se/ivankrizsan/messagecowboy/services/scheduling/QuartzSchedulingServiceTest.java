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
package se.ivankrizsan.messagecowboy.services.scheduling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
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
import se.ivankrizsan.messagecowboy.domain.valueobjects.TaskKey;
import se.ivankrizsan.messagecowboy.services.scheduling.helpers.JPATestMoverTask;
import se.ivankrizsan.messagecowboy.services.scheduling.helpers.QuartzTestTaskJob;

/**
 * Tests the {@code QuartzSchedulingService} class.
 *
 * @author Ivan Krizsan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { SchedulingServiceConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class QuartzSchedulingServiceTest {
    /* Constant(s): */
    private final static String TASK_NAME = "QuartzMuleTaskJobTest";
    private final static String TASK_GROUP_NAME = "TestGroup";

    /* Instance variable(s): */
    private MessageCowboySchedulableTaskConfig mTestTask;
    private Map<String, Object> mJobDataMap;
    @Autowired
    private QuartzSchedulingService mSchedulingService;

    @Before
    public void setUp() throws Exception {

        mTestTask = new JPATestMoverTask();
        mTestTask.setName(TASK_NAME);
        mTestTask.setCronExpression("* * * * * ?");
        mTestTask.setInboundTimeout(0);
        mTestTask.setTaskGroupName(TASK_GROUP_NAME);
        mTestTask.setTaskEnabledFlag(true);

        mJobDataMap = new HashMap<String, Object>();
        /*
         * Insert reference to map that is to receive invocation count and invocation times.
         * This map is reused, since Quartz will only copy the contents when creating a job.
         */
        mJobDataMap.put(QuartzTestTaskJob.TEST_MAP_KEY, mJobDataMap);
    }

    /**
     * Cleans up after each test by unscheduling all tasks.
     */
    @After
    public void cleanUp() {
        mSchedulingService.unscheduleAllTasks();
    }

    /**
     * Tests scheduling a task.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testScheduleTask() throws Exception {
        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        Thread.sleep(1500);

        final Integer theTaskInvocationCount =
            (Integer) mJobDataMap.get(QuartzTestTaskJob.INVOCATION_COUNTER_KEY);

        Assert.assertTrue("Task should have been executed at least once",
            theTaskInvocationCount.intValue() >= 1);
    }

    /**
     * Tests scheduling a disabled task.
     * The task should not be scheduled and thus never executed.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testScheduleDisabledTask() throws Exception {
        mTestTask.setTaskEnabledFlag(false);
        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        Thread.sleep(1500);

        final Integer theTaskInvocationCount =
            (Integer) mJobDataMap.get(QuartzTestTaskJob.INVOCATION_COUNTER_KEY);

        Assert.assertNull(
            "Task should never have executed and no counter exist",
            theTaskInvocationCount);
    }

    /**
     * Tests retrieval of the job data map of a task.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testRetrieveJobDataMap() throws Exception {
        Map<String, Object> theJobDataMap;
        Integer theTaskInvocationCount;

        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        /* Check that there is a job data map while the task is running. */
        Thread.sleep(1500);
        theJobDataMap =
            mSchedulingService.findJobDataMap(TASK_GROUP_NAME, TASK_NAME);
        Assert.assertNotNull(theJobDataMap);
        theTaskInvocationCount =
            (Integer) mJobDataMap.get(QuartzTestTaskJob.INVOCATION_COUNTER_KEY);
        Assert.assertNotNull("The task should have been invoked at least once",
            theTaskInvocationCount);

        mSchedulingService.unscheduleTask(TASK_GROUP_NAME, TASK_NAME);

        /* Check that there is no job data map after the task has been unscheduled. */
        Thread.sleep(1500);
        theJobDataMap =
            mSchedulingService.findJobDataMap(TASK_GROUP_NAME, TASK_NAME);
        Assert.assertNull(theJobDataMap);

        /* Check that the task was executed. */
        theTaskInvocationCount =
            (Integer) mJobDataMap.get(QuartzTestTaskJob.INVOCATION_COUNTER_KEY);
        Assert.assertTrue("Task should have been executed at least once",
            theTaskInvocationCount.intValue() >= 1);
    }

    /**
     * Tests unscheduling of a task.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testUnscheduleTask() throws Exception {
        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        Thread.sleep(100);

        boolean theUnscheduledFlag =
            mSchedulingService.unscheduleTask(TASK_GROUP_NAME, TASK_NAME);

        Thread.sleep(100);

        Assert.assertTrue("Task should have been successfully unscheduled",
            theUnscheduledFlag);

        /*
         * Trying to unschedule the same task again should indicate that
         * no task has been unscheduled.
         */
        theUnscheduledFlag =
            mSchedulingService.unscheduleTask(TASK_GROUP_NAME, TASK_NAME);
        Assert.assertFalse("Task should already have been unscheduled",
            theUnscheduledFlag);
    }

    /**
     * Tests unscheduling of all tasks.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testUnscheduleAllTasks() throws Exception {
        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        Thread.sleep(100);

        mSchedulingService.unscheduleAllTasks();

        Thread.sleep(100);

        /*
         * Trying to unschedule the same task again should indicate that
         * no task has been unscheduled.
         */
        final boolean theUnscheduledFlag =
            mSchedulingService.unscheduleTask(TASK_GROUP_NAME, TASK_NAME);
        Assert.assertFalse("Task should already have been unscheduled",
            theUnscheduledFlag);
    }

    /**
     * Tests unscheduling of other tasks, that is tasks which group and
     * name has not been specified.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testUnscheduleOtherTask() throws Exception {
        mSchedulingService.scheduleTask(mTestTask, mJobDataMap);

        Thread.sleep(100);

        /* Create list of tasks not to unschedule. */
        List<TaskKey> theTaskList = new ArrayList<TaskKey>();
        final TaskKey theTaskKey = new TaskKey(TASK_GROUP_NAME, TASK_NAME);
        theTaskList.add(theTaskKey);

        mSchedulingService.unscheduleOtherTasks(theTaskList);

        Thread.sleep(100);

        /*
         * Trying to unschedule a task that was to be excluded when
         * unscheduling tasks earlier should indicate that the task
         * has now been unscheduled successfully.
         */
        final boolean theUnscheduledFlag =
            mSchedulingService.unscheduleTask(TASK_GROUP_NAME, TASK_NAME);
        Assert.assertTrue("Task should not have been unscheduled earlier",
            theUnscheduledFlag);
    }

    /**
     * Tests scheduling a task that invokes a method.
     *
     * @throws Exception If error occurs. Indicates test failure.
     */
    @Test
    public void testScheduleMethodInvokingTask() throws Exception {
        mSchedulingService.scheduleMethodInvocation(
            this, "methodToBeInvoked", null, "* * * * * ?", TASK_NAME, TASK_GROUP_NAME);

        Thread.sleep(1500);

        final Integer theTaskInvocationCount =
            (Integer) mJobDataMap.get(QuartzTestTaskJob.INVOCATION_COUNTER_KEY);

        Assert.assertTrue("Task should have been executed at least once",
            theTaskInvocationCount.intValue() >= 1);
    }

    /**
     * Method that is to be invoked by scheduling service as part of the
     * test "testScheduleMethodInvokingTask".
     */
    public void methodToBeInvoked() {
        mJobDataMap.put(QuartzTestTaskJob.INVOCATION_COUNTER_KEY, Integer.valueOf(1));
    }
}
