package com.ebay.cip.framework.exception;


/**
 * Created by hachong on 4/3/2015.
 */
public class OneChildCompleteException extends ProcessingException {

    public OneChildCompleteException(Exception e){
        super(e);
    }
}