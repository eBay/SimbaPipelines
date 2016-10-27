package com.ebay.cip.framework.gatekeeper;

/**
 * Created by hachong on 6/2/2015.
 */
public interface GateKeeperProtocol {
    boolean isAllowed(String key, BaseThrottleDataSource dataSource);
    Counter getCounterDataAndValidate(String key,  BaseThrottleDataSource dataSource);
}
