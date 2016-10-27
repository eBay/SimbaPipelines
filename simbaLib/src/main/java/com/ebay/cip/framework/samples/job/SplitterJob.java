package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.samples.configBean.LMSConfigBean;

/**
 * Created by hachong on 3/26/2015.
 */
public class SplitterJob extends Job {

    public SplitterJob(){}

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.SplitterJob;
    }

    @Override
    public int getTimeoutPeriod() {return LMSConfigBean.getBean().getSplitterJobTimout(); }
}