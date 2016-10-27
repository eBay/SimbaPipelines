package com.ebay.cip.framework.test.jobProcessor;

import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by hachong on 8/13/2015.
 */
public class ExecuteExceptionJobProcessor extends BaseJobProcessor {

    public static final String FAILJOBS_KEY =  "TestJobFail";

    @Override
    public void Initialize() throws ServiceException, ProcessingException {

    }

    @Override
    public void execute(Job job) throws ExecuteException {
        // by default throw exeception except System property says not to
        Boolean shouldFail = Boolean.getBoolean(FAILJOBS_KEY);
        if(shouldFail) {
            throw new ExecuteException(new RuntimeException());
        }
        job.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("true"));
        job.success();

    }

    @Override
    public void onFailExecute(Job failedJob,Throwable e){
        System.out.println("calling onFailExecute");
        failedJob.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("true"));
        failedJob.failure();}
}