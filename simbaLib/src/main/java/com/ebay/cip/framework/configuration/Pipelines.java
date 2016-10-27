package com.ebay.cip.framework.configuration;

import com.ebay.cip.framework.BaseContext;
import com.ebay.cip.framework.dispatcher.DispatcherFactory;
import com.ebay.cip.framework.exception.ConfigurationException;
import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.feed.FeedStatus;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.job.JobMetadata;
import com.ebay.cip.framework.util.CipClassUtil;
import com.ebay.cip.service.job.OrchestratorJob;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * This class is the implementation to create/fork a pipeline
 * Created by hachong on 4/2/2015.
 */
public class Pipelines {
    private static Map<String,PipelineConfiguration> pipelines = new HashMap<String,PipelineConfiguration>();
    private static final Pipelines _instance = new Pipelines();

    private Pipelines() {}

    public static Pipelines getInstance(){return _instance;}


    /**
     * When create a pipeline,it will create a pipelineId associate with the pipeline.
     * it will create a metadata of the pipeline called(JobContext).
     * depending on the configuration it either will save the instance in DB or in memory.
     * Then it will create the pipeline by sending first job depending on pipeline configuration
     * @param feed
     */
    public static void create(Feed feed){
        feed.setFeedStatus(FeedStatus.PROCESSING);

        //generate pipelineId
        String pipelineId = UUID.randomUUID().toString();

        //get pipelineConfiguration to decide whether to create DBContext or NonDBContext
        PipelineConfiguration pipelineConfiguration = feed.getPipelineConfiguration();

        //Base Context for jobContext
        new BaseContext(pipelineId,feed.getFeedId());
        //Base Context for feedContext;
        new BaseContext(feed.getFeedId(),feed.getFeedId());

        Job firstJob = CipClassUtil.getJobObject(pipelineConfiguration.getFirstJobName(),pipelineId,feed.getFeedId());
        feed.setFirstJobId(firstJob.getJobId());
        feed.getInstanceFactory().setFirstJobId(firstJob.getJobId());
        FeedFactory.updateFeed(feed);
        DispatcherFactory.getInstance().getdispatcherByName(pipelineConfiguration.getDefaultDispatcher(),pipelineConfiguration.getDefaultDispatcher()).dispatchNextJob(firstJob, OrchestratorJob.INSTANCE);

    }


    /**
     * This function is use to fork a pipeline from an existing job/Pipeline.
     *
     * @param originJob is the job who did the fork
     * @param startingJob is the forked job.
     */

    //TODO: fork on oneChildren Complete testcase
    public static void fork(Job originJob, Job startingJob){

        //generate pipelineId

        String pipelineId = UUID.randomUUID().toString();
        String feedId = originJob.getJobMetaData().getFeedId();
        Feed feed = null;
        try {
            feed = FeedFactory.getFeed(feedId);
        }catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return;
        }
        //get pipelineConfiguration to decide whether to create DBContext or NonDBContext
        PipelineConfiguration pipelineConfiguration = feed.getPipelineConfiguration();

        //create baseContext for jobContext
            new BaseContext(pipelineId,feed.getFeedId());

        startingJob.setJobMetadata(new JobMetadata(feedId));
        startingJob.setPipelineId(pipelineId);

        DispatcherFactory.getInstance().getdispatcherByName(pipelineConfiguration.getDefaultDispatcher(), pipelineConfiguration.getDefaultDispatcher()).fork(originJob, startingJob);
    }


    /**
     * Get a pipelineConfiguration object from a cache map given a pipelineKey
     * @param pipelineKey
     * @return PipelineConfiguration
     */
    public static PipelineConfiguration getPipelineConfiguration(String pipelineKey)  {
        PipelineConfiguration config =  pipelines.get(pipelineKey);
        if(config == null){
            try {
                config = JsonBasedPipelineConfiguration.getFromKey(pipelineKey);
                registerPipelineConfiguration(pipelineKey,config);
            } catch (Exception e) {
                CalEventHelper.writeException("register",e,true,"Unable to register pipeline with pipelineKey = "+pipelineKey);
            }
        }
        return config;
    }

    /**
     * register pipeline configuration without putting on the config
     * @param pipelineKey
     * @param config
     * @throws ConfigurationException
     */
    public static void registerPipelineConfiguration(String pipelineKey,PipelineConfiguration config) throws ConfigurationException{
        if(pipelines.containsKey(pipelineKey)){
            throw new ConfigurationException(String.format("Configuration already exists for %s",pipelineKey));
        }
        if(config == null){
            throw new ConfigurationException(String.format("NULL configuration is supplied for %s",pipelineKey));
        }
        pipelines.put(pipelineKey,config);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CalEventHelper.writeLog("Pipeline","register",pipelineKey+"  =  "+objectMapper.writeValueAsString(config),"0");
        } catch (Exception e) {
            CalEventHelper.writeLog("Pipeline", "register", pipelineKey+"  =  Registered but unable to display as JSON", "0");
        }

    }
}
