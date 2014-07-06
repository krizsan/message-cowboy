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
package se.ivankrizsan.messagecowboy.services.starter;

/**
 * Defines the public interface of the service that is responsible for starting
 * and stopping the Message Cowboy application.
 * 
 * @author Ivan Krizsan
 */
public interface MessageCowboyStarterService {

    /**
     * Starts the Message Cowboy application.<br/>
     * Reads the available task configurations and scheduling a task for each.
     */
    abstract void start();

    /**
     * Stops the Message Cowboy application.
     */
    abstract void stop();

    /**
     * Schedules, or re-schedules, all available tasks.<br/>
     * Any already scheduled tasks will be unscheduled before all available
     * task configurations are read and a task is scheduled for each of them.
     */
    abstract void scheduleTasks();

}