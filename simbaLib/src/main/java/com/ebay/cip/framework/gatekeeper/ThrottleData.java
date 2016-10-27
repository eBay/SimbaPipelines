package com.ebay.cip.framework.gatekeeper;

/**
 * Created by hachong on 6/2/2015.
 */
public class ThrottleData {
    private int jobAllowed;
    private int throttlePeriod;


    public ThrottleData(int jobAllowed,int throttlePeriod){
        this.jobAllowed = jobAllowed;
        this.throttlePeriod = throttlePeriod;
    }

    public int getJobAllowed(){
        return this.jobAllowed;
    }

    public int getThrottlePeriod(){
        return this.throttlePeriod;
    }

}
