package com.ebay.cip.framework.exception;


/**
 * Created by jagmehta on 8/20/2015.
 * This exception is used when we are fast failing some calls.
 */
public class SimbaCircuitBreakerOpenException extends ExecuteException {

    public SimbaCircuitBreakerOpenException(Exception e){
        super(e);
    }
}
