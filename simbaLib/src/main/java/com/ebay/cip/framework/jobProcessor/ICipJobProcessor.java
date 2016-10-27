package com.ebay.cip.framework.jobProcessor;

import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.exception.*;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.job.Job;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by hachong on 4/3/2015.
 */
public interface ICipJobProcessor {

    /**
     * This method should be called exactly once per object creation.
     * @throws ProcessingException
     * @throws ServiceException
     */
    void Initialize() throws ServiceException, ProcessingException;

    /**
     * This is the core method to execute job. This method may be called multiple
     * times in failed scenario. Logic needs to be idempotent.
     * @param job Job to be executed.
     * @throws ProcessingException
     */
    void execute(Job job) throws ExecuteException;

    /**
     * This method will be called in case execute method completes but ICipJob.isComplete() returns false.<br>
     * This method may be called multiple times till job status is in complete state or wait period is timed out.
     * You may want to check if you can resume or still need to wait.
     * If still need to waite then just return without changing job status. In that case this method will be called again after some time.
     * @param job
     * @throws ProcessingException
     */
    void resumeFromWaiting(Job job) throws ResumeCompleteProcessingException;


    /**
     * This method will be called if job is timeout due to any reason (ex: timeout due to waiting children to complete/ timeout due to waiting service call)
     * @param job
     */
    void timeout(Job job);

    /**
     * This method will be called if job execution gets exception and all retries are
     * completed. This method logic should be idempotent.
     * @param failedJob
     */
    void onFail(Job failedJob,Exception e);
    void onFailExecute(Job failedJob,Throwable e);
    void onFailResume(Job failedJob,Throwable e);
    void onFailOneChildComplete(Job failedChildJob,Throwable e);
    void onFailAllChildComplete(Job failedJob,Throwable e);


    /**
     * This method will be called when one child is completed (status by ICipJob.isComplete())
     * Note that it will be called only if pipeline is configured to get notification.
     * @param childJob
     * @throws ProcessingException
     */
    void onOneChildComplete(Job childJob) throws OneChildCompleteException;

    /**
     * This method will be called when one child is completed (status by ICipJob.isComplete())
     * Note that it will be called only if pipeline is configured to get notification.
     * @param childJob
     * @param parentJobContext
     * @throws OneChildCompleteException
     */
    void onOneChildComplete(Job childJob,JobContext parentJobContext) throws OneChildCompleteException;

    /**
     * This method will be called when all children are completed (status by ICipJob.isComplete())
     * Note that it will be called only if pipeline is configured to get notification.
     * @param parentJob
     */
    void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException;

    /**
     * This method will be called when feed is set to failed due to runtimeException/un-handle exception
     * @param feed
     */
    void onFeedFail(Feed feed);


}
