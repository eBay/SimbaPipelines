package com.ebay.cip.framework.feed;

/**
 * Created by hachong on 10/1/2015.
 */
public class ErrorLog {
    private String jobId;
    private String calLink;

    public ErrorLog(String jobId, String calLink){
        this.jobId = jobId;
        this.calLink = calLink;
    }

    public String getJobId() {
        return jobId;
    }

    public String getCalLink() {
        return calLink;
    }
}
