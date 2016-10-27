package com.ebay.cip.framework.test.jobProcessor;

import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.test.BasePipelineUnitTest;
import com.ebay.cip.framework.test.job.UnitTestParentJob;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by jagmehta on 5/12/2015.
 */
public class UnitTestParentJobProcessor extends BaseJobProcessor {
    /**
     * This method should be called exactly once per object creation.
     *
     * @throws ProcessingException
     * @throws ServiceException
     */
    @Override
    public void Initialize() throws ServiceException, ProcessingException {

    }

    /**
     * This is the core method to execute job. This method may be called multiple
     * times in failed scenario. Logic needs to be idempotent.
     *
     * @param job Job to be executed.
     * @throws ProcessingException
     */
    @Override
    public void execute(Job job) {
    }


    @Override
    public void onOneChildComplete(Job childJob) {
        UnitTestParentJob myJob = BasePipelineUnitTest.parentJobCache;
        myJob.setLastExecutedJob(childJob);
        //myJob.getCallBackTestClass().onPipelineComplete(myJob);
    }

    @Override
    public void onAllChildrenComplete(Job parentJob) {
        parentJob.getJobMetaData().setJobStatus(JobStatusEnum.SUCCESS);
    }

}
