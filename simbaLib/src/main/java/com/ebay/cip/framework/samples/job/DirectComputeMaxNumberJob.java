package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;

import java.util.List;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class DirectComputeMaxNumberJob extends Job{

    public static final String DATA_TO_PROCESS = "DATA_TO_PROCESS";
    public static final String RESULT = "RESULT";

    List<String> numbers;
    Long max;

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.DirectComputeMaxNumberJob.getId());
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

}
