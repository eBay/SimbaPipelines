package com.ebay.cip.framework.configuration;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.es.cipconfig.cipconfig.common.ConfigConstants;
import com.ebay.es.cipconfig.cipconfig.core.ConfigContext;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.contextual.mongo.MongoModuleInitializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jagmehta on 3/18/2015.
 */
public class JsonBasedPipelineConfiguration implements PipelineConfiguration {
    private String firstJobName;
    private String feedFailJobProcessorName;
    private String defaultDispatcher;
    private String feedTimeout;
    private Map<JobTypeEnum,JobConfiguration> jobs;

    static {
        new MongoModuleInitializer().init();
    }

    public static JsonBasedPipelineConfiguration getFromKey (String pipelineKey) throws IOException {
        String configString = getConfigString(pipelineKey);
        return getFromJson(configString);
    }

    public static JsonBasedPipelineConfiguration getFromJson (String configString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonBasedPipelineConfiguration p = objectMapper.readValue(configString,JsonBasedPipelineConfiguration.class);
        return p;
    }


    @Override
    public JobConfiguration getJobConfiguration(JobTypeEnum jobType) {
        return jobs.get(jobType);
    }

    @Override
    public void addJobConfiguration(JobTypeEnum jobType, JobConfiguration jobConfiguration) {
        if (jobType != null && jobConfiguration != null){
            this.jobs.put(jobType, jobConfiguration);
        }else {
            throw new RuntimeException("jobType or jobConfiguration is null");
        }
    }

    @Override
    public String findNextJobName(Job currentJob) {
        return jobs.get(currentJob.getJobType()).getValue(JobConfiguration.NEXT_JOB);
    }

    @Override
    public String getFirstJobName() {
        return firstJobName;
    }

    @Override
    public String getFeedFailJobProcessorName() {
        return feedFailJobProcessorName;
    }

    @Override
    public String getDefaultDispatcher() {
        return this.defaultDispatcher;
    }

    @Override
    public String getFeedTimeout() {
        return feedTimeout;
    }


    /* Setters for Json */

    public void setFirstJobName(String firstJobName){
        this.firstJobName = firstJobName;
    }
    public void setDefaultDispatcher(String dispatcher){
        this.defaultDispatcher = dispatcher;
    }
    public void setFeedFailJobProcessorName(String jobProcessorName){ this.feedFailJobProcessorName = jobProcessorName;}
    public void setFeedTimeout(String feedTimeout){ this.feedTimeout = feedTimeout;}


    public void setJobs(Map<String, Map<String, String>> jobJson){

        jobs = new HashMap<>();
        for (Map.Entry<String,Map<String,String>> entry : jobJson.entrySet()) {
            String key = entry.getKey();
            Map<String, String> jobConfigMap = entry.getValue();

            JobTypeEnum jobType = JobTypeEnum.get(key);
            JobConfigurationImpl jobConfiguration = new JobConfigurationImpl(jobConfigMap);
            jobs.put(jobType, jobConfiguration);
        }
    }




    /**
     * Mock method. Do not use it in real code.
     * @return
     */
    private static String getConfigString(String key){

        String configString = null;

        /***********************************************************************************/
        //TODO DO NOT CHECKING YOUR TEST PIPELINES IN git. It breaks others workspace
        /***********************************************************************************/

        configString = ConfigContext.getString(ConfigConstants.PIPELINE_CONFIG_ID, key);
        return configString;
    }
}