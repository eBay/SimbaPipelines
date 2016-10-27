package com.ebay.cip.framework.job;

/**
 * Created by hachong on 4/3/2015.
 */
import akka.actor.ActorRef;
import com.ebay.cip.framework.*;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.handler.ForkedJobCompletionCounterHandler;
import com.ebay.cip.framework.messages.StopMonitoringJobMessage;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.service.FeedRequestData;
import com.ebay.cip.service.FeedResponseData;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;


import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Job implements ICipJob {

    private String jobId;
    private String pipelineId;
    private boolean forkComplete = true;
    private JobMetadata jobMetadata;

    private transient Feed feed;
    private transient JobContext jobContext;
    static transient Logger logger = Logger.getInstance(Job.class);

    public Job(){
        if(jobId == null) {
            this.jobId = UUID.randomUUID().toString();
        }
    }

    public Job( JobMetadata jobMetadata,String pipelineId){
        this();
        this.jobMetadata = jobMetadata;
        this.pipelineId = pipelineId;
    }

    public String getJobId() {
        return jobId;
    }

    public  abstract JobTypeEnum getJobType();

    private Context getContext(String id){
            return new BaseContext(id,jobMetadata.getFeedId());
    }

    /**
     * Gives jobContext active for current job
     * @return
     */
    public JobContext getJobContext() {
        if(jobContext == null)
            jobContext = (JobContext) getContext(this.pipelineId);
        return jobContext;
    }

    /**
     * Gives Global level feed context. Any data here are visible throughout the feed execution.
     * @return
     */
    @JsonIgnore
    public FeedContext getFeedContext() {
        return getFeed().getFeedContext();
    }

    public JobStatusEnum getJobStatus(){
        return getJobMetaData().getJobStatus();
    }



    //TODO need to question business logic for the usage of the payload
    @JsonIgnore
    public Payload getPayload() {
        return getFeed().getFeedRequestData().getRequestPayload();
    }


    public int getMaxRetryCount() {
        return FrameworkConfigBean.getBean().getRetryCount();
    }
    public int getTimeoutPeriod() { return FrameworkConfigBean.getBean().getTimeoutPeriod(); }

    public boolean isComplete() {
        return JobStatusEnum.SUCCESS.equals(jobMetadata.getJobStatus()) || JobStatusEnum.FAILURE.equals(jobMetadata.getJobStatus()) || JobStatusEnum.TIMEOUT.equals(jobMetadata.getJobStatus());
    }

    public boolean isForkComplete(){
        return forkComplete;
    }

    /**
     * Business logic will set the fork complete to false. in case the forking is not done from execute() method.
     */
    public void setForkComplete(boolean status){
        forkComplete = status;
    }




    public void success(){
        feed = getFeed();
        if(isComplete()){
            String message = "Job id: "+jobId+" is already complete";
            CalEventHelper.writeLog("Framework", "job.success()", message, "0");
            logger.log(LogLevel.INFO,message);
            return;
        }

        if(JobStatusEnum.WAITING.equals(jobMetadata.getJobStatus())) {
            jobMetadata.setJobStatus(JobStatusEnum.SUCCESS);
            //sent to watcher actor to stop monitoring this job
            CipActorSystem.getInstance().getJobMonitorActor().tell(new StopMonitoringJobMessage(this.getFeedId(), this.getJobId()), ActorRef.noSender());

        }
        else{
            jobMetadata.setJobStatus(JobStatusEnum.SUCCESS);
        }
        try {
            feed.getInstanceFactory().saveObjectInstance(this.getJobId(), this);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,"Unable to save the job. JobId: "+this.getJobId());
        }
    }

    public void failure(){
        feed = getFeed();
        if(isComplete()){
            String message = "Job id: "+jobId+" is already complete";
            CalEventHelper.writeLog("Framework", "job failure", message, "0");
            logger.log(LogLevel.INFO,message);
            return;
        }
        jobMetadata.setJobStatus(JobStatusEnum.FAILURE);
        try {
            feed.getInstanceFactory().saveObjectInstance(this.getJobId(), this);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,"Unable to save the job. JobId: "+this.getJobId());
        }
    }

    public void timeout(){
        feed = getFeed();
        if(isComplete()){
            CalEventHelper.writeLog("Framework", "job.timeout()", "Job id: " +jobId+ " is already complete", "0");
            return;
        }
        jobMetadata.setJobStatus(JobStatusEnum.TIMEOUT);
        try {
            feed.getInstanceFactory().saveObjectInstance(this.getJobId(), this);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,"Unable to save the job. JobId: "+this.getJobId());
        }
    }

    /**
     * Returns parent job id for this job
     * @return
     */
    public String getParentJobId(){
        return jobMetadata.getParentJobId();
    }

    @JsonIgnore
    public Job getParentJob() {
        try {
            return  getFeed().getInstanceFactory().getObjectInstance(getParentJobId(), Job.class);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            CalEventHelper.writeException("DBError", e,true,"Unable to get instance job with key: " + getParentJobId());
        }
        return null;
    }


    /**
     * This method returns current count of forked jobs.
     * @return
     */
    public int getForkedJobsCount() {
        return jobMetadata.getForkedJobsCounts();
    }

    public void incrementForkedJobsCount(){
        int currentCount = jobMetadata.getForkedJobsCounts();
        currentCount++;
        jobMetadata.setForkedJobsCounts(currentCount);
    }


    public String getNextJobId(){
        return jobMetadata.getNextJobId();
    }

    /**
     * Returns job id of previous job in this pipeline. Please not that its not parent job id.
     */
    public String getPreviousJobId(){
        return jobMetadata.getPreviousJobId();
    }


    @Override
    public JobMetadata getJobMetaData(){
        return jobMetadata;
    }

    public void setJobMetadata(JobMetadata jobMetadata) {
        this.jobMetadata = jobMetadata;
    }


    @Override
    public String getPipelineId(){
        return pipelineId;
    }

    public void setPipelineId(String pipelineId){
        this.pipelineId = pipelineId;
    }

    public void addForkedJobId(Job forkedJob){
        feed = getFeed();
        ForkedJobCompletionCounterHandler.addChildrenId(jobId,forkedJob.getJobId(),forkedJob.getPipelineId(),feed);
    }
    public String getFeedId(){
        return jobMetadata.getFeedId();
    }


    private Feed getFeed(){
        if(feed == null){
            try {
                feed = FeedFactory.getFeed(jobMetadata.getFeedId());
            }
            catch(FeedNotFoundException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
                return null;
            }
        }
        return feed;
    }

    public void setFeed(){

    }

}