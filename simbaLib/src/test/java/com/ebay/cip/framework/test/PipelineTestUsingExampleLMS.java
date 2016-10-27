package com.ebay.cip.framework.test;


import com.ebay.cip.Cip;
import com.ebay.cip.TestModule;
import com.ebay.cip.akka.configuration.CustomActorConfig;
import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.feed.FeedStatus;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.samples.configBean.LMSConfigBean;
import com.ebay.cip.framework.samples.job.SplitterJob;
import com.ebay.cip.framework.test.job.TestJob2;
import com.ebay.cip.framework.test.job.UnitTestParentJob;
import com.ebay.cip.framework.test.jobProcessor.ExecuteExceptionJobProcessor;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.kernel.util.FileUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;
/**
 * Created by jagmehta
 */
public class PipelineTestUsingExampleLMS extends BasePipelineUnitTest {

    @BeforeClass
    public static void setup(){
    	System.out.println("\n\n==================================== Starting Test for class PipelineTestUsingExampleLMS====================================");
        //BasePipelineUnitTest.setup();
    }
    

    static Random random = new Random();
    @Override
    public void onPipelineCompleteInternal(UnitTestParentJob parentJob,ICipJob lastExecutedPipelineJob) throws Throwable {

        SplitterJob results = (SplitterJob)lastExecutedPipelineJob;
        Payload responsePayload = results.getFeedContext().getFeedResponseData().getResponsePayload();
        boolean passed = false;
        if(responsePayload != null) {
            String response = responsePayload.getData();
            if(response != null && response.contains("<Ack>")) {
                System.out.println("Passed");
                passed = true;
            }
        }
        assertTrue("ResponsePayload has errors",passed);
    }

    /**
     * Register the example LMS pipeline with given apiActor
     * @param apiActor
     * @return registered name.
     * @throws Exception
     */
    public static String registerPipeline(String apiActor) throws Exception{

        //String actorPath = "ItemTradingAPIActor";
        String pipelineName = "example/LMS-"+apiActor+random.nextInt();

        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.samples.job.SplitterJob\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"SplitterJob\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.SplitterJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    },\n" +
                "    \"APIJob\": {\n" +
                "      \"actorPath\": \""+apiActor+"\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ApiJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        System.out.println("Registering test pipeline:- \n"+pipelineJson);

        PipelineConfiguration pipeline = JsonBasedPipelineConfiguration.getFromJson(pipelineJson);
        Pipelines.registerPipelineConfiguration(pipelineName, pipeline);
        return pipelineName;
    }

    /**
     * Make sure that default is non db backed always.
     */
    @Override
    protected void beforeSetup(){
        FrameworkConfigBean.getBean().setDbBacked(false);
        FrameworkConfigBean.getBean().setThrottlingRateLimitPeriod(1);
        FrameworkConfigBean.getBean().setThrottlingRateLimitAllowed(2000);
    }

    @Test
    public void testDefault() throws Throwable {
        System.out.println(FrameworkConfigBean.getBean().getTimeoutPeriod());
        String name = registerPipeline("ItemTradingAPIActor");
        startTest(name, getDefaultPayload());
    }

    @Test
    public void testDefaultDBBacked() throws Throwable {
        FrameworkConfigBean.getBean().setDbBacked(true);
        String name = registerPipeline("ItemTradingAPIActor");
        startTest(name,getDefaultPayload());
    }



    @Test
    public void testDefaultQueueThrottle() throws Throwable {
        FrameworkConfigBean.getBean().setDbBacked(true);
        FrameworkConfigBean.getBean().setThrottlingRateLimitPeriod(5);
        FrameworkConfigBean.getBean().setThrottlingRateLimitAllowed(1);
        FrameworkConfigBean.getBean().setCreateRaptorConfigBackup(false);
        String name = registerPipeline("APIThrottleActor");
        startTest(name,get20RequestPayload());
    }

    @Test
    public void testMultipleIteration() throws Throwable {
        String name = registerPipeline("ItemTradingAPIActor");
        addHeader("X-EBAY-TEST-ITERATION", "5");
        startTest(name, getDefaultPayload());
    }


    private String getDefaultPayload() throws IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ExampleLMSPayload.xml");
        String content = FileUtils.readStream(stream);
        return content;
   }

    private String get20RequestPayload() throws IOException {
        //ExamplLMSPayload_20Requests
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ExamplLMSPayload_20Requests.xml");
        String content = FileUtils.readStream(stream);
        return content;
    }

    @Test
    public void testCBForAsyncClient() throws Throwable {

        String name = registerPipeline("AsyncCBTestActorNoProd");
        String originalURL = LMSConfigBean.getBean().getTradingApiURL();
        LMSConfigBean.getBean().setTradingApiURL("http://somejunk.junk.ebay.com/abc.dll");
        startTest(name, get20RequestPayload());
        startTest(name, get20RequestPayload());

        LMSConfigBean.getBean().setTradingApiURL(originalURL);
        startTest(name, get20RequestPayload());

        Thread.sleep(5000);
        startTest(name,get20RequestPayload());
        startTest(name, get20RequestPayload());
        Thread.sleep(1000);
    }

    @Test
    public void testGetAllFeeds() {
        InstanceFactory instanceFactory = new InstanceFactory(true);
        String jsonString = null;
        jsonString = instanceFactory.getAllFeeds(FeedStatus.COMPLETE, 1);
        System.out.println("Complete: "+jsonString);
        assertNotNull(jsonString);
        jsonString = instanceFactory.getAllFeeds(FeedStatus.CREATED, 1);
        System.out.println("CREATED: "+jsonString);
        assertNotNull(jsonString);
        jsonString = instanceFactory.getAllFeeds(FeedStatus.PROCESSING, 1);
        System.out.println("PROCESSING: " + jsonString);
        assertNotNull(jsonString);
        jsonString = instanceFactory.getAllFeeds(FeedStatus.FAILURE, 1);
        System.out.println("FAILURE: " + jsonString);
        assertNotNull(jsonString);
        jsonString = instanceFactory.getAllFeeds(FeedStatus.ALL, 1);
        System.out.println("ALL: " + jsonString);
        assertNotNull(jsonString);
    }

    @Test
    public void testGetFeedDataByCorelationid() throws Throwable {
        String corelationId = UUID.randomUUID().toString();
        System.out.println("Corelationid: " + corelationId);

        addHeader("X-EBAY-CORRELATION-ID", corelationId);
        testDefaultDBBacked();

        //Let give some time to Couchbase View to get new data
        InstanceFactory instanceFactory = new InstanceFactory(true);
        String jsonString = null;
        for (int i=0;i<30;i++){
            Thread.sleep(2000);
            jsonString = instanceFactory.getFeedDataByCorelationId(corelationId);
            if(!jsonString.equals("[]")) {
                break;
            }
        }

        System.out.println("Feed Data: " + jsonString);
        assertNotNull(jsonString);

    }


}