package com.ebay.cip.framework.test.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;

/**
 * Created by hachong on 8/13/2015.
 */
public class TestJob2 extends Job {
    //Keep this public. Used in junit.
    public static int maxRetry = 3;
    @Override
    public JobTypeEnum getJobType() {
        return TestjobTypeEnum.TEST_JOB2;
    }
    @Override
    public int getMaxRetryCount() {
        return maxRetry;
    }
}
