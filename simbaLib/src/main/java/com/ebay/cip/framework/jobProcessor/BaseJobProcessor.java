package com.ebay.cip.framework.jobProcessor;

/**
 * Created by hachong on 4/3/2015.
 */

import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.exception.*;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.Job;
import com.ebay.kernel.cal.helper.RLogId;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.context.AppBuildConfig;
import com.ebay.soaframework.common.exceptions.ServiceException;

import java.io.PrintWriter;
import java.io.StringWriter;


public abstract class BaseJobProcessor implements ICipJobProcessor {

    @Override
    public void Initialize() throws ServiceException, ProcessingException{}

    @Override
    public void onOneChildComplete(Job childJob,JobContext parentJobContext) throws OneChildCompleteException {
        onOneChildComplete(childJob);
    }

    @Override
    public void onOneChildComplete(Job childJob) throws OneChildCompleteException {}

    /**
     * Override this method in subclass if your business logic needs to wait on some condition. Check below documentation from ICipJobProcessor
     * @param job
     * @throws ProcessingException
     */
    @Override
    public void resumeFromWaiting(Job job)throws ResumeCompleteProcessingException {}

    @Override
    public void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException {
        parentJob.success();
    }

    @Override
    public void timeout(Job job){}

    @Override
    public void onFail(Job failedJob,Exception e){
        CalEventHelper.writeException("onFail",e,true,e.getMessage());
        String rLogId = RLogId.getRLogId(true);
        AppBuildConfig appBuildConfig = AppBuildConfig.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        String calLink;
       if(appBuildConfig.isProduction() || appBuildConfig.isPreProd() || appBuildConfig.isSandbox()){
           stringBuilder.append("http://appmon.vip.ebay.com/logviewui/rlogid/");
           stringBuilder.append(rLogId);
           stringBuilder.append("/eventDetail");
           calLink = stringBuilder.toString();
           failedJob.getJobMetaData().setExceptionCALLink(calLink);
       }
        else{
           stringBuilder.append("http://appmon.vip.qa.ebay.com/logviewui/environment/raptorqasql/rlogid/");
           stringBuilder.append(rLogId);
           stringBuilder.append("/eventDetail");
           calLink = stringBuilder.toString();
           failedJob.getJobMetaData().setExceptionCALLink(calLink);
       }

        try{
            FeedFactory.getFeed(failedJob.getFeedId()).addErrorLog(failedJob.getJobId(),calLink);
        }catch(FeedNotFoundException e1){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e1,true,e1.getMessage());
        }

        try {
            if (e instanceof ExecuteException) {
                onFailExecute(failedJob, e.getCause());
            } else if (e instanceof ResumeCompleteProcessingException) {
                onFailResume(failedJob, e);
            } else if (e instanceof OneChildCompleteException) {
                onFailOneChildComplete(failedJob, e.getCause());
            } else if (e instanceof AllChildrenCompleteException) {
                onFailAllChildComplete(failedJob, e.getCause());
            }

            failedJob.failure();
        }
        catch(Exception exception){
            exception.printStackTrace();
            failedJob.failure();
        }
    }

    @Override
    public void onFailExecute(Job failedJob,Throwable e){failedJob.failure();}
    @Override
    public void onFailResume(Job failedJob,Throwable e){failedJob.failure();}
    @Override
    public void onFailOneChildComplete(Job failedChildJob,Throwable e){}
    @Override
    public void onFailAllChildComplete(Job failedJob,Throwable e){failedJob.failure();}
    @Override
    public void onFeedFail(Feed feed){}

}