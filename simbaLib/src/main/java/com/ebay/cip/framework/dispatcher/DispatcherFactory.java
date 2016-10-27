package com.ebay.cip.framework.dispatcher;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hachong on 4/2/2015.
 */
public class DispatcherFactory {
    private Map<String,Dispatcher> dispatcherMap = new HashMap<String,Dispatcher>();
    //private IDispatcher defaultDispatcher = new AkkaBasedSingleJVMDispatcher();
    private static DispatcherFactory _i = new DispatcherFactory();
    private DispatcherFactory() {}
    public static DispatcherFactory getInstance(){return _i;}

/*
    public Dispatcher getDispatcher(Job job){
        return getDispatcher(job.getJobType(),job.getFeedContext().getPipelineKey());
    }
    public Dispatcher getDispatcher(JobTypeEnum jobType, JobContext jobContext){
        return getDispatcher(jobType, jobContext.getPipelineKey());
    }*/
    public Dispatcher getDispatcher(JobTypeEnum jobType, String pipelineKey){
        PipelineConfiguration pipelineConfig = Pipelines.getInstance().getPipelineConfiguration(pipelineKey);
        return getDispatcher(jobType,pipelineConfig);
    }

    public Dispatcher getDispatcher(JobTypeEnum jobType, PipelineConfiguration pipelineConfig){
        JobConfiguration jobConfig = pipelineConfig.getJobConfiguration(jobType);
        String dispatcherName = jobConfig.getValue(JobConfiguration.DISPATCHER);
        return getdispatcherByName(dispatcherName,pipelineConfig.getDefaultDispatcher());
    }

    public Dispatcher getdispatcherByName(String dispatcherName,String defaultDisp){

        Dispatcher dispatcher = getdispatcherByName(dispatcherName);
        if(dispatcher == null) {
            dispatcher = getdispatcherByName(defaultDisp);
        }
        return dispatcher;
    }

    protected Dispatcher getdispatcherByName(String dispatcherName) {
        Dispatcher dispatcher = null;
        if(StringUtils.isNotBlank(dispatcherName)) {
            dispatcher = dispatcherMap.get(dispatcherName);
            if (dispatcher == null) {
                dispatcher = createDispatcher(dispatcherName);
            }
        }
        return dispatcher;
    }

    protected Dispatcher createDispatcher(String dispatcherName){
        try {
            Constructor ctr = Class.forName(dispatcherName).getConstructor();
            Dispatcher dispatcher = (Dispatcher)ctr.newInstance();
            dispatcherMap.put(dispatcherName, dispatcher);
            return dispatcher;
        } catch (Exception e) {
            e.printStackTrace();
            //TODO alert and log
        }
        return null;
    }
}
