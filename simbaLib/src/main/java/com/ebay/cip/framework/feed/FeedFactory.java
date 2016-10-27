package com.ebay.cip.framework.feed;

import akka.actor.ActorRef;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.jobProcessor.JobProcessorFactory;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.cip.service.FeedRequestData;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hachong on 7/24/2015.
 */
public class FeedFactory {

    protected static Map<String,Object> feedsMap = new ConcurrentHashMap<>();

    public static Feed start(FeedRequestData requestData,String machineName){

        //create feedId
        String feedId = UUID.randomUUID().toString();
        System.out.println("Start feed: "+feedId);
        Feed feed = new Feed(feedId,requestData);
        feed.setStartTime(new DateTime(DateTimeZone.UTC).toString());
        feed.setMachineName(machineName);

        feedsMap.put(feedId, feed);
        CalEventHelper.writeLog("Framework","Feed","Feed created: "+feedId,"0");

        //save to DB for monitoring
        try {
            feed.getInstanceFactory().saveObjectInstanceNonKyro(feedId, feed);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            StringBuffer message = new StringBuffer();
            message.append("Unable to save feed with feedId: ").append(feed.getFeedId()).append("during start feed");
            CalEventHelper.writeException("DBError", e, true, message.toString());
        }


        return feed;

    }

    public static Feed getFeed(String feedId) throws FeedNotFoundException{
        Feed feed = (Feed) feedsMap.get(feedId);
        if(feed == null)
            throw new FeedNotFoundException("Feed not found in the feedsMap");
        return feed;
    }

    public static void updateFeed(Feed feed){
        String feedId = feed.getFeedId();
        CalEventHelper.writeLog("Framework","Feed","Feed Updated: "+feedId,"0");
        feedsMap.put(feedId,feed);

        try {
            feed.getInstanceFactory().saveObjectInstanceNonKyro(feedId, feed);
        } catch (InstanceProcessingException e) {
            StringBuffer message = new StringBuffer();
            message.append("Unable to save feed with feedId: ").append(feed.getFeedId()).append("during update feed");
            CalEventHelper.writeException("DBError", e, true, message.toString());
        }
    }

    public static void done(Feed feed){
        if(feed == null){
            CalEventHelper.writeLog("Framework","Feed","unable to retrieve feed","0");
            return;
        }
        feed.setFeedStatus(FeedStatus.COMPLETE);
        feed.setEndTime(new DateTime(DateTimeZone.UTC).toString());
        cleanUp(feed);
        CalEventHelper.writeLog("Framework","Feed","Feed Complete: "+feed.getFeedId(),"0");
        System.out.println("Feed Complete:"+feed.getFeedId());
    }


    public static boolean fail(Feed feed,String stackTrace){
        if(feed == null ){
            CalEventHelper.writeLog("Framework","Feed","Feed is null","0");
            return false;
        }
        CalEventHelper.writeLog("Framework","Feed","Feed Fail: "+feed.getFeedId(),"0");

        PipelineConfiguration pipelineConfiguration =  Pipelines.getPipelineConfiguration(feed.getFeedRequestData().getPipelineKey());
        String jobProcessorName = pipelineConfiguration.getFeedFailJobProcessorName();
        if(jobProcessorName != null){
            try {
                JobProcessorFactory.INSTANCE.getJobProcessor(jobProcessorName).onFeedFail(feed);
            }
            catch(Exception e){
                StringBuffer message = new StringBuffer().append("exception occurs onFeedFail. Feed id: ").append(feed.getFeedId());
                CalEventHelper.writeException("Framework", e, true, message.toString());
            }
        }
        else{
            System.out.println("You didn't define any onFeedFail in pipeline!");
            CalEventHelper.writeLog("Framework","You didn't define any onFeedFail in pipeline!",feed.getFeedId(),"0");
        }
        feed.setFeedStatus(FeedStatus.FAILURE);
        feed.setNote(stackTrace);
        feed.setEndTime(new DateTime(DateTimeZone.UTC).toString());
        return cleanUp(feed);
    }

    private static boolean cleanUp(Feed feed){
        try {
            feed.getInstanceFactory().saveObjectInstanceNonKyro(feed.getFeedId(), feed);
            ConcurrentHashMap postExecutionActorMap = feed.getPostExecutionActorMap();

            if(!postExecutionActorMap.isEmpty()) {
                for (Object entry : postExecutionActorMap.entrySet()) {
                    Map.Entry<JobTypeEnum, ActorRef> e = (Map.Entry<JobTypeEnum, ActorRef>) entry;
                    CipActorSystem.getInstance().getParentActorInstance().stopActor(e.getValue());
                }
            }
            if(feedsMap.containsKey(feed.getFeedId()))
                feedsMap.remove(feed.getFeedId());
            return true;
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            StringBuffer message = new StringBuffer();
            message.append("Unable to save feed with feedId: ").append(feed.getFeedId()).append(" during clean up");
            CalEventHelper.writeException("DBError", e, true, message.toString());
            return false;
        }
    }

    public static boolean abortFeed(String feedId){
        Feed feed;

        try{
            feed = getFeed(feedId);
            if(feed.getFeedStatus() == FeedStatus.COMPLETE || feed.getFeedStatus() == FeedStatus.FAILURE)
                return false;
            return fail(feed,"Abort feed on demand");
        } catch (FeedNotFoundException e) {
            e.printStackTrace();
            InstanceFactory instanceFactory = new InstanceFactory(true);
            JSONObject json = instanceFactory.getFeedJSONObject(feedId,Feed.class);
            if(json != null) {
                try {
                    feed = new ObjectMapper().readValue(json.toString(), Feed.class);

                    if(feed.getFeedStatus() == FeedStatus.COMPLETE || feed.getFeedStatus() == FeedStatus.FAILURE)
                        return false;

                    feed.setPipelineConfiguration(Pipelines.getPipelineConfiguration(feed.getFeedRequestData().getPipelineKey()));
                    feed.setInstanceFactory(instanceFactory);
                    return fail(feed,"Abort feed on demand");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return false;
                }
            }
            return false;
        }
    }

    public static String[] getRunningFeedIds() {
        Set<String> keys = feedsMap.keySet();
        String[] strKeys = new String[keys.size()];
        return feedsMap.keySet().toArray(strKeys);
    }
}
