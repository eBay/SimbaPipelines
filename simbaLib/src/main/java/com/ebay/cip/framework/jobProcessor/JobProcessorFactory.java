package com.ebay.cip.framework.jobProcessor;

import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hachong on 4/3/2015.
 */
public class JobProcessorFactory {
    public static final JobProcessorFactory INSTANCE = new JobProcessorFactory();
    private Map<String,ICipJobProcessor> map = new ConcurrentHashMap<String,ICipJobProcessor>();
    private JobProcessorFactory() {

    }

    /**
     * This method returns instance of ICipJobProcessor for a given fully qualified class name.
     * <li> First it tries to find from its own cache</li>
     * <li> Then it tries to see if any customized creation logic is available or not.</li>
     * <li> Finally if instance is not found then it tries to build Instance with default (blank) custroctor. It then caches it for future use.</li>
     * @param jobProcessorName
     * @return
     */
    public ICipJobProcessor getJobProcessor(String jobProcessorName){
        if(StringUtils.isEmpty(jobProcessorName)) {
            return null;
        }

        ICipJobProcessor processor = getCachedJobProcessor(jobProcessorName);
        if(processor == null){
            processor = getCustomizedJobProcessor(jobProcessorName);
        }
        if(processor == null){
            processor = getJobProcessorFromINSTANCEVariable(jobProcessorName);
        }
        if(processor == null){
            processor = getDefaultJobProcessor(jobProcessorName);
        }

        return processor;
    }

    public ICipJobProcessor getJobProcessor(String pipelineKey, JobTypeEnum jobType){
        PipelineConfiguration pipelineConfig = Pipelines.getInstance().getPipelineConfiguration(pipelineKey);
        JobConfiguration jobConfig = pipelineConfig.getJobConfiguration(jobType);
        return getJobProcessor(jobConfig.getValue(JobConfiguration.JOB_PROCESSOR));
    }

    protected ICipJobProcessor getDefaultJobProcessor(String jobProcessorName){

        try {
            Class claz;
            claz = this.getClass().getClassLoader().loadClass(jobProcessorName);
            Constructor ctr =claz.getConstructor();
            ICipJobProcessor processor = (ICipJobProcessor) ctr.newInstance();
            map.put(jobProcessorName,processor);
            return processor;
        } catch (Exception e) {
            //e.printStackTrace();
            //TODO: alert and log
        }
        return null;
    }

    /**
     * Look for a public INSTANCE variable, if found not null then use it.
     * @param jobProcessorName
     * @return
     */
    protected ICipJobProcessor getJobProcessorFromINSTANCEVariable(String jobProcessorName){

        try {
            //Class claz =Class.forName(jobProcessorName);
            Class claz;
            claz = this.getClass().getClassLoader().loadClass(jobProcessorName);
            Field instance = claz.getDeclaredField("INSTANCE");
            ICipJobProcessor processor = (ICipJobProcessor)instance.get(null);
            if(processor != null) {
                map.put(jobProcessorName, processor);
            }
            return processor;
        } catch (Exception e) {
            //e.printStackTrace();
            //TODO: alert and log
        }
        return null;
    }

    /**
     * Get cached jobprocessor instance
     * @param jobProcessorName
     * @return ICipJobProcessor
     */
    protected ICipJobProcessor getCachedJobProcessor(String jobProcessorName){
        return map.get(jobProcessorName);
    }

    /**
     * Handle any custom creation of JobProcessor here. You may want to cache it of may apply your own logic.
     * @param jobProcessorName
     * @return
     */
    protected ICipJobProcessor getCustomizedJobProcessor(String jobProcessorName){


        return null;
    }

}
