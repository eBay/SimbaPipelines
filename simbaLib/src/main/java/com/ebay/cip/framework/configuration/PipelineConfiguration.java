package com.ebay.cip.framework.configuration;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;

/**
 * Created by hachong on 4/2/2015.
 */
public interface PipelineConfiguration {
    JobConfiguration getJobConfiguration(JobTypeEnum jobType);
    void addJobConfiguration(JobTypeEnum jobType, JobConfiguration jobConfiguration);
    String findNextJobName(Job currentJob);
    String getFirstJobName();
    String getFeedFailJobProcessorName();
    String getDefaultDispatcher();
    String getFeedTimeout();
}
