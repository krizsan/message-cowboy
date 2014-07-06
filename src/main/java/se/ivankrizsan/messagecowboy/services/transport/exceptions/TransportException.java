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
package se.ivankrizsan.messagecowboy.services.transport.exceptions;

import org.springframework.core.NestedRuntimeException;

/**
 * Base class for exceptions occurring when transporting messages.<br/>
 * This exception and any child exceptions are unchecked exceptions.
 *
 * @author Ivan Krizsan
 */
public class TransportException extends NestedRuntimeException {
    /* Constant(s): */
    private static final long serialVersionUID = 2928345200953168566L;

    /* Instance variable(s): */

    public TransportException(final String inMsg, final Throwable inCause) {
        super(inMsg, inCause);
    }

    public TransportException(final String inMsg) {
        super(inMsg);
    }
}
