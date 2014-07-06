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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import se.ivankrizsan.messagecowboy.domain.entities.SchedulableTaskConfig;
import se.ivankrizsan.messagecowboy.domain.entities.TaskJob;

/**
 * A schedulable task that contains information specific to a message move
 * to be performed my the Message Cowboy.
 *
 * @author Ivan Krizsan
 */
@Entity(name = "MessageCowboySchedulableTaskConfig")
@Table(name = "SchedulableTaskConfigurations")
public class MessageCowboySchedulableTaskConfig implements
    SchedulableTaskConfig {

    /* Constant(s): */
    /** Serialization version id of this class. */
    private static final long serialVersionUID = 5628402793423246072L;

    /* Instance variable(s): */
    /** Name of the task. Serves as id of the task and must be unique. */
    @Id
    @Column(unique = true, nullable = false)
    protected String name;
    /** URI of inbound endpoint which is to be polled for incoming messages. */
    @Column(nullable = false)
    protected String inboundEndpointURI;
    /** URI of outbound endpoint to which messages are to be sent. */
    @Column(nullable = false)
    protected String outboundEndpointURI;
    /** Name of the group the task belongs to. */
    @Column(nullable = false)
    protected String taskGroupName;
    /** Timeout time in milliseconds requesting message from inbound endpoint. */
    @Column(nullable = false)
    protected long inboundTimeout;
    /** Cron expression determining when the task will be run. */
    @Column(nullable = false)
    protected String cronExpression;
    /**
     * Start date after the scheduled task will start executing, or null
     * if task will start executing immediately after having been scheduled.
     */
    @Column(nullable = true)
    @Temporal(TemporalType.DATE)
    protected Date startDate;
    /**
     * End date after the scheduled task will no longer execute, or null
     * if the scheduled task will execute indefinitely.
     */
    @Column(nullable = true)
    @Temporal(TemporalType.DATE)
    protected Date endDate;
    /** Flag indicating whether task is enabled. */
    @Column(nullable = false)
    protected boolean taskEnabledFlag;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(
        final String inName) {
        name = inName;
    }

    @Override
    public String getTaskGroupName() {
        return taskGroupName;
    }

    @Override
    public void setTaskGroupName(
        final String inTaskGroupName) {
        taskGroupName = inTaskGroupName;
    }

    public String getInboundEndpointURI() {
        return inboundEndpointURI;
    }

    public void setInboundEndpointURI(
        final String inInboundEndpointURI) {
        inboundEndpointURI = inInboundEndpointURI;
    }

    public long getInboundTimeout() {
        return inboundTimeout;
    }

    public void setInboundTimeout(
        final long inInboundTimeout) {
        inboundTimeout = inInboundTimeout;
    }

    public String getOutboundEndpointURI() {
        return outboundEndpointURI;
    }

    public void setOutboundEndpoint(
        final String inOutboundEndpointURI) {
        outboundEndpointURI = inOutboundEndpointURI;
    }

    @Override
    public Class<? extends TaskJob> getTaskJobType() {
        return QuartzMuleTaskJob.class;
    }

    @Override
    public String getCronExpression() {
        return cronExpression;
    }

    @Override
    public void setCronExpression(
        final String inCronExpression) {
        cronExpression = inCronExpression;
    }

    @Override
    public Date getStartDate() {
        return (Date) (startDate == null ? null : startDate.clone());
    }

    @Override
    public void setStartDate(
        final Date inStartDate) {
        startDate = (Date) (inStartDate == null ? null : inStartDate.clone());
    }

    @Override
    public Date getEndDate() {
        return (Date) (endDate == null ? null : endDate.clone());
    }

    @Override
    public void setEndDate(
        final Date inEndDate) {
        endDate = (Date) (inEndDate == null ? null : inEndDate.clone());
    }

    @Override
    public boolean getTaskEnabledFlag() {
        return taskEnabledFlag;
    }

    @Override
    public void setTaskEnabledFlag(
        final boolean inTaskEnabledFlag) {
        taskEnabledFlag = inTaskEnabledFlag;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MessageCowboySchedulableTaskConfig [name=")
            .append(name).append(", inboundEndpointURI=")
            .append(inboundEndpointURI).append(", outboundEndpointURI=")
            .append(outboundEndpointURI).append(", taskGroupName=")
            .append(taskGroupName).append(", inboundTimeout=")
            .append(inboundTimeout).append(", cronExpression=")
            .append(cronExpression).append(", startDate=").append(startDate)
            .append(", endDate=").append(endDate).append(", taskEnabledFlag=")
            .append(taskEnabledFlag).append("]");
        return builder.toString();
    }
}
