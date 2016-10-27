package com.ebay.cip.framework.test.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;
import com.ebay.cip.framework.test.BasePipelineUnitTest;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class UnitTestParentJob extends Job{

    ICipJob lastExecutedJob;
    BasePipelineUnitTest callBackTestClass;

    public UnitTestParentJob(BasePipelineUnitTest testClass) {
        this.callBackTestClass = testClass;
    }

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.UnitTestParentJob.getId());
    }

    public ICipJob getLastExecutedJob() {
        return lastExecutedJob;
    }

    public void setLastExecutedJob(ICipJob lastExecutedJob) {
        this.lastExecutedJob = lastExecutedJob;
    }

    public BasePipelineUnitTest getCallBackTestClass() {
        return callBackTestClass;
    }

    @Override
    public String getJobId() {
        return ICipJob.ORCHESTRATOR_JOB_ID;
    }
}
