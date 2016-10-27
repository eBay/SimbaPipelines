package com.ebay.cip.framework.messages;


/**
 * Created by hachong on 7/30/2015.
 */
public class WaitMessage extends BaseMessage {

    public boolean runResume = true;

    public WaitMessage(String feedId,String jobId){
        super(feedId,jobId);
    }

}
