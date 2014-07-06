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
package se.ivankrizsan.messagecowboy.domain.valueobjects;

/**
 * Value object that holds a task key, that is a task group name and a task
 * name.<br/>
 * A task key is immutable once it has been created.
 * 
 * @author Ivan Krizsan
 */
public class TaskKey {
    /* Constant(s): */

    /* Instance variable(s): */
    protected String mTaskGroupName;
    protected String mTaskName;

    /**
     * Creates a task key using supplied task group and task names, which must
     * not be null.
     * 
     * @param inTaskGroupName Task group name.
     * @param inTaskName Task name.
     */
    public TaskKey(final String inTaskGroupName, final String inTaskName) {
        super();
        if (inTaskGroupName == null || inTaskName == null) {
            throw new IllegalArgumentException(
                "Task group and task name must both be specified");
        }
        mTaskGroupName = inTaskGroupName;
        mTaskName = inTaskName;
    }

    public String getTaskGroupName() {
        return mTaskGroupName;
    }

    public String getTaskName() {
        return mTaskName;
    }
}
