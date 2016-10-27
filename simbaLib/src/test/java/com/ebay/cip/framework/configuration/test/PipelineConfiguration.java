package com.ebay.cip.framework.configuration.test;

import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.exception.ConfigurationException;
import com.ebay.cip.framework.job.Job;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by jagmehta on 8/13/2015.
 */
public class PipelineConfiguration {
    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };

    public static String samplePipeline = "\t{ \"firstJobName\": \"com.ebay.cip.framework.samples.job.SplitterJob\", \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\", \"jobs\": { \"SplitterJob\": { \"actorPath\": \"CommonActor\", \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.SplitterJobProcessor\", \"nextJob\": \"\" }, \"APIJob\": { \"actorPath\": \"ItemTradingAPIActor\", \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ApiJobProcessor\", \"nextJob\": \"\" } } }";

    @Test
    public void testDoubleRegistration() throws IOException, ConfigurationException {
        JsonBasedPipelineConfiguration configuration = JsonBasedPipelineConfiguration.getFromJson(samplePipeline);
        Pipelines.registerPipelineConfiguration("duplicate/test1",configuration);

        try {
            Pipelines.registerPipelineConfiguration("duplicate/test1",configuration);
        }catch(ConfigurationException e){
            //success
            return;
        }
        fail();
    }
    @Test
    public void testNullPipelineRegistration() throws IOException, ConfigurationException {
        try {
            Pipelines.registerPipelineConfiguration("null/test1",null);
        }catch(ConfigurationException e){
            //success
            return;
        }
        fail();
    }
    @Test
    public void testGetUnknownPipeline() throws IOException, ConfigurationException {
        assertNull(Pipelines.getPipelineConfiguration("notExists/test1"));
    }

}
