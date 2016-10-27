package com.ebay.cip.framework.handler;

import akka.actor.ActorRef;
import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.FastFailException;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.ICipJobProcessor;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.jobProcessor.JobProcessorFactory;
import com.ebay.cip.framework.messages.*;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.calwrapper.CalTransaction;
import com.ebay.kernel.calwrapper.CalTransactionFactory;

public class JobExecutionHandler extends BaseMessageHandler {


    public void onReceive(BaseMessage message) throws ExecuteException {
        CipMessage msg = (CipMessage) message;
        Job job = msg.getJob();
        if(job.isComplete()){
            System.out.println("job is already complete! "+job.getJobStatus());
            return;
        }


        FeedContext feedContext = job.getFeedContext();
        PipelineConfiguration pipelineConfig = Pipelines.getInstance().getPipelineConfiguration(feedContext.getPipelineKey());
        JobConfiguration jobConfig = pipelineConfig.getJobConfiguration(job.getJobType());
        ICipJobProcessor processor = JobProcessorFactory.INSTANCE.getJobProcessor(jobConfig.getValue(JobConfiguration.JOB_PROCESSOR));
        handleExecuteJob(msg, processor);
        msg.setJob(null);
        if(!job.isComplete()) {
            String jobId = job.getJobId();
            String feedId = job.getFeedId();
            Feed feed = null;
            try {
                feed = FeedFactory.getFeed(job.getFeedId());
                feed.getInstanceFactory().saveObjectInstance(jobId,job);
            } catch (InstanceProcessingException e) {
                CalEventHelper.writeException("Framework", e, true, "Unable to save the job. JobId: " + jobId);
            } catch(FeedNotFoundException e){
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
                return;
            }
            feed.getPostJobExecutionActor(job.getJobType()).tell(new WaitMessage(feedId, jobId), ActorRef.noSender());
            CipActorSystem.getInstance().getJobMonitorActor().tell(new StartMonitoringJobMessage(feedId,jobId , job.getTimeoutPeriod()), ActorRef.noSender());
            return;
        }
        handleDispatchNextJob(msg, jobConfig);
    }

    /**
     * In case of fastfail - when circuit breaker is open - this method will call processor's onFail and also set job to failure. 
     * @param message
     * @param e
     */
    public void onFastFail(BaseMessage message, FastFailException e){
        CipMessage msg = (CipMessage) message;
        Job job = msg.getJob();
        FeedContext feedContext = job.getFeedContext();
        PipelineConfiguration pipelineConfig = Pipelines.getInstance().getPipelineConfiguration(feedContext.getPipelineKey());
        JobConfiguration jobConfig = pipelineConfig.getJobConfiguration(job.getJobType());
        ICipJobProcessor processor = JobProcessorFactory.INSTANCE.getJobProcessor(jobConfig.getValue(JobConfiguration.JOB_PROCESSOR));
        processor.onFail(job, e);
        job.failure();
        msg.setJob(null);
        handleDispatchNextJob(msg, jobConfig);
    }



    /**
     * This method makes job execution/retry call and handle retry logic.
     * @param msg
     * @param processor
     * @throws Exception
     */
    protected void handleExecuteJob(CipMessage msg, ICipJobProcessor processor) throws ExecuteException {

        //execute job here including checking retry count if we need to call retry method.
        CalTransaction trans = CalTransactionFactory.create("handleExecuteJob");
        trans.setName(msg.getJob().getJobType().getName()+"_executeCall");
        Job job = msg.getJob();
        Feed feed = null;

        try {
            feed = FeedFactory.getFeed(job.getFeedId());
            job.getJobMetaData().setJobStatus(JobStatusEnum.IN_PROGRESS);
            processor.execute(job);
            if(job.getJobStatus().equals(JobStatusEnum.IN_PROGRESS))
                job.getJobMetaData().setJobStatus(JobStatusEnum.WAITING);

            trans.setStatus("0");
        }catch(ExecuteException e){
            trans.setStatus(e);
            if(msg.getRetryCount() == job.getMaxRetryCount()) {
                processor.onFail(job, e);
                return;
            }
            e.printStackTrace();
            msg.increamentRetryCount();
            throw e;
        }catch (FeedNotFoundException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }catch(Exception e){
            trans.setStatus(e);
            processor.onFail(job,new ExecuteException(e));
        }
        finally {
            try {
                feed.getInstanceFactory().saveObjectInstance(job.getJobId(), job);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework", e, true, "Unable to save the job after ExecuteJob. JobId: " + job.getJobId());
            }
            trans.completed();
        }
    }
}