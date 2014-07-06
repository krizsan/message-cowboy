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
package se.ivankrizsan.messagecowboy.domain.entities.impl;

import org.mule.api.MuleMessage;

import se.ivankrizsan.messagecowboy.domain.entities.MoverMessage;

/**
 * Implementation of {@code MoverMessage} used with the Mule implementation
 * {@code MuleTransportService} of {@code TransportService}
 *
 * @author Ivan Krizsan
 */
public class MuleMoverMessage implements MoverMessage<MuleMessage> {
    /* Constant(s): */

    /* Instance variable(s): */
    /** Object holding implementation-specific message and message metadata. */
    protected MuleMessage mMessage;

    /**
     * Default constructor.
     */
    public MuleMoverMessage() {
    }

    /**
     * Creates an instance and sets the message contents to supplied
     * {@code MuleMessage}.
     *
     * @param inMessage Implementation-specific message.
     */
    public MuleMoverMessage(final MuleMessage inMessage) {
        mMessage = inMessage;
    }

    @Override
    public MuleMessage getMessage() {
        return mMessage;
    }

    @Override
    public void setMessage(
        final MuleMessage inMessage) {
        mMessage = inMessage;
    }
}
