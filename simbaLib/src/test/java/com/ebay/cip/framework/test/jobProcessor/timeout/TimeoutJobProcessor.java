package com.ebay.cip.framework.test.jobProcessor.timeout;

import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;

/**
 * Created by hachong on 9/24/2015.
 */
public class TimeoutJobProcessor extends BaseJobProcessor {

    @Override
    public void execute(Job job) throws ExecuteException {
        System.out.println("execute timeoutJobProcessor execute");
    }

    @Override
    public void timeout(Job job){
        System.out.println("execute timeout method in TimeoutJobProcessor");

    }


}
