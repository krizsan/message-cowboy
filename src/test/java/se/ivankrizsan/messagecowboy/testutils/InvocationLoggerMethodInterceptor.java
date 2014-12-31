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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Method interceptor that logs all method invocations to a list on the interceptor.
 * The list can later be retrieved to inspect which method(s) were invoked and at which time each
 * method invocation occurred.
 *
 * @author Ivan Krizsan
 */
public class InvocationLoggerMethodInterceptor implements MethodInterceptor {
    /* Constant(s): */

    /* Instance variable(s): */
    protected List<InvocationLogEntry> mInvocationLogEntries = Collections
        .synchronizedList(new ArrayList<InvocationLogEntry>());

    @Override
    public Object invoke(final MethodInvocation inInvocation) throws Throwable {
        final String theMethodName = inInvocation.getMethod().toGenericString();
        final Object theTargetObject = inInvocation.getThis();
        final InvocationLogEntry theInvocationLogEntry = new InvocationLogEntry(theMethodName, theTargetObject);
        mInvocationLogEntries.add(theInvocationLogEntry);

        return inInvocation.proceed();
    }

    /**
     * Retrieves an unmodifiable list of the invocation log entries for this interceptor.
     *
     * @return Unmodifiable list of invocation log entries.
     */
    public List<InvocationLogEntry> getInvocationLogEntries() {
        return Collections.unmodifiableList(mInvocationLogEntries);
    }

    /**
     * Instances of this class represent a method invocation as logged by a {@code InvocationLoggerMethodInterceptor}.
     * An invocation log entry is immutable once it has been created.
     *
     * @author Ivan Krizsan
     */
    public class InvocationLogEntry {
        /* Constant(s): */

        /* Instance variable(s): */
        protected String mMethodName;
        protected Object mTargetObject;
        protected Date mInvocationTime;

        /**
         * Creates an invocation log entry.
         *
         * @param inMethodName Name of method invoked.
         * @param inTargetObject Object on which method was invoked.
         * @param inInvocationTime Time of method invocation, or null if current time is to be used.
         */
        public InvocationLogEntry(final String inMethodName, final Object inTargetObject, final Date inInvocationTime) {
            super();
            mMethodName = inMethodName;
            mTargetObject = inTargetObject;
            mInvocationTime = inInvocationTime;
            if (mInvocationTime == null) {
                mInvocationTime = new Date();
            }
        }

        /**
         * Creates an invocation log entry, setting the invocation time to the current time.
         *
         * @param inMethodName Name of method invoked.
         * @param inTargetObject Object on which method was invoked.
         */
        public InvocationLogEntry(final String inMethodName, final Object inTargetObject) {
            this(inMethodName, inTargetObject, null);
        }

        public String getMethodName() {
            return mMethodName;
        }

        public Object getTargetObject() {
            return mTargetObject;
        }

        public Date getInvocationTime() {
            return mInvocationTime;
        }

        @Override
        public String toString() {
            return "InvocationLogEntry [mMethodName=" + mMethodName + ", mTargetObject=" + mTargetObject
                + ", mInvocationTime=" + mInvocationTime + "]";
        }
    }
}
