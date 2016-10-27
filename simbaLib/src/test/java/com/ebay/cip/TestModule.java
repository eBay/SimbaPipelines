package com.ebay.cip;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.samples.configBean.LMSConfigBean;
import com.ebay.es.cipconfig.cipconfig.StorageClientConfig;
import com.ebay.es.cipconfig.cipconfig.common.ConfigConstants;
import com.ebay.es.cipconfig.cipconfig.core.ConfigContext;
import com.ebay.es.cipconfig.cipconfig.core.ConfigParams;
import com.ebay.es.cipconfig.cipconfig.core.ConfigTarget;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.logger.LogLevel;

/**
 * Created by jagmehta on 10/29/2015.
 */
public class TestModule {

    public static void init() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);
        
        FrameworkConfigBean.getBean();
        
		ConfigParams param = new ConfigParams();
        param.setProject(ConfigConstants.PROJECT_CIP);
        param.setTarget(ConfigTarget.Global);
        param.setVersion(ConfigConstants.VERSION_1_0_0);
        ConfigContext.registerConfig(param,FrameworkConfigBean.getBean().isCreateRaptorConfigBackup());

        com.ebay.cip.framework.samples.Module.init();
        LMSConfigBean.initialize();
        try {
            StorageClientConfig.getInstance().init();
        }catch(Exception e){
            CalEventHelper.writeException("Initialize",e,true,"Unable to start storageClient from framework");
        }
        
        root.setLevel(Level.INFO);
    }
    
//    @Test
//    public void setup(){
//        TestModule.init();
//    }

}
