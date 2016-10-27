package com.ebay.cip.framework.job;

import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Created by hachong on 7/24/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobMetadata {
    private JobStatusEnum jobStatus;
    private String ExceptionCALLink;
    private String feedId;
    private String parentJobId;
    private String nextJobId;
    private String previousJobId;
    private int forkedJobsCounts;


    

    public JobMetadata(String feedId){
        this.jobStatus = JobStatusEnum.CREATED;
        this.forkedJobsCounts = 0;
        this.feedId = feedId;
    }

    public void setJobStatus(JobStatusEnum jobStatus){
        this.jobStatus = jobStatus;
    }

    public JobStatusEnum getJobStatus(){
        return jobStatus;
    }

    public void setFeedId(String feedId){
        this.feedId = feedId;
    }

    public String getFeedId(){
        return this.feedId;
    }

    public String getParentJobId(){
        return parentJobId;
    }

    public int getForkedJobsCounts(){
        return forkedJobsCounts;
    }

    public void setForkedJobsCounts(int forkedJobsCounts){
        this.forkedJobsCounts = forkedJobsCounts;
    }

    public void setParentJobId(String parentJobId){
        this.parentJobId = parentJobId;
    }

    public void setNextJobId(String nextJobId){
        this.nextJobId = nextJobId;
    }

    public String getNextJobId(){
        return this.nextJobId;
    }

    public void setPreviousJobId(String previousJobId){
        this.previousJobId = previousJobId;
    }

    public String getPreviousJobId(){
        return this.previousJobId;
    }

    public String getExceptionCALLink() {
        return ExceptionCALLink;
    }

    public void setExceptionCALLink(String exceptionCALLink) {
        this.ExceptionCALLink = exceptionCALLink;
    }


}
