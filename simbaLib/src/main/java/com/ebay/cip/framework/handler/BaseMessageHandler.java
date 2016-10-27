package com.ebay.cip.framework.handler;


import com.ebay.cip.framework.exception.FastFailException;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.util.CipClassUtil;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.dispatcher.Dispatcher;
import com.ebay.cip.framework.messages.BaseMessage;
import org.apache.commons.lang.StringUtils;


public abstract class BaseMessageHandler {

    /**
     * This method will be called to handle and process message.
     * @param message
     * @throws Exception
     */
    abstract public void onReceive(BaseMessage message) throws Exception;

    /**
     * In case of circuit breaker open, fastFail method will be called. Default implementaiton is blank. Subclass can override it.
     * @param message
     * @param e
     */
    public void onFastFail(BaseMessage message,FastFailException e) {}


    protected void handleDispatchNextJob(BaseMessage msg, JobConfiguration jobConfig) {
        Feed feed;
        try {
            feed = FeedFactory.getFeed(msg.getFeedId());
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Dispatcher dispatcher = feed.getDispatcher();
        String nextJobName = jobConfig.getValue(JobConfiguration.NEXT_JOB);
        Job job = msg.getJob();

        if (StringUtils.isNotEmpty(nextJobName)) {
            Job nextJob = CipClassUtil.getJobObject(nextJobName,job.getPipelineId(),job.getFeedId());
            dispatcher.dispatchNextJob(nextJob, job);
        }
        else {
            // If next job is empty, we consider this job as end of the pipeline/sub-pipeline. so we send this job to the parent.
            dispatcher.dispatchPipelineCompletionMessage(job);
        }
    }
}