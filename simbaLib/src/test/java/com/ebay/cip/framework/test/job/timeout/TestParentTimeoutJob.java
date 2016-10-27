package com.ebay.cip.framework.test.job.timeout;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.test.job.TestjobTypeEnum;

/**
 * Created by hachong on 9/25/2015.
 */
public class TestParentTimeoutJob extends Job {


    @Override
    public JobTypeEnum getJobType() {
        return TestjobTypeEnum.TEST_PARENT_TIMEOUT_JOB;
    }

    @Override
    public int getTimeoutPeriod() {return 5;}
}
