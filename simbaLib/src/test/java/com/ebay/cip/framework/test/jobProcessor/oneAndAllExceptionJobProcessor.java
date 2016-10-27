package com.ebay.cip.framework.test.jobProcessor;

import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.exception.*;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.test.job.TestJob3;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by hachong on 8/13/2015.
 */
public class oneAndAllExceptionJobProcessor extends BaseJobProcessor {
    @Override
    public void Initialize() throws ServiceException, ProcessingException {

    }

    @Override
    public void execute(Job job) throws ExecuteException {
        TestJob3 testJob3 = new TestJob3();
        Pipelines.fork(job, testJob3);
    }

    @Override
    public void onOneChildComplete(Job childJob) throws OneChildCompleteException { throw new OneChildCompleteException(new RuntimeException());}
    @Override
    public void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException {
        throw new AllChildrenCompleteException(new RuntimeException());
    }

    @Override
    public void onFailAllChildComplete(Job failedJob,Throwable e) {
        failedJob.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("true"));
        failedJob.failure();}

}
