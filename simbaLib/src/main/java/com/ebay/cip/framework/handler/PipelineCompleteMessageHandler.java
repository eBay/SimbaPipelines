package com.ebay.cip.framework.handler;

import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.exception.OneChildCompleteException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.ICipJobProcessor;
import com.ebay.cip.framework.jobProcessor.JobProcessorFactory;
import com.ebay.cip.framework.messages.*;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.calwrapper.CalTransaction;
import com.ebay.kernel.calwrapper.CalTransactionFactory;

import java.lang.reflect.Method;

/**
 * This Actor handles PipelineComplete messages. It finds appropriate jobProcessor and calls onChildComplete method.
 * Created by jagmehta on 3/25/2015.
 */
public class PipelineCompleteMessageHandler extends BaseMessageHandler {

    public void onReceive(BaseMessage message) throws OneChildCompleteException {
        CipPipelineCompleteMessage msg = (CipPipelineCompleteMessage) message;
        Job job = msg.getJob();
        Job parent = job.getParentJob();
        if(parent.isComplete()){
            String logMessage = "Job "+parent.getJobId()+" is "+parent.getJobMetaData().getJobStatus()+". Will not execute onOneChildrenComplete";
            CalEventHelper.writeLog("Framework", "Timeout", logMessage, "0");
            System.out.println(logMessage);
            return;
        }

        FeedContext feedContext = job.getFeedContext();
        ICipJobProcessor processor = JobProcessorFactory.INSTANCE.getJobProcessor(feedContext.getPipelineKey(),parent.getJobType());

        handleOneChildComplete(msg,processor);
    }

    protected void handleOneChildComplete(CipPipelineCompleteMessage msg,ICipJobProcessor processor) throws OneChildCompleteException {

        Job job = msg.getJob();
        Job parent = job.getParentJob();
        Feed feed;
        try {
            feed = FeedFactory.getFeed(job.getFeedId());
        }catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }

        CalTransaction trans = CalTransactionFactory.create("OneChildComplete");
        try {
            trans.setName(msg.getJob().getJobType().getName() + "_onOneChildCompleteCall");
            invokeOnOneChildComplete(processor, job, parent);
            trans.setStatus("0");
            ForkedJobCompletionCounterHandler.childrenComplete(parent.getJobId(),job.getJobId(),job.getPipelineId(),job.getJobStatus(),feed);
        } catch (OneChildCompleteException e) {
            trans.setStatus(e);

            if(msg.getRetryCount() == job.getMaxRetryCount()) {
                try {
                    processor.onFail(job, e);
                    job.failure();
                    return;
                }
                finally{
                    ForkedJobCompletionCounterHandler.childrenComplete(parent.getJobId(),job.getJobId(),job.getPipelineId(),job.getJobStatus(),feed);
                }
            }
            e.printStackTrace();
            msg.increamentRetryCount();
            throw e;
        } catch(Exception e){
            processor.onFail(job,new OneChildCompleteException(e));
            job.failure();
            ForkedJobCompletionCounterHandler.childrenComplete(parent.getJobId(),job.getJobId(),job.getPipelineId(),job.getJobStatus(),feed);
            trans.setStatus(e);
        } finally{
            trans.completed();
            try {
                feed.getInstanceFactory().saveObjectInstance(job.getJobId(), job);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,"Unable to save job. JobId: "+job.getJobId());
            }

        }
    }

    /**
     * Invokes appropriate onOneChildCompete method. If JobProcesor has override {@link com.ebay.cip.framework.jobProcessor.BaseJobProcessor#onOneChildComplete(Job, JobContext)} then
     * it will call that method. Otherwise it will call {@link com.ebay.cip.framework.jobProcessor.BaseJobProcessor#onOneChildComplete(Job)}
     * This is done to avoid fetching JobContext from DB if not required.
     * @param processor
     * @param job
     * @param parent
     * @throws OneChildCompleteException
     */
    protected void invokeOnOneChildComplete(ICipJobProcessor processor, Job job, Job parent) throws OneChildCompleteException {
        try {
            Method method = processor.getClass().getMethod("onOneChildComplete", Job.class, JobContext.class);
            if(method != null && method.getDeclaringClass().equals(processor.getClass())) {
                processor.onOneChildComplete(job, parent.getJobContext());
            }else {
                processor.onOneChildComplete(job);
            }
        } catch (NoSuchMethodException e) {
            processor.onOneChildComplete(job);
        }
    }

}