package com.ebay.cip.framework.messages;

import com.ebay.cip.framework.job.Job;

import java.io.Serializable;

/**
 * Created by hachong on 1/21/2015.
 */
public class CipMessage extends BaseMessage implements Serializable {

    public CipMessage(Job job){
        super(job);
    }

}
