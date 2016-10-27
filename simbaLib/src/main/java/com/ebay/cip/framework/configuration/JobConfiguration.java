package com.ebay.cip.framework.configuration;

/**
 * Created by hachong on 4/2/2015.
 */
public interface JobConfiguration {
    /**
     * Actor who will process this job.
     */
    String ACTOR_PATH = "actorPath";

    /**
     * Requires only if you want to get notified when this job completes.
     * Provide JobProcessor who should handle onChildComplete(ICipJob) method.
     */
    String JOB_PROCESSOR = "jobProcessorName";
    String NEXT_JOB = "nextJob";
    String DISPATCHER = "dispatcher";

    /**
     * Returns value for asked configuration key
     * @param key key to lookup. Predefined key names are available in this class itself to use.
     * @return value if found in configuration
     */
    String getValue(String key);


}
