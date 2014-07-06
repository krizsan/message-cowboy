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
package se.ivankrizsan.messagecowboy.services.scheduling.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import se.ivankrizsan.messagecowboy.domain.entities.TaskJob;

/**
 * Quartz task job class used for testing purposes.<br/>
 * When a job is executed, a counter in the job data map is incremented
 * and a {@code Date} representing the invocation time is appended to a list
 * of invocation times.
 * The list of invocation times is also fetched from the job data map.
 * Both the counter and the list are inserted in the job data map if not
 * present.
 *
 * @author Ivan Krizsan
 */
public class QuartzTestTaskJob implements TaskJob, Job {
    /* Constant(s): */
    public final static String TEST_MAP_KEY = "testMap";
    public final static String INVOCATION_COUNTER_KEY = "counterKey";
    public final static String INVOCATION_TIME_LIST = "invocationTimeList";

    @Override
    public void execute(
        final JobExecutionContext inContext) throws JobExecutionException {
        final JobDataMap theJobDataMap =
            inContext.getJobDetail().getJobDataMap();
        if (theJobDataMap.containsKey(TEST_MAP_KEY)) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> theTestDataMap =
                (Map<String, Object>) theJobDataMap.get(TEST_MAP_KEY);

            incrementInvocationCounter(theTestDataMap);

            insertInvocationTime(theTestDataMap);
        }
    }

    private void insertInvocationTime(
        final Map<String, Object> inTestDataMap) {
        if (inTestDataMap.containsKey(INVOCATION_TIME_LIST)) {
            @SuppressWarnings("unchecked")
            final List<Date> theDateList =
                (List<Date>) inTestDataMap.get(INVOCATION_TIME_LIST);
            theDateList.add(new Date());
        } else {
            final List<Date> theDateList = new ArrayList<Date>();
            theDateList.add(new Date());
            inTestDataMap.put(INVOCATION_TIME_LIST, theDateList);
        }
    }

    private void incrementInvocationCounter(
        final Map<String, Object> inTestDataMap) {
        if (inTestDataMap.containsKey(INVOCATION_COUNTER_KEY)) {
            final Integer theCounter =
                (Integer) inTestDataMap.get(INVOCATION_COUNTER_KEY);
            inTestDataMap.put(INVOCATION_COUNTER_KEY,
                Integer.valueOf(theCounter + 1));
        } else {
            inTestDataMap.put(INVOCATION_COUNTER_KEY, Integer.valueOf(1));
        }
    }
}
