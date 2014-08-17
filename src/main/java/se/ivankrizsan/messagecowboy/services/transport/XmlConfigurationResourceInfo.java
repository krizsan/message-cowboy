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
package se.ivankrizsan.messagecowboy.services.transport;

/**
 * Holds information about one XML configuration resource, such as name and a
 * checksum of the resource.
 * Instances of this class are immutable.
 *
 * @author Ivan Krizsan
 */
class XmlConfigurationResourceInfo implements
    Comparable<XmlConfigurationResourceInfo> {
    /* Constant(s): */

    /* Instance variable(s): */
    protected String mResourceName;
    protected String mResourceChecksum;

    /**
     * Creates a resource info instance for configuration resource with supplied name
     * having supplied checksum.
     *
     * @param inResourceName Name of configuration resource.
     * @param inResourceChecksum Checksum of configuration resource.
     */
    public XmlConfigurationResourceInfo(final String inResourceName,
        final String inResourceChecksum) {
        if (inResourceName == null || inResourceChecksum == null) {
            throw new IllegalArgumentException(
                "Name and checksum must not be null");
        }

        mResourceName = inResourceName;
        mResourceChecksum = inResourceChecksum;
    }

    public String getResourceName() {
        return mResourceName;
    }

    public String getResourceChecksum() {
        return mResourceChecksum;
    }

    @Override
    public int hashCode() {
        final int thePrime = 31;
        int theResult = 1;
        theResult =
            thePrime
                * theResult
                + ((mResourceChecksum == null) ? 0 : mResourceChecksum
                    .hashCode());
        theResult =
            thePrime * theResult
                + ((mResourceName == null) ? 0 : mResourceName.hashCode());
        return theResult;
    }

    @Override
    public boolean equals(final Object inObjectToCompare) {
        if (this == inObjectToCompare) {
            return true;
        }
        if (inObjectToCompare == null) {
            return false;
        }
        if (getClass() != inObjectToCompare.getClass()) {
            return false;
        }
        XmlConfigurationResourceInfo theOtherConfigRsrscInfo =
            (XmlConfigurationResourceInfo) inObjectToCompare;
        if (mResourceChecksum == null) {
            if (theOtherConfigRsrscInfo.mResourceChecksum != null) {
                return false;
            }
        } else if (!mResourceChecksum
            .equals(theOtherConfigRsrscInfo.mResourceChecksum)) {
            return false;
        }
        if (mResourceName == null) {
            if (theOtherConfigRsrscInfo.mResourceName != null) {
                return false;
            }
        } else if (!mResourceName.equals(theOtherConfigRsrscInfo.mResourceName)) {
            return false;
        }
        return true;
    }

    /**
     * Compares supplied configuration resource info object with this object.
     * Only compares with regard to the name of the configuration resource.
     */
    @Override
    public int compareTo(
        final XmlConfigurationResourceInfo inOtherConfigRsrcInfo) {
        final int theCompareResult =
            mResourceName.compareTo(inOtherConfigRsrcInfo.getResourceName());
        return theCompareResult;
    }
}
