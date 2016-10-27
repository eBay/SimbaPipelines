package com.ebay.cip.framework.test.jobProcessor;

import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.exception.AllChildrenCompleteException;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.exception.ResumeCompleteProcessingException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.test.job.TestJob3;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by hachong on 8/17/2015.
 */
public class ResumeExceptionJobProcessor extends BaseJobProcessor {
    @Override
    public void Initialize() throws ServiceException, ProcessingException {

    }

    @Override
    public void execute(Job job) throws ExecuteException {
        TestJob3 testJob3 = new TestJob3();
        Pipelines.fork(job, testJob3);
    }

    @Override
    public void resumeFromWaiting(Job job)throws ResumeCompleteProcessingException {
        throw new ResumeCompleteProcessingException(new RuntimeException());
    }

    @Override
    public void onFailResume(Job failedJob,Throwable e){
        System.out.println("in onFail Resume");
    }

    @Override
    public void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException {
        parentJob.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("true"));
        parentJob.success();
    }
}
