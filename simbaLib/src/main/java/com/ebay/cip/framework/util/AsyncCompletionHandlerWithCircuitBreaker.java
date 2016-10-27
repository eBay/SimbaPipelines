package com.ebay.cip.framework.util;

import com.ebay.cip.akka.circuitebreker.SimbaCircuitBreaker;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.Response;

import java.util.concurrent.Callable;

/**
 * Created by jagmehta on 1/13/2016.
 */
public abstract class AsyncCompletionHandlerWithCircuitBreaker extends AsyncCompletionHandler {

    SimbaCircuitBreaker circuitBreaker;

    abstract public Object onCompletedWithCB(Response response) throws Exception;
    abstract public void onThrowableWithCB(Throwable t)throws Exception;

    public AsyncCompletionHandlerWithCircuitBreaker(final String pipelineKey, final JobTypeEnum jobType){
        PipelineConfiguration pipelineConfiguration = Pipelines.getInstance().getPipelineConfiguration(pipelineKey);
        JobConfiguration jc = pipelineConfiguration.getJobConfiguration(jobType);
        String actorPath = jc.getValue(JobConfiguration.ACTOR_PATH);
        circuitBreaker = SimbaCircuitBreaker.getSimbaCircuitBreaker(actorPath);
    }

    protected void invokeCBOnError(final Exception e){
        if (circuitBreaker != null) {
            try {
                circuitBreaker.callWithSyncCircuitBreaker(
                        new Callable<Void>() {
                            public Void call() throws Exception {
                                if (e != null) throw e;
                                return null;
                            }
                        });
            }catch(Exception newE){/*no need to do anything */
            }
        }
    }

    @Override
    public Object onCompleted(final Response response) throws Exception {
        try {
            return onCompletedWithCB(response);
        }catch(Exception e) {
            invokeCBOnError(e);
            throw e;
        }
    }

    @Override
    public void onThrowable(Throwable t) {
        try {
            onThrowableWithCB(t);
        }catch(Exception e) {
            invokeCBOnError(e);
        }
    }
}
