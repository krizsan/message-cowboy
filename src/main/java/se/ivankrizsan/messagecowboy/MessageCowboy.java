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
package se.ivankrizsan.messagecowboy;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Message Cowboy starter class.
 * 
 * @author Ivan Krizsan
 */
public final class MessageCowboy {
    /* Constant(s): */
    /** Class logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCowboy.class);

    /**
     * Starts the Message Cowboy.
     * 
     * @param args Command line arguments. Not used.
     */
    public static void main(final String[] args) {
        LOGGER.debug("Loading Spring context...");

        logProcessIdAndHost();

        @SuppressWarnings("resource")
        final AnnotationConfigApplicationContext theSpringContext =
            new AnnotationConfigApplicationContext(MessageCowboyConfiguration.class, ProductionPropertyOverrides.class);
        theSpringContext.registerShutdownHook();

        LOGGER.debug("Spring context loaded.");
        LOGGER.info("Hit [ENTER] in console to stop Message Cowboy.");

        waitForKeyboardEnter();

        System.exit(0);
    }

    private static void waitForKeyboardEnter() {
        try {
            System.in.read();
        } catch (final IOException theException) {
            /* Ignore exceptions. */
        }
    }

    private static void logProcessIdAndHost() {
        final String[] thePidAndHost = ManagementFactory.getRuntimeMXBean().getName().split("@");

        LOGGER.info("Process id: {}", thePidAndHost[0]);
        LOGGER.info("Host: {}", thePidAndHost[1]);
    }
}
