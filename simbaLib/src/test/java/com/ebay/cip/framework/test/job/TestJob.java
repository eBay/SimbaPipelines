package com.ebay.cip.framework.test.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;

/**
 * Created by hachong on 6/3/2015.
 */
public class TestJob extends Job {
    @Override
    public JobTypeEnum getJobType() {
        return TestjobTypeEnum.TEST_JOB;
    }

    @Override
    public int getTimeoutPeriod() {return 3; }

    @Override
    public boolean equals(final Object obj){
        if(obj == null)
            return false;
        if(! (obj instanceof TestJob))
            return false;

        TestJob that = (TestJob) obj;
        return this.getJobId().equals(that.getJobId());
    }

}
