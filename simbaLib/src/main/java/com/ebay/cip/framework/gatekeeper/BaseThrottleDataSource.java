package com.ebay.cip.framework.gatekeeper;


/**
 * This class is the accessor for ThrottleData
 * Created by hachong on 6/2/2015.
 */
public abstract class BaseThrottleDataSource implements ThrottleDataSourceProtocol {

    protected String key;
    protected ThrottleData data;

    public BaseThrottleDataSource(String key){
        this.key = key;
        this.data = injectThrottleData();
    }

    public BaseThrottleDataSource(String key,int jobAllowed, int throttlePeriod){
        ThrottleData throttleData = new ThrottleData(jobAllowed,throttlePeriod);
        this.key = key;
        this.data = throttleData;
    }
}
