package com.ebay.cip.framework.test.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;

/**
 * Created by hachong on 8/13/2015.
 */
public class TestJob3 extends Job {
    @Override
    public JobTypeEnum getJobType() {
        return TestjobTypeEnum.TEST_JOB3;
    }
}
