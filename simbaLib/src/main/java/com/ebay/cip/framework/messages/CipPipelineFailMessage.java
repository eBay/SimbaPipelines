package com.ebay.cip.framework.messages;

/**
 * Created by hachong on 10/22/2015.
 */
public class CipPipelineFailMessage {
    public String feedId;
    public String stacktrace;

    public CipPipelineFailMessage(String feedId,String stacktrace){
     this.feedId = feedId;
     this.stacktrace = stacktrace;
    }

}
