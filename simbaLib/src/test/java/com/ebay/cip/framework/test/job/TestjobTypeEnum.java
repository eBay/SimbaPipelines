package com.ebay.cip.framework.test.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;

/**
 * Created by hachong on 8/13/2015.
 */
public class TestjobTypeEnum extends JobTypeEnum {
    public static final JobTypeEnum TEST_JOB = JobTypeEnum.add("TestJob");
    public static final JobTypeEnum TEST_JOB2 = JobTypeEnum.add("TestJob2");
    public static final JobTypeEnum TEST_JOB3 = JobTypeEnum.add("TestJob3");


    //JobTypeEnum for Timeout test
    public static final JobTypeEnum TEST_TIMEOUT_JOB = JobTypeEnum.add("TestTimeoutJob");
    public static final JobTypeEnum TEST_TIMEOUT_JOB2 = JobTypeEnum.add("TestTimeoutJob2");
    public static final JobTypeEnum TEST_PARENT_TIMEOUT_JOB = JobTypeEnum.add("TestParentTimeoutJob");
    public static final JobTypeEnum TEST_PARENT_TIMEOUT_JOB2 = JobTypeEnum.add("TestParentTimeoutJob2");




    protected TestjobTypeEnum(int id, String name) {
            super(id, name);
        }
}

