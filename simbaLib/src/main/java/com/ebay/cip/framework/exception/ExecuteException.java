package com.ebay.cip.framework.exception;

/**
 * Created by hachong on 8/6/2015.
 */
public class ExecuteException extends ProcessingException {

    public ExecuteException(Exception e){
        super(e);
    }
    public ExecuteException(String message){
        super(message);
    }
}
