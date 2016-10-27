package com.ebay.cip.framework.job;

/**
 * Created by hachong on 4/3/2015.
 */

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import java.io.Serializable;


public interface ICipJob extends Serializable {

    /**
     * For internal use only. Do not put any logic on top of this ourside of framework & Service.
     */
    String ORCHESTRATOR_JOB_ID = "0";
    String getJobId();
    JobTypeEnum getJobType();
    JobMetadata getJobMetaData();
    int getMaxRetryCount();
    int getTimeoutPeriod();
    String getPipelineId();


    /**
     * Returns whether job is in its final status or still executing/waiting for children to complete.
     * Note that for some job (like splitter), code may be compelte but job is still not complete.
     * Also note that failed status is considered as Complete.
     * Basically this method needs to indicate whether anything is left to execute this job or not
     * @return
     */
     boolean isComplete();

}