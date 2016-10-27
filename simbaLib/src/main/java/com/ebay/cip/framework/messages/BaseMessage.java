package com.ebay.cip.framework.messages;


import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.kernel.calwrapper.CalEventHelper;

import java.io.Serializable;

/**
 * Created by hachong on 4/2/2015.
 */
public abstract class BaseMessage implements Serializable {
    private String feedId;
    private String jobId;
    private Job job;
    private int retryCount = 0;


    public BaseMessage(String feedId, String jobId){
        this.feedId = feedId;
        this.jobId = jobId;
    }

    public BaseMessage(Job job){
        this.job = job;
        this.jobId = job.getJobId();
        this.feedId = job.getJobMetaData().getFeedId();
    }

    public Job getJob() {
        if(job!= null){
            return job;
        }

        if (job == null)
            try {
                Feed feed = FeedFactory.getFeed(feedId);
                job =  feed.getInstanceFactory().getObjectInstance(jobId, Job.class);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("DBError",e,true,"Unable to get instance job with key: " + jobId);
            } catch (FeedNotFoundException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
            }
        return job;
    }

    public void setJob(Job job){
        this.job = job;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getFeedId(){
       return this.feedId;
    }

    public String getJobId(){
        return this.jobId;
    }

    public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
    }

    public boolean isRetriable() {
        if(retryCount < getJob().getMaxRetryCount()) {
            return true;
        }
        return false;
    }

    public void increamentRetryCount(){
        retryCount += 1;
    }

}
