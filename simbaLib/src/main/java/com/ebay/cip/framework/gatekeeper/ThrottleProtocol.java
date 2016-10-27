package com.ebay.cip.framework.gatekeeper;

/**
 * Created by hachong on 6/2/2015.
 */
public interface ThrottleProtocol {
    /**
     * This method will be executed when throttling exceeds the limit
     * @param object
     */
    void reject(Object object);

    /**
     * This method will be executed when Throttling is not happen (under the limit)
     * @param object
     */
    void pass(Object object);

    /**
     * This method is used to determine how to get the DataSource
     * @param key
     * @return
     */

    BaseThrottleDataSource getDataSource(String key);
}
