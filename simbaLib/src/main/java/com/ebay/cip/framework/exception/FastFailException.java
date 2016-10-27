package com.ebay.cip.framework.exception;

import akka.pattern.CircuitBreakerOpenException;

/**
 * Created by jagmehta on 8/20/2015.
 * This exception is used when we are fast failing some calls.
 */
public class FastFailException extends ExecuteException {

    public FastFailException(Exception e){
        super(e);
    }
}
