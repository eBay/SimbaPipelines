package com.ebay.cip.framework.handler;

import akka.actor.ActorRef;
import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.ForkMetadata;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.ICipJobProcessor;
import com.ebay.cip.framework.jobProcessor.JobProcessorFactory;
import com.ebay.cip.framework.messages.BaseMessage;
import com.ebay.cip.framework.messages.TimeoutMessage;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.calwrapper.CalTransaction;
import com.ebay.kernel.calwrapper.CalTransactionFactory;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;

import java.util.Map;

/**
 * Created by hachong on 9/21/2015.
 */
public class TimeoutHandler extends BaseMessageHandler{

    static Logger logger = Logger.getInstance(TimeoutHandler.class);

    public TimeoutHandler(){

    }

    @Override
    public void onReceive(BaseMessage message) throws Exception {
        Job job = message.getJob();
        String jobId = job.getJobId();
        String feedId = job.getFeedId();

        if(job.isComplete()){
            CalEventHelper.writeLog("framework", "Job timeout", "Job "+jobId+" is already completed", "0");
            logger.log(LogLevel.INFO,"Job: " + jobId+ " is already completed");
            return;
        }
        else{
            FeedContext feedContext = job.getFeedContext();
            ICipJobProcessor processor = JobProcessorFactory.INSTANCE.getJobProcessor(feedContext.getPipelineKey(),job.getJobType());
            job.timeout();

            CalTransaction trans = CalTransactionFactory.create("timeoutHandler");
            trans.setName(job.getJobType().getName() + "_timeoutCall");
            try {
                processor.timeout(job);
            }
            catch(Exception e){
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
            }
            trans.setStatus("0");
            trans.completed();
            Feed feed;

            try {
                feed = FeedFactory.getFeed(feedId);

            }catch(FeedNotFoundException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
                return;
            }

            if(job.getForkedJobsCount() > 0){
                ForkMetadata forkMetadata = ForkedJobCompletionCounterHandler.getForkMetadata(jobId);

                Map<String,JobStatusEnum> childrenMap = forkMetadata.getChildrenMap();
                for(Map.Entry<String,JobStatusEnum> entry : childrenMap.entrySet()){
                    if(JobStatusEnum.WAITING.equals(entry.getValue())|| JobStatusEnum.CREATED.equals(entry.getValue())){
                        CipActorSystem.getInstance().getJobMonitorActor().tell(new TimeoutMessage(feedId,entry.getKey()), ActorRef.noSender());
                    }
                }
                ForkedJobCompletionCounterHandler.remove(job.getJobId());
            }
            try {
                feed.getInstanceFactory().saveObjectInstance(job.getJobId(), job);
            }
            catch(InstanceProcessingException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,"Unable to save the job . JobId: "+job.getJobId());
            }
        }
    }

}
