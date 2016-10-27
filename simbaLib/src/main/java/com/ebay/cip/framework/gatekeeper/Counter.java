package com.ebay.cip.framework.gatekeeper;


/**
 * Created by hachong on 6/3/2015.
 */
public class Counter {
    private int count;
    //in millis
    private long lastCounterUpdated;

    public Counter(int count, long lastCounterUpdated){
        this.count = count;
        this.lastCounterUpdated = lastCounterUpdated;
    }

    public int getCount(){
        return this.count;
    }

    public void setCount(int count){
        this.count = count;
    }

    public long getLastCounterUpdated(){
        return this.lastCounterUpdated;
    }

    public void incrementCount(){
        count++;
    }

    public void resetCount(){
        this.count = 0;
        this.lastCounterUpdated = System.currentTimeMillis();
    }
}
