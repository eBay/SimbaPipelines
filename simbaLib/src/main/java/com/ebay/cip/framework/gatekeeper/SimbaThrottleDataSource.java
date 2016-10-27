package com.ebay.cip.framework.gatekeeper;

import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;

/**
 * Created by hachong on 6/3/2015.
 */
public class SimbaThrottleDataSource extends BaseThrottleDataSource {

    public SimbaThrottleDataSource(String key) {
        super(key);
    }

    public SimbaThrottleDataSource(String key,int jobAllowed, int throttlePeriod){
        super(key,jobAllowed,throttlePeriod);
    }

    public ThrottleData injectThrottleData(){
        return new ThrottleData(FrameworkConfigBean.getBean().getThrottlingRateLimitAllowed(),FrameworkConfigBean.getBean().getThrottlingRateLimitPeriod());
    }

    @Override
    public int getJobAllowed(){
        return data.getJobAllowed();
    }
    @Override
    public int getThrottlePeriod(){
        return  data.getThrottlePeriod();
    }




}
