package com.ebay.cip.framework.job;

import com.ebay.cip.framework.enumeration.JobStatusEnum;

/**
 * Created by hachong on 9/21/2015.
 */
public class ForkedJobInfo {

    private String jobId;
    private JobStatusEnum status;

    public ForkedJobInfo(){}

    public ForkedJobInfo(String jobId,JobStatusEnum status){
        this.jobId = jobId;
        this.status = status;
    }

    public JobStatusEnum getJobStatus(){
        return this.status;
    }

    public String getJobId(){
        return this.jobId;
    }
}
