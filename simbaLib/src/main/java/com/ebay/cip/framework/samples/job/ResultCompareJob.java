package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class ResultCompareJob extends Job{

    public static final String FINAL_RESULT = "X-TEST-FINAL_RESULT";
    private boolean passed = true;
    private String errorMessage = null;
    private Throwable exception = null;


    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.get(FrameworkJobTypeEnum.ResultCompareJob.getId());
    }

    @Override
    public int getMaxRetryCount() {
        return 0;
    }

    public boolean isPassed() {
        return passed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }
}
