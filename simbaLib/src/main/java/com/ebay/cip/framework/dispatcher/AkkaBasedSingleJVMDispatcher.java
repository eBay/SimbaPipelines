package com.ebay.cip.framework.dispatcher;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.ebay.cip.akka.configuration.ActorConfig;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.feed.FeedStatus;
import com.ebay.cip.framework.handler.QueueMessageHandler;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.messages.*;
import com.ebay.kernel.calwrapper.CalEventHelper;

/**
 * Created by jagmehta on 3/17/2015.
 */
public class AkkaBasedSingleJVMDispatcher extends BaseDispatcher{

    /**
     * This is core method to send messages to another actor. It does following
     * <li>Finds actor for this message</li>
     * <li>Determines if queueing of the message is required</li>
     * <li>If yes, then enqueue this message. If for any reason enqueue fails then bypass queue and sends message directly to destination actor</li>
     * <li>This method also saves job instance into DB. If DB is unavailable then moves to in-memory</li>
     */
    @Override
    public void sendCipMessage(BaseMessage message) {
        //1. Find actor
        ActorSelection actor = getActorForMessage(message);
        Job job =  message.getJob();
        Feed feed;
        try {
            feed = FeedFactory.getFeed(job.getFeedId());
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }
        PipelineConfiguration pipeline = feed.getPipelineConfiguration();
        JobConfiguration jobConfig = pipeline.getJobConfiguration(job.getJobType());

        boolean queueEnabled = false;
        String queueName = null;

        ActorConfig actorConfig = ActorConfig.getActorConfig(jobConfig.getValue(JobConfiguration.ACTOR_PATH));
        if(actorConfig.getQueueConfig().isEnabled()){
            queueEnabled = true;
            queueName = actorConfig.getQueueConfig().getQueueName();
        }

        message.setJob(null);

        if(queueEnabled){
            boolean success = false;
            try {
                //remove hard reference to job object to save JVM memory
                message.setJob(null);
                System.out.println("enqueue to: "+queueName);
                success = QueueMessageHandler.getInstance().enqueue(queueName,message);

            } catch (Exception e) {
                CalEventHelper.writeWarning("sendCipMessage",e,"Unable to save job. Directly delivering to Actor. JobId->"+job.getJobId());
            }finally {
                if(!success){
                    actor.tell(message,ActorRef.noSender());
                }
            }
        }else {
            //dispatch message to destination actor to process.
            actor.tell(message, ActorRef.noSender());
        }
    }

    protected ActorSelection getActorForMessage(BaseMessage message){
        Job job = message.getJob();
        PipelineConfiguration pipeline = null;
        try {
            pipeline = FeedFactory.getFeed(job.getFeedId()).getPipelineConfiguration();
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
        }
        JobConfiguration jobConfig = pipeline.getJobConfiguration(job.getJobType());
        //1. Find ActorRef from config for this job
        ActorSelection actor = CipActorSystem.getInstance().getActor(jobConfig.getValue(JobConfiguration.ACTOR_PATH));
        return actor;
    }

    /**
     * This method will check the parent of the job.
     * it will get the actor path of the job and send the completePipelineMessage to the parentJob.
     * @param completePipelineMessage
     */
    @Override
    public void sendPipelineCompletionMessage(BaseMessage completePipelineMessage) {
        Job job = completePipelineMessage.getJob();
        ActorSelection actor;

        try {
            if (Job.ORCHESTRATOR_JOB_ID.equals(job.getParentJobId())) {
                FeedFactory.getFeed(job.getFeedId()).setFeedStatus(FeedStatus.COMPLETE);
                actor = CipActorSystem.getInstance().getActorSystem().actorSelection("/user/HttpParentActor/orchestratorRouter");
                actor.tell(completePipelineMessage, ActorRef.noSender());
            } else {
                FeedFactory.getFeed(completePipelineMessage.getJob().getFeedId()).getPostJobExecutionActor(job.getJobType()).tell(completePipelineMessage, ActorRef.noSender());
            }
        }
        catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }

    }

}