package com.ebay.cip.framework.test;

import com.ebay.cip.TestModule;
import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.test.job.TestjobTypeEnum;
import com.ebay.cip.framework.test.job.UnitTestParentJob;
import com.ebay.cip.framework.test.job.timeout.TestParentTimeoutJob;
import com.ebay.cip.framework.test.job.timeout.TestTimeoutJob;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;

/**
 * Created by hachong on 9/25/2015.
 */
public class TimeoutTest extends BasePipelineUnitTest {

    static Random random = new Random();
    static int originalTimeout = FrameworkConfigBean.getBean().getTimeoutPeriod();
    
    @BeforeClass
    public static void init(){
    	System.out.println("\n\n==================================== Starting Test for class TimeoutTest====================================");
    }

    @Override
    public void onPipelineCompleteInternal(UnitTestParentJob parentJob, ICipJob lastExecutedPipelineJob) throws Throwable {
        FrameworkConfigBean.getBean().setTimeoutPeriod(originalTimeout);
        System.out.println("Ending Test:"+System.currentTimeMillis());
        Job resultJob;
        if(lastExecutedPipelineJob instanceof TestTimeoutJob)
            resultJob = (TestTimeoutJob)lastExecutedPipelineJob;
        else
            resultJob = (TestParentTimeoutJob)lastExecutedPipelineJob;
        Payload responsePayload = resultJob.getFeedContext().getFeedResponseData().getResponsePayload();
        boolean passed = false;
        if(responsePayload != null) {
            String response = responsePayload.getData();
            if(response != null && response.contains("Passed")) {
                System.err.println("Test pass!");
                passed = true;
            }
        }
        assertTrue("ResponsePayload has errors",passed);
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
        FrameworkJobTypeEnum.get(TestjobTypeEnum.TEST_TIMEOUT_JOB.getName());
        FrameworkConfigBean.getBean().setDbBacked(false);
        FrameworkConfigBean.getBean().setTimeoutPeriod(10);
    }


    @Test
    public void testChildrenTimeout() throws Throwable{
        FrameworkConfigBean.getBean().setTimeoutPeriod(10);
        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.test.job.timeout.TestTimeoutJob\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"TestTimeoutJob\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.timeout.TimeoutParentJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    },\n" +
                "    \"TestTimeoutJob2\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.timeout.TimeoutJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String name = registerPipeline("test/timeout-testChildrenParentTimeout"+random.nextInt(),pipelineJson);
        startTest(name);
    }
    @Test
    public void testParentTimeoutBeforeChildrenTimeout() throws Throwable{
        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.test.job.timeout.TestParentTimeoutJob\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"TestParentTimeoutJob\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.timeout.TimeoutParentJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    },\n" +
                "    \"TestParentTimeoutJob2\": {\n" +
                "      \"actorPath\": \"CommonActor\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.test.jobProcessor.timeout.TimeoutJobProcessor\",\n" +
                "      \"nextJob\": \"\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        String name = registerPipeline("test/timeout-testParentTimeoutBeforeChildrenTimeout"+random.nextInt(),pipelineJson);
        startTest(name);
    }
}
