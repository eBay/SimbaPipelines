package com.ebay.cip.framework.messages;

import com.ebay.cip.framework.job.Job;

import java.io.Serializable;

/**
 * Created by hachong on 5/11/2015.
 */
public class CipPipelineCompleteMessage extends BaseMessage implements Serializable {
    public CipPipelineCompleteMessage(Job cipJob) {
        super(cipJob);
    }

}
