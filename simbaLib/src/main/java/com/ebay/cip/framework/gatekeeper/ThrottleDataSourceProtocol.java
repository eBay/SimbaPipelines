package com.ebay.cip.framework.gatekeeper;

/**
 * Created by hachong on 6/3/2015.
 */
public interface ThrottleDataSourceProtocol {
    ThrottleData injectThrottleData();
    int getJobAllowed();
    int getThrottlePeriod();

}
