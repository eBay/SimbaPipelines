package com.ebay.cip.framework.messages;

import com.ebay.cip.framework.job.Job;

/**
 * Created by hachong on 9/21/2015.
 */
public class TimeoutMessage extends BaseMessage {

    public TimeoutMessage(String feedId,String jobId){
        super(feedId,jobId);
    }
}
