package com.ebay.cip.framework.messages;

import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.Duration;

/**
 * Created by hachong on 9/17/2015.
 */
public class StartMonitoringJobMessage extends BaseMessage{

    private Deadline deadline;

    public StartMonitoringJobMessage(String feedId, String jobId, int timeoutPeriod){
        super(feedId,jobId);
        this.deadline = Duration.create(timeoutPeriod,"seconds").fromNow();
    }

    public Deadline getDeadline(){
        return this.deadline;
    }
}
