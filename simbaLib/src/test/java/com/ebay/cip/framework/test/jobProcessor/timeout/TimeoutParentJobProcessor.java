package com.ebay.cip.framework.test.jobProcessor.timeout;

import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.exception.AllChildrenCompleteException;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.OneChildCompleteException;
import com.ebay.cip.framework.exception.ResumeCompleteProcessingException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.test.job.timeout.TestParentTimeoutJob2;
import com.ebay.cip.framework.test.job.timeout.TestTimeoutJob;
import com.ebay.cip.framework.test.job.timeout.TestTimeoutJob2;

/**
 * Created by hachong on 9/25/2015.
 */
public class TimeoutParentJobProcessor extends BaseJobProcessor {
    @Override
    public void execute(Job job) throws ExecuteException {
        Job testTimeoutJob21,testTimeoutJob22;
        if(job instanceof TestTimeoutJob) {
            testTimeoutJob21 = new TestTimeoutJob2();
            testTimeoutJob22 = new TestTimeoutJob2();
        }
        else{
            testTimeoutJob21 = new TestParentTimeoutJob2();
            testTimeoutJob22 = new TestParentTimeoutJob2();
        }
        System.out.println("Forking two "+job.getJobType().getName()+" jobs");
        //forking two children
        Pipelines.fork(job, testTimeoutJob21);
        Pipelines.fork(job, testTimeoutJob22);
    }

    @Override
    public void timeout(Job job){
        System.out.println("execute timeout method in TimeoutParentJobProcessor");
        job.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("Passed"));
    }

    @Override
    public void onOneChildComplete(Job childJob) throws OneChildCompleteException {
        System.out.println("execute on OneChildComplete method in TimeoutParentJobProcessor");
    }

    @Override
    public void resumeFromWaiting(Job job)throws ResumeCompleteProcessingException {
        //System.out.println("execute on resumeFromWaiting method in TimeoutParentJobProcessor");
    }

    @Override
    public void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException {
        parentJob.getFeedContext().getFeedResponseData().setResponsePayload(new Payload("Passed"));
        System.out.println("execute on allChildComplete method in TimeoutParentJobProcessor");
        parentJob.success();
    }
}
