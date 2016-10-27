package com.ebay.cip.framework.handler;

import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.job.ForkMetadata;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Counter that keeps track of completed child/fork jobs
 * TODO this should be backed by DB Automic operation. Right now its in Memory
 * Created by jagmehta on 4/7/2015.
 */
public class ForkedJobCompletionCounterHandler {
    static Map<String,ForkMetadata> counter = new ConcurrentHashMap<>();
    static Logger logger = Logger.getInstance(ForkedJobCompletionCounterHandler.class);

    public static Integer getCount(String jobId){
        ForkMetadata forkMetadata =  getForkMetadata(jobId);
        return forkMetadata.getCount();
    }


    public static void addChildrenId(String jobId,String childrenJobId,String pipelineId,Feed feed){
        ForkMetadata forkMetadata =  getForkMetadata(jobId);
        forkMetadata.addChildren(childrenJobId);
        String data = ","+pipelineId+ ":" + childrenJobId + ":" + JobStatusEnum.CREATED.getName();
        feed.getInstanceFactory().appendAsync(jobId, ForkMetadata.class, data, (forkMetadata.getChildrenMap().size() == 1));
    }

    /**
     * Putting synchronized to be on safer side - in case in future someone start using in multiple thread.
     * This method is can be non synchronized because PostJobExecutionActor is only one actor and calls to this method
     * will always be one by one.
     * Once we go to DB, we need utilize DB specific functions to synchronize increments.
     * @param jobId the parent job id that fork this children
     * @param childrenJobId the children job id that want to change it status
     * @param childrenStatus the children job status
     */
     public static void childrenComplete(String jobId,String childrenJobId,String pipelineId,JobStatusEnum childrenStatus,Feed feed){
        ForkMetadata forkMetadata =  getForkMetadata(jobId);
        forkMetadata.increaseCount();
        forkMetadata.changeChildrenStatus(childrenJobId, childrenStatus);
         feed.getInstanceFactory().appendAsync(jobId, ForkMetadata.class, ","+pipelineId+ ":"  + childrenJobId + ":" + childrenStatus.getName(), false);
    }

    public static ArrayList<String> getUncompletedChildren(String jobId){
        ForkMetadata forkMetadata = getForkMetadata(jobId);
        ArrayList<String> listOfUncompletedChildren = new ArrayList<String>();
        Map<String,JobStatusEnum> childrenMap = forkMetadata.getChildrenMap();
        for(Map.Entry<String,JobStatusEnum> entry : childrenMap.entrySet()){
            if(JobStatusEnum.SUCCESS.equals(entry.getValue()) || JobStatusEnum.FAILURE.equals(entry.getValue()) || JobStatusEnum.TIMEOUT.equals(entry.getValue())){
                listOfUncompletedChildren.add(entry.getKey());
            }
        }
        return listOfUncompletedChildren;
    }


    public static void remove(String jobId){
        CalEventHelper.writeLog("Framework", "Throttling", "remove fork counter for jobId " + jobId,"0");
        logger.log(LogLevel.INFO,"remove fork counter for jobId: "+jobId);
        counter.remove(jobId);
    }

    public static ForkMetadata getForkMetadata(String jobId){
        ForkMetadata forkMetadata =  counter.get(jobId);
        if (forkMetadata == null){
            forkMetadata = new ForkMetadata();
            counter.put(jobId,forkMetadata);
        }
        return forkMetadata;
    }

}
