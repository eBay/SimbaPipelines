package com.ebay.cip.framework.gatekeeper;

/**
 * Created by hachong on 6/3/2015.
 */
public abstract class BaseGateKeeper implements GateKeeperProtocol {

    protected BaseGateKeeper(){
    }

    protected void validateCounter(Counter counter,BaseThrottleDataSource dataSource){
        if((System.currentTimeMillis() - counter.getLastCounterUpdated()) > dataSource.getThrottlePeriod()){
            System.out.println("Reset Count");
            counter.resetCount();
        }
    }
}
