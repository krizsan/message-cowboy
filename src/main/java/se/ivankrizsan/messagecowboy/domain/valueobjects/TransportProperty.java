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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Value object holding one name and value pair of a property that will be
 * enclosed when requesting and dispatching messages for a task.
 *
 * @author Ivan Krizsan
 */
@Entity(name = "TransportProperty")
@Table(name = "TransportProperties")
public class TransportProperty {
    /* Constant(s): */

    /* Instance variable(s): */
    /** Name of property. */
    @Id
    @Column(nullable = false)
    protected String propertyName;
    /** Value of property. */
    @Column(nullable = false)
    protected String propertyValue;

    public String getName() {
        return propertyName;
    }

    public void setName(final String inName) {
        propertyName = inName;
    }

    public String getValue() {
        return propertyValue;
    }

    public void setValue(final String inValue) {
        propertyValue = inValue;
    }
}
