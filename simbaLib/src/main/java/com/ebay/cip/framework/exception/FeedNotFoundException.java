package com.ebay.cip.framework.exception;

/**
 * Created by hachong on 11/19/2015.
 */
public class FeedNotFoundException extends ProcessingException {

    public FeedNotFoundException(Exception e){
        super(e);
    }
    public FeedNotFoundException(String message){
        super(message);
    }

}
