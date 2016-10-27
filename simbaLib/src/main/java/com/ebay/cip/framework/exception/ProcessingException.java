package com.ebay.cip.framework.exception;

import com.ebay.kernel.exception.ErrorData;

/**
 * Created by hachong on 4/3/2015.
 */
public class ProcessingException extends Exception {

    protected String m_method = "none";
    protected boolean isRetry = false;
    protected String m_message = null;
    protected ErrorData error;

    public ProcessingException(Exception e){
        super(e);
    }

    public ProcessingException(String message) {
        super(message);
        m_message = message;
    }

    public ProcessingException(String message,ErrorData error) {
        super(message);
        m_message = message;
        this.error = error;
    }

    public ProcessingException(String message,ErrorData error, boolean isRetry) {
        super(message);
        m_message = message;
        this.error = error;
        this.isRetry = isRetry;
    }

    public ProcessingException(Throwable th,ErrorData error,String message, boolean isRetry) {
        super(th);
        m_message = message;
        this.error = error;
        this.isRetry = isRetry;
    }

    public ProcessingException(Throwable th) {
        super(th);
    }

    public ProcessingException(Throwable th, String message, boolean isRetry) {
        super(th);
        m_message = message;
        this.isRetry = isRetry;
    }

    public ProcessingException(String method, String message) {
        super(message);
        m_method = method;
        m_message = message;
    }

    public ProcessingException(String method, String message, boolean isRetry) {
        super(message);
        m_method = method;
        m_message = message;
        this.isRetry = isRetry;
    }


    public String getMethod() {
        return m_method;
    }

    public void setMethod(String method) {
        m_method = method;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(String message) {
        this.m_message = message;
    }

    public ProcessingException() {
    }


    public boolean isRetry() {
        return isRetry;
    }

    public void setRetry(boolean isRetry) {
        this.isRetry = isRetry;
    }

    public ErrorData getError() {
        return error;
    }

    public void setError(ErrorData error) {
        this.error = error;
    }

}