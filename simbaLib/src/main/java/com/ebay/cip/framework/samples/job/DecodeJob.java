package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class DecodeJob extends Job{

    String inputString = null;

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.DecodeJob.getId());
    }

    public String getInputString() {
        return inputString;
    }

    public void setInputString(String inputString) {
        this.inputString = inputString;
    }
}
