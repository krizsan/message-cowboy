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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.FileCopyUtils;

/**
 * Implements an abstract {@link TransportService} based on refreshable XML configuration files.
 * Allows detecting reloading changed configuration files.
 */
public abstract class AbstractXmlConfigurerdTransportService implements TransportService {

    /* Constant(s): */
    /** Class logger. */
    static final Logger LOGGER = LoggerFactory
        .getLogger(AbstractXmlConfigurerdTransportService.class);

    /**
     * Location patterns specifying the locations of XML configuration
     * files used by the transport service.
     * @see PathMatchingResourcePatternResolver for information on how to
     * construct such a path.
     */
    protected List<String> mConfigResourcesLocationPatterns;

    /** Information about currently used XML configuration resources. */
    protected List<XmlConfigurationResourceInfo> mConfigurationResourceInfos =
        new ArrayList<XmlConfigurationResourceInfo>();

    /**
     * Sets the configuration resource location patterns that specifies where
     * to look for connector definition resources (commonly files).
     *
     * @param inConfigurationResourceLocationPatterns Configuration resource
     * location patterns.
     * Please refer to the documentation of the Spring
     * {@code PathMatchingResourcePatternResolver} class for information on
     * location pattern format.
     */
    @Required
    public void setConnectorsResourcesLocationPattern(
        final List<String> inConfigurationResourceLocationPatterns) {
        mConfigResourcesLocationPatterns = inConfigurationResourceLocationPatterns;
    }

    /**
     * Retrieves the configuration resource location patterns that specifies where
     * to look for connector definition resources (commonly files).
     *
     * @return Configuration resource location patterns.
     */
    public List<String> getConnectorsResourcesLocationPattern() {
        return mConfigResourcesLocationPatterns;
    }

    /**
     * Retrieves configuration resource information for the currently configured
     * XML configuration resources.
     *
     * @return List of configuration resource information.
     * @throws IOException If error occurs discovering or accessing configuration resource.
     */
    protected List<XmlConfigurationResourceInfo> retrieveXmlConfigResourceInfos()
        throws IOException {
        final PathMatchingResourcePatternResolver theConnectorsResolver =
            new PathMatchingResourcePatternResolver();
        final List<XmlConfigurationResourceInfo> theConfigRsrcInfos =
            new ArrayList<XmlConfigurationResourceInfo>();

        for (String theConfigRsrcsLocationPattern : mConfigResourcesLocationPatterns) {
            final Resource[] theConnectorsConfigurations =
                theConnectorsResolver.getResources(theConfigRsrcsLocationPattern);

            LOGGER.debug("Found {} connector configuration files using the pattern {}",
                theConnectorsConfigurations.length, theConfigRsrcsLocationPattern);

            if (theConnectorsConfigurations.length > 0) {
                for (Resource theResource : theConnectorsConfigurations) {

                    final byte[] theConfigRsrcContents =
                        FileCopyUtils.copyToByteArray(theResource.getInputStream());
                    final String theConfigRsrcName = theResource.getFilename();
                    final String theConfigRsrcChecksum = DigestUtils.md5Hex(theConfigRsrcContents);

                    final XmlConfigurationResourceInfo theConfigRsrcInfo =
                        new XmlConfigurationResourceInfo(theConfigRsrcName, theConfigRsrcChecksum);
                    theConfigRsrcInfos.add(theConfigRsrcInfo);
                }
            }
        }
        return theConfigRsrcInfos;
    }

    /**
     * Determines whether an XML configuration resources have changed since last
     * time the configuration resources were read.
     * If configuration resources has changed, then the current configuration
     * resource information list will be updated.
     *
     * @throws IOException If error occurs accessing configuration resource.
     */
    protected boolean hasConfigurationResourceBeenModified() throws IOException {
        /* Get configuration resource information for (new) resources. */
        final List<XmlConfigurationResourceInfo> theNewConfigRsrscInfos =
            retrieveXmlConfigResourceInfos();
        Collections.sort(theNewConfigRsrscInfos);
        Collections.sort(mConfigurationResourceInfos);

        /* Determine if there are any changes to configuration resources. */
        boolean theConfigRsrcChangedFlag = false;
        if (mConfigurationResourceInfos.size() != theNewConfigRsrscInfos.size()) {
            /* Different number of configuration resources. */
            theConfigRsrcChangedFlag = true;
        } else {
            /* Check each configuration resource for modifications. */
            for (int i = 0; i < theNewConfigRsrscInfos.size(); i++) {
                final XmlConfigurationResourceInfo theNewConfigRsrcInfo =
                    theNewConfigRsrscInfos.get(i);
                final XmlConfigurationResourceInfo theOldConfigRsrcInfo =
                    mConfigurationResourceInfos.get(i);
                if (!theOldConfigRsrcInfo.equals(theNewConfigRsrcInfo)) {
                    theConfigRsrcChangedFlag = true;
                }
            }
        }

        if (theConfigRsrcChangedFlag) {
            mConfigurationResourceInfos = theNewConfigRsrscInfos;
        }

        return theConfigRsrcChangedFlag;
    }
}
