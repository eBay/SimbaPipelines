package com.ebay.cip.framework.messages;

/**
 * Created by hachong on 9/24/2015.
 */
public class StopMonitoringJobMessage extends BaseMessage {
    public StopMonitoringJobMessage(String feedId, String jobId) {
        super(feedId, jobId);
    }
}
