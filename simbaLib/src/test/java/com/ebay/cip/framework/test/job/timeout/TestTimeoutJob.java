package com.ebay.cip.framework.test.job.timeout;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.test.job.TestjobTypeEnum;

/**
 * Created by hachong on 6/3/2015.
 */
public class TestTimeoutJob extends Job {


    @Override
    public JobTypeEnum getJobType() {
        return TestjobTypeEnum.TEST_TIMEOUT_JOB;
    }

    @Override
    public int getTimeoutPeriod() {return 1000; }

}
