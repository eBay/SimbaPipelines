package com.ebay.cip.framework.configuration.test;

import com.ebay.cip.SimbaUtil;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.FrameworkConfiguration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.io.FileNotFoundException;
import java.util.Properties;

/**
 * Created by jagmehta on 9/23/2015.
 */
public class FrameworkConfigBeanErrorTest {
    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };

    @Test
    public void tetErroredConfiguration() {
        Config props = SimbaUtil.getConfigFromOneFile("META-INF/test-simba-global-errored.conf");
        FrameworkConfigBean.getBean(new FrameworkConfiguration(props));
    }

}
