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

import java.util.Arrays;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MethodInvoker;

import se.ivankrizsan.messagecowboy.domain.entities.TaskJob;

/**
 * Task scheduled by the Message Cowboy starter service that invokes
 * a named method on an object with zero or more parameters.<br/>
 * Method name and target object, which both are required, are passed
 * in the job data map. The array of parameters to the target method is,
 * if the target method takes parameters, passed in the job data map.
 * If the target method takes no parameters, no entry need to be specified
 * in the job data map.<br/>
 * All types of exceptions that occur when the target method is invoked are
 * caught by this job.
 *
 * @author Ivan Krizsan
 */
public class MethodInvokingJob implements TaskJob, Job {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(MethodInvokingJob.class);
    /** Key to entry in job data map to object on which method is to be invoked on. */
    public final static String TARGET_OBJECT_KEY =
        "_methodinvokingjob_targetobject";
    /** Key to entry in job data map to name of method to be invoked. */
    public final static String TARGET_METHOD_KEY =
        "_methodinvokingjob_targetmethod";
    /** Key to entry in job data map to array of parameters to method to invoke. */
    public final static String TARGET_METHOD_PARAMETERS_KEY =
        "_methodinvokingjob_targetmethodparams";

    @Override
    public void execute(
        final JobExecutionContext inContext) throws JobExecutionException {
        final JobDataMap theJobDataMap =
            inContext.getJobDetail().getJobDataMap();
        final String theTaskName = inContext.getJobDetail().getKey().getName();
        final String theTaskGroupName =
            inContext.getJobDetail().getKey().getGroup();

        LOGGER.info("Started executing task {} in group {}", theTaskName,
            theTaskGroupName);

        final Object theTargetObject = theJobDataMap.get(TARGET_OBJECT_KEY);
        final Object theTargetMethodNameObject =
            theJobDataMap.get(TARGET_METHOD_KEY);
        final Object theTargetMethodParamsObject =
            theJobDataMap.get(TARGET_METHOD_PARAMETERS_KEY);

        if (theTargetObject != null && theTargetMethodNameObject != null) {
            /* Cast parameters to appropriate types. */
            final String theTargetMethodName =
                (String) theTargetMethodNameObject;
            Object[] theTargetMethodParams = new Object[0];

            /*
             * Parameter array may be null, in which a no-params method
             * invocation will be performed.
             */
            if (theTargetMethodParamsObject != null) {
                theTargetMethodParams = (Object[]) theTargetMethodParamsObject;
            }

            /*
             * Check if logging is enabled before applying conversions only
             * needed for logging purposes.
             */
            if (LOGGER.isDebugEnabled()) {
                final Object[] theDebugLogArguments =
                    new Object[] { theTargetMethodName,
                        theTargetObject.getClass().getName(),
                        Arrays.asList(theTargetMethodParams).toString() };
                LOGGER
                    .debug(
                        "Invoking the method {} on object of the type {} with the parameters {}",
                        theDebugLogArguments);
            }

            /* Invoke the target method. */
            try {
                MethodInvoker theMethodInvoker = new MethodInvoker();
                theMethodInvoker.setTargetObject(theTargetObject);
                theMethodInvoker.setTargetMethod(theTargetMethodName);
                theMethodInvoker.setArguments(theTargetMethodParams);
                theMethodInvoker.prepare();

                theMethodInvoker.invoke();
            } catch (final Throwable theException) {
                /* 
                 * Catch all exceptions, in order to allow the program to 
                 * continue to run despite exceptions.
                 */
                LOGGER.error(
                    "An error occurred invoking the method "
                        + theTargetMethodName + " on object of" + " the type "
                        + theTargetObject.getClass().getName()
                        + " with the parameters "
                        + Arrays.asList(theTargetMethodParams).toString(),
                    theException);
            }

            LOGGER.info("Successfully completed executing task {} in group {}",
                theTaskName, theTaskGroupName);
        }
    }
}
