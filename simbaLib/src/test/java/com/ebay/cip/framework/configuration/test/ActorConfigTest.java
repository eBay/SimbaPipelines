package com.ebay.cip.framework.configuration.test;

import com.ebay.cip.akka.configuration.CustomActorConfig;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * Created by jagmehta on 9/23/2015.
 */
public class ActorConfigTest {

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };

    @After
    public void cleanup() {
        //CustomActorConfig.getConfiguredInstance().cleanup();
    }

    @Test
    public void testCustomActorConfig() {
        CustomActorConfig.init("META-INF/simba-actors.conf");
        CustomActorConfig config = CustomActorConfig.getConfiguredInstance();
        Assert.assertNotNull(config);
        String str = config.loadConfiguration("CommonActor");
        Assert.assertNotNull(str);
        System.out.println(str);
        str = config.loadConfiguration("dynamicActors");
        Assert.assertNotNull(str);
        System.out.println(str);
    }

}
