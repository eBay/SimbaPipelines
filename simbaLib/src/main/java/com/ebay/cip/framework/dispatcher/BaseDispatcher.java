package com.ebay.cip.framework.dispatcher;

import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.ForkedJobInfo;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.messages.*;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.kernel.calwrapper.CalEventHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created by jagmehta on 3/17/2015.
 */
public abstract class BaseDispatcher implements Dispatcher {


    @Override
    public void dispatchNextJob(Job jobToDispatch, Job currentJob) {
        Feed feed = null;
        try {
            feed = FeedFactory.getFeed(jobToDispatch.getFeedId());
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }
        if(currentJob != null){
            jobToDispatch.getJobMetaData().setPreviousJobId(currentJob.getJobId());
            if(!currentJob.getJobId().equalsIgnoreCase(ICipJob.ORCHESTRATOR_JOB_ID)){
                jobToDispatch.getJobMetaData().setParentJobId(currentJob.getParentJobId());
                currentJob.getJobMetaData().setNextJobId(jobToDispatch.getJobId());
                try {
                    feed.getInstanceFactory().saveObjectInstance(currentJob.getJobId(), currentJob);
                } catch (InstanceProcessingException e) {
                    e.printStackTrace();
                    CalEventHelper.writeException("Framework",e,"Unable to save the current job before dispatching other job. JobId: "+currentJob.getJobId());
                }
            } else{
                jobToDispatch.getJobMetaData().setParentJobId(currentJob.getJobId());
            }
        }

        CipMessage message = new CipMessage(jobToDispatch);
        dispatchMessage(message);
    }

    @Override
    public void dispatchMessage(BaseMessage message) {
        Job jobToDispatch = message.getJob();
        if(jobToDispatch != null){
            try {
                Feed feed = FeedFactory.getFeed(jobToDispatch.getFeedId());
                feed.getInstanceFactory().saveObjectInstance(jobToDispatch.getJobId(), jobToDispatch);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,"Unable to save the job that will be dispatched. JobId:  "+jobToDispatch.getJobId());
            } catch ( FeedNotFoundException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
                return;
            }
        }
        sendCipMessage(message);
    }

    @Override
    public void dispatchPipelineCompletionMessage(Job job) {
        CipPipelineCompleteMessage pipelineCompleteMessage = new CipPipelineCompleteMessage(job);
        sendPipelineCompletionMessage(pipelineCompleteMessage);
    }

    @Override
    /** This method is only for forking purpose. Should not be used by platform directly.
     *
     */
    public void fork(Job parent,Job forkedJob) {
        CalEventHelper.writeLog("Dispatcher", "fork", "New " + forkedJob.getJobType().getName() + " forked. jobId = " + forkedJob.getJobId(), "0");
        Feed feed = null;
        try {
            feed = FeedFactory.getFeed(parent.getFeedId());
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }


        forkedJob.getJobMetaData().setParentJobId(parent.getJobId());
        parent.incrementForkedJobsCount();
        parent.addForkedJobId(forkedJob);

        if(!parent.getJobId().equalsIgnoreCase(ICipJob.ORCHESTRATOR_JOB_ID)){
            try {
                feed.getInstanceFactory().saveObjectInstance(parent.getJobId(),parent);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,"Unable to save parent job before dispatching other job. JobId: "+parent.getJobId());
            }
        }
        CipMessage message = new CipMessage(forkedJob);

        dispatchMessage(message);
    }

    /**
     * A normal CIP message to execute a job.
     * @param message
     */
    abstract protected void sendCipMessage(BaseMessage message);

    /**
     * pipeline completion notification
     * @param message
     */
    abstract protected void sendPipelineCompletionMessage(BaseMessage message);

}