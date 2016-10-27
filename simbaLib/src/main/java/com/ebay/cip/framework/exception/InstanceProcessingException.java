package com.ebay.cip.framework.exception;

/**
 * Created by hachong on 7/7/2015.
 */
public class InstanceProcessingException extends ProcessingException {

    public InstanceProcessingException(Exception e){
        super(e);
    }
    public InstanceProcessingException(String message) {
        super(message);
    }

}
