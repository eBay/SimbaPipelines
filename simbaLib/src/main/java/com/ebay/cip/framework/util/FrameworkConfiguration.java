package com.ebay.cip.framework.util;

import com.ebay.cip.SimbaUtil;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;

import java.io.FileNotFoundException;

/**
 * Created by hachong on 7/8/2015.
 * Loads FrameworkConfiguration. It tries to load in following order and stops where it finds.
 * <li>If Config object is supplied then uses that</li>
 * <li>If configFileName is supplied then loads that</li>
 * <li>If nothing is supplied then tries to load "META-INF/simba-global.conf"</li>
 * <li>Else tries to load simba-global-defaults.conf</li>
 * <li> if nothing is found than throws FileNotFoundException</li>
 */
public class FrameworkConfiguration {
    public static final String defaultPropFileName = "simba-global-defaults.conf";
    Config config = null;
    Config defaultConf = SimbaUtil.getConfigFromOneFile(defaultPropFileName);

    public FrameworkConfiguration(){
        try {
            loadProperty("META-INF/simba-global.conf");
        }catch(FileNotFoundException e) {//This should never happen }
        }
    }
    public FrameworkConfiguration(String configFileName) throws FileNotFoundException {
        loadProperty(configFileName);
    }
    public FrameworkConfiguration(Config properties){
        this.config = properties;
        config = config==null?defaultConf:config;
    }

    protected Config loadProperty(String propFileName) throws FileNotFoundException {


        config = SimbaUtil.getConfigFromOneFile(propFileName);
        config = config==null?defaultConf:config;
        if(config == null){
            throw new FileNotFoundException("Config file '" + propFileName + "' or '"+defaultPropFileName +"' not found in the classpath");
        }
        return config;
    }

    public String getProperty(String key) {
        try {
            return config.getValue(key).unwrapped().toString();
        }catch(Exception e){
            return defaultConf.getValue(key).unwrapped().toString();
        }
    }
    public String getJsonString(String key){
        try {
            return config.getValue(key).render(ConfigRenderOptions.concise());
        }catch (Exception e){
            return defaultConf.getValue(key).render(ConfigRenderOptions.concise());
        }
    }

}