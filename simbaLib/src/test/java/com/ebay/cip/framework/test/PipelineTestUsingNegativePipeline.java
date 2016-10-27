package com.ebay.cip.framework.test;

import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.test.job.*;
import com.ebay.cip.framework.test.jobProcessor.ExecuteExceptionJobProcessor;
import com.ebay.kernel.util.FileUtils;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by hachong on 8/13/2015.
 */
public class PipelineTestUsingNegativePipeline extends BasePipelineUnitTest{

    static Random random = new Random();
    
    @BeforeClass
    public static void childSetup(){
    	System.out.println("\n\n==================================== Starting Test for class PipelineTestUsingNegativePipeline====================================");
    }

    @Override
    public void onPipelineCompleteInternal(UnitTestParentJob parentJob, ICipJob lastExecutedPipelineJob) throws Throwable {

        System.out.println("Ending Test:"+System.currentTimeMillis());
        TestJob2 results = (TestJob2)lastExecutedPipelineJob;
        Payload responsePayload = results.getFeedContext().getFeedResponseData().getResponsePayload();
        boolean passed = false;
        if(responsePayload != null) {
            String response = responsePayload.getData();
            if(response != null && response.contains("true")) {
                System.out.println("Passed");
                passed = true;
            }
        }
        assertTrue("ResponsePayload has errors", passed);

    }

    private static String registerPipeline(String pipelineName,String pipelineJson) throws Exception{
        System.out.println("Registering test pipeline:- \n" + pipelineJson);
        PipelineConfiguration pipeline = JsonBasedPipelineConfiguration.getFromJson(pipelineJson);
        Pipelines.registerPipelineConfiguration(pipelineName, pipeline);
        return pipelineName;
    }

    @Before
    /**
     * Make sure that default is non db backed always.
     */
    public void beforeEachTest() {

        FrameworkJobTypeEnum.get(TestjobTypeEnum.TEST_JOB.getName());
        FrameworkConfigBean.getBean().setDbBacked(true);
    }

    /**
     * this test will test onFailResume methods
     * @throws Throwable
     */
    @Test
    public void testOnResumeFail() throws Throwable {
        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.test.job.TestJob2\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"TestJob2\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.ResumeExceptionJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    },\n" +
                "    \"TestJob3\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.ExecuteExceptionJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String name = registerPipeline("test/negative-"+random.nextInt(),pipelineJson);
        startTest(name);
    }

    @Test
    public void testOnOneChildFail() throws Throwable {
        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.test.job.TestJob2\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"TestJob2\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.oneAndAllExceptionJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    },\n" +
                "    \"TestJob3\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.ExecuteExceptionJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String name = registerPipeline("test/negative-"+random.nextInt(),pipelineJson);
        System.setProperty(ExecuteExceptionJobProcessor.FAILJOBS_KEY,"true");
        startTest(name);
    }



    @Test
    public void testCB() throws Throwable {
        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.test.job.TestJob2\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"TestJob2\": {\n" +
                "      \"actorPath\": \"CBTestActorNoProd\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.ExecuteExceptionJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    } "+
                "  }\n" +
                "}";
        String name = registerPipeline("test/negative-"+random.nextInt(),pipelineJson);

        System.setProperty(ExecuteExceptionJobProcessor.FAILJOBS_KEY,"true");
        TestJob2.maxRetry = 5;
        startTest(name);
        startTest(name);
        System.setProperty(ExecuteExceptionJobProcessor.FAILJOBS_KEY,"false");
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        startTest(name);
        Thread.sleep(1000);
    }


}
