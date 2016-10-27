package com.ebay.cip.framework.handler;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.exception.AllChildrenCompleteException;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.exception.ResumeCompleteProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.ICipJobProcessor;
import com.ebay.cip.framework.jobProcessor.JobProcessorFactory;
import com.ebay.cip.framework.messages.*;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.CipClassUtil;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.calwrapper.CalTransaction;
import com.ebay.kernel.calwrapper.CalTransactionFactory;
import scala.concurrent.duration.Duration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by hachong on 7/30/2015.
 */
public class WaitMessageHandler extends BaseMessageHandler {
    @Override
    public void onReceive(BaseMessage message) throws ResumeCompleteProcessingException,AllChildrenCompleteException{
        WaitMessage waitMessage = (WaitMessage) message;
        Job job = waitMessage.getJob();


        FeedContext feedContext = job.getFeedContext();

        PipelineConfiguration pipeline = Pipelines.getInstance().getPipelineConfiguration(feedContext.getPipelineKey());
        JobConfiguration jobConfig = pipeline.getJobConfiguration(job.getJobType());
        ICipJobProcessor processor = JobProcessorFactory.INSTANCE.getJobProcessor(jobConfig.getValue(JobConfiguration.JOB_PROCESSOR));

        if (!waitMessage.runResume) {
            String notes = "warning! runResume is not executed!";
            CalEventHelper.writeLog("Framework", "WaitMessageHandler", notes, "0");
        }

        if (waitMessage.runResume) {
            handleWaitForJobToComplete(waitMessage, processor);
        }

        //check if this job is waiting due to fork
        if (job.getForkedJobsCount() > 0) {
            if (job.isComplete()) {
                String logMessage = "Job " + job.getJobId() + " is " + job.getJobMetaData().getJobStatus() + ". Will not execute onAllChildrenComplete";
                CalEventHelper.writeLog("Framework", "Timeout", logMessage, "0");
            } else {
                int completedJobs = ForkedJobCompletionCounterHandler.getCount(job.getJobId());
                if (job.isForkComplete() && completedJobs >= job.getForkedJobsCount()) {
                    handleAllChildrenComplete(waitMessage, processor);
                    ForkedJobCompletionCounterHandler.remove(job.getJobId());
                }
            }
        }

        if (!job.isComplete()) {
            waitMessage.setJob(null);
            ActorRef pipelineCompleteActor;
            try {
                pipelineCompleteActor = FeedFactory.getFeed(waitMessage.getJob().getFeedId()).getPostJobExecutionActor(job.getJobType());
            } catch (FeedNotFoundException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
                return;
            }
            ActorSystem system = CipActorSystem.getInstance().getActorSystem();
            system.scheduler().scheduleOnce(Duration.create(FrameworkConfigBean.getBean().getDelayPeriod(), TimeUnit.SECONDS), pipelineCompleteActor, waitMessage, system.dispatcher(), null);
            return;
        }

        handleDispatchNextJob(waitMessage, jobConfig);

    }

    private void handleWaitForJobToComplete(WaitMessage msg, ICipJobProcessor processor) throws ResumeCompleteProcessingException {

        Job job = msg.getJob();
        Job old = job;
        byte[] oldHash = null;
        byte[] newHash = null;
        if (job != null) {
            try {
                oldHash = CipClassUtil.getSHA1(job);
            } catch (Exception e) {
                CalEventHelper.writeWarning("Hash", e, "unable to compute hash JobId->" + job.getJobId());
            }
        }

        CalTransaction trans = CalTransactionFactory.create("ResumeFromWaiting");
        try {
            trans.setName(msg.getJob().getJobType().getName() + "_resumeFromWaitingCall");
            processor.resumeFromWaiting(job);
            trans.setStatus("0");
            trans.completed();
            if (job != null) {
                try {
                    newHash = CipClassUtil.getSHA1(job);
                } catch (Exception e) {
                    CalEventHelper.writeWarning("Hash", e, "unable to compute hash JobId->" + job.getJobId());
                }
            }

        } catch(ResumeCompleteProcessingException e){
            if(msg.getRetryCount() == job.getMaxRetryCount()) {
                processor.onFail(job, e);
                job.failure();
                msg.runResume = false;
                trans.setStatus(e);
                trans.completed();
                return;
            }
            e.printStackTrace();
            msg.increamentRetryCount();
            trans.setStatus(e);
            trans.completed();
            throw e;

        }catch (Exception e) {
            processor.onFail(job,new ResumeCompleteProcessingException(e));
            trans.setStatus(e);
            trans.completed();
            msg.runResume = false;
        } finally {
            if (!Arrays.equals(newHash, oldHash)) {
                try {
                    Feed feed = FeedFactory.getFeed(job.getFeedId());
                    feed.getInstanceFactory().saveObjectInstance(job.getJobId(), job);
                } catch (InstanceProcessingException e) {
                    e.printStackTrace();
                    CalEventHelper.writeException("Framework",e,true,"Unable to save the job . JobId: "+job.getJobId());
                } catch(FeedNotFoundException e){
                    e.printStackTrace();
                    CalEventHelper.writeException("Framework",e,true,e.getMessage());
                }
            }
        }
    }

    private void handleAllChildrenComplete(final WaitMessage msg,ICipJobProcessor processor) throws AllChildrenCompleteException{
        Job job = msg.getJob();
        Feed feed;
        try{
            feed = FeedFactory.getFeed(job.getFeedId());
        }
        catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }



        CalTransaction trans = CalTransactionFactory.create("AllChildComplete");
        try {
            trans.setName(msg.getJob().getJobType().getName() + "_onAllChileCompleteCall");
            processor.onAllChildrenComplete(job);
            trans.setStatus("0");
        }
        catch (AllChildrenCompleteException e) {
            trans.setStatus(e);
            if(msg.getRetryCount() == job.getMaxRetryCount()) {
                processor.onFail(job, e);
                job.failure();
                return;
            }
            e.printStackTrace();
            msg.increamentRetryCount();
            throw e;
        }catch(Exception e){
            trans.setStatus(e);
            processor.onFail(job, new AllChildrenCompleteException(e));
            job.failure();
        }finally{
            InstanceFactory instanceFactory = feed.getInstanceFactory();
            try {
                instanceFactory.saveObjectInstance(job.getJobId(), job);
            } catch (InstanceProcessingException e) {
                e.printStackTrace();
                CalEventHelper.writeException("Framework",e,true,"Unable to save the job . JobId: "+job.getJobId());
            }
            trans.completed();
            //return;
        }
    }
}
