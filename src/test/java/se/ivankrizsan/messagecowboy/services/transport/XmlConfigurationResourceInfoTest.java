/*
 * This file is part of Message Cowboy.
 * Copyright 2015 Ivan A Krizsan. All Rights Reserved.
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
package se.ivankrizsan.messagecowboy.services.transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Features;

/**
 * Tests the {@code XmlConfigurationResourceInfo} class.
 *
 * @author Ivan Krizsan
 */
@Features("XML Configuration Metadata")
public class XmlConfigurationResourceInfoTest {
    /* Constant(s): */
    protected static final String RESOURCE_CHECKSUM_1 = "123456";
    protected static final String RESOURCE_NAME_1 = "TheResourceName";

    /* Instance variable(s): */
    protected XmlConfigurationResourceInfo mInstanceUnderTest;

    /**
     * Sets up before each test.
     */
    @Before
    public void setUpBeforeTest() {
        mInstanceUnderTest = new XmlConfigurationResourceInfo(RESOURCE_NAME_1, RESOURCE_CHECKSUM_1);
    }

    /**
     * Tests calculation of hash code.
     */
    @Test
    public void testHashCode() {
        final int theHashCode = mInstanceUnderTest.hashCode();
        Assert.assertTrue("Hash code should never be zero", theHashCode != 0);
    }

    /**
     * Tests creation of instances.
     */
    @Test
    public void testXmlConfigurationResourceInfo() {
        Assert.assertNotNull("An instance should have been created", mInstanceUnderTest);

        Assert.assertNotNull("Resource name cannot be null in a new instance", mInstanceUnderTest.getResourceName());
        Assert.assertNotNull("Resource checksum cannot be null in a new instance",
            mInstanceUnderTest.getResourceChecksum());
    }

    /**
     * Tests creation of an instance with a null resource name value.
     * Should result in an exception being thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstanceCreationWithNullName() {
        new XmlConfigurationResourceInfo(null, "123123");
    }

    /**
     * Tests creation of an instance with a null resource checksum value.
     * Should result in an exception being thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInstanceCreationWithNullResourceChecksum() {
        new XmlConfigurationResourceInfo("MyResourceName", null);
    }

    /**
     * Tests checking for equality.
     */
    @Test
    public void testEqualsObject() {
        final XmlConfigurationResourceInfo theEqualInstance =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1, RESOURCE_CHECKSUM_1);
        final XmlConfigurationResourceInfo theUnequalInstanceName =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1 + "1", RESOURCE_CHECKSUM_1);
        final XmlConfigurationResourceInfo theUnequalInstanceChecksum =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1, RESOURCE_CHECKSUM_1 + "1");

        Assert.assertTrue("An instance with same parameters should be equal",
            mInstanceUnderTest.equals(theEqualInstance));
        Assert.assertFalse("An instance with different name should not be equal",
            mInstanceUnderTest.equals(theUnequalInstanceName));
        Assert.assertFalse("An instance with different checksum should not be equal",
            mInstanceUnderTest.equals(theUnequalInstanceChecksum));
    }

    /**
     * Tests comparing two instances.
     */
    @Test
    public void testCompareTo() {
        final XmlConfigurationResourceInfo theEqualInstance =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1, RESOURCE_CHECKSUM_1);
        final XmlConfigurationResourceInfo theInstanceDifferentChecksum =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1, RESOURCE_CHECKSUM_1 + "1");
        final XmlConfigurationResourceInfo theInstanceDifferentName =
            new XmlConfigurationResourceInfo(RESOURCE_NAME_1 + "1", RESOURCE_CHECKSUM_1);

        Assert.assertTrue("Two instances with same name and same checksum should compare as equal",
            mInstanceUnderTest.compareTo(theEqualInstance) == 0);
        Assert.assertTrue("Two instances with same name and different checksum should compare as equal",
            mInstanceUnderTest.compareTo(theInstanceDifferentChecksum) == 0);
        Assert.assertTrue("Two instances with different name and same checksum should compare as not equal",
            mInstanceUnderTest.compareTo(theInstanceDifferentName) != 0);
    }
}
