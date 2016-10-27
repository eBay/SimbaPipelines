package com.ebay.cip.framework.messages;

/**
 * Created by hachong on 8/17/2015.
 */
public class HttpTimeoutMessage {
    private String feedId;

    public HttpTimeoutMessage(String feedId){
        this.feedId = feedId;
    }

    public String getFeedId(){
        return this.feedId;
    }
}
