package com.ebay.cip.framework.configuration.test;

import com.ebay.cip.SimbaUtil;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.FrameworkConfiguration;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
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
public class FrameworkConfigurationTest {
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
    public void testDefaultFrameworkConfiguration() throws FileNotFoundException {
        FrameworkConfiguration config = new FrameworkConfiguration("somejunk so that it pick up default");
        Assert.assertNotNull(config.getProperty("dbBacked"));
    }

    @Test
    public void testCustomFrameowkConfiguration() {
        String bucketValue = "myTest";
        Config props = null;//SimbaUtil.getConfigFromOneFile("META-INF/simba-global.conf");
        ConfigValue configValue = ConfigValueFactory.fromAnyRef(bucketValue);
        props = configValue.atKey("bucketName");
        FrameworkConfiguration config = new FrameworkConfiguration(props);
        Assert.assertNotNull(config);
        String bucketFromConfig = config.getProperty("bucketName");
        Assert.assertEquals(bucketValue, bucketFromConfig);
    }

    @Test
    public void tetGetProperties() {
        Properties props = SimbaUtil.getPropertis("META-INF/simba-global.conf");
        Assert.assertNotNull(props);
    }

    @Test
    public void testFileFrameworkConfiguration() throws FileNotFoundException {
        FrameworkConfiguration config = new FrameworkConfiguration("META-INF/simba-global.conf");
        Assert.assertNotNull(config);
        String bucketFromConfig = config.getProperty("bucketName");
        Assert.assertEquals("cip_framework", bucketFromConfig);
    }

    @Test
    public void testDefaultFrameworkConfigBean() {
        FrameworkConfigBean bean = FrameworkConfigBean.getBean();
        Assert.assertNotNull(bean);
    }

    @Test
    public void tetErroredConfiguration() {
        Config props = SimbaUtil.getConfigFromOneFile("META-INF/test-simba-global-errored.conf");
        FrameworkConfigBean.getBean(new FrameworkConfiguration(props));
    }

}
