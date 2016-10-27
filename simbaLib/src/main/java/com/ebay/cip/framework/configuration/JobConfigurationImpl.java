package com.ebay.cip.framework.configuration;
import java.util.Map;

/**
 * Created by hachong on 4/2/2015.
 */
public class JobConfigurationImpl implements JobConfiguration {

    protected Map<String,String> jobs;

    public JobConfigurationImpl(Map<String, String> data) {
        this.jobs = data;
    }

    /**
     * Returns value for asked configuration key
     * @param key key to lookup. Predefined key names are available in this class itself to use.
     * @return value if found in configuration
     */
    public String getValue(String key){
        return jobs.get(key);
    }


}