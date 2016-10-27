//package com.ebay.cip.framework.test;
//
//
//import com.ebay.cip.framework.JobContext;
//import com.ebay.cip.framework.NonDBContext;
//import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
//import com.ebay.cip.framework.configuration.PipelineConfiguration;
//import com.ebay.cip.framework.configuration.Pipelines;
//import com.ebay.cip.framework.job.ICipJob;
//import com.ebay.cip.framework.samples.job.LineSplitterJob;
//import com.ebay.cip.framework.samples.job.ResultCompareJob;
//import com.ebay.cip.framework.test.job.UnitTestParentJob;
//import com.ebay.kernel.util.Base64;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.util.Random;
//
///**
// * Created by jagmehta
// */
//public class QueuePipelineTest extends BasePipelineUnitTest {
//
//    @Override
//    public void onPipelineCompleteInternal(UnitTestParentJob parentJob,ICipJob lastExecutedPipelineJob) throws Throwable {
///*
//        System.out.println("TotalMsg: "+CommonUnTypedActor.count);
//        System.out.println("RepeateMsg: "+ JobExecutionHandler.waitMsgcount);
//        CommonUnTypedActor.count = 0;
//        JobExecutionHandler.waitMsgcount = 0;
//*/
//        System.out.println("Ending Test:"+System.currentTimeMillis());
//        ResultCompareJob results = (ResultCompareJob)lastExecutedPipelineJob;
//        if(results.isPassed()) {
//            System.out.println("Passed");
//        }else {
//            System.err.println(results.getErrorMessage());
//            Throwable e = results.getExceptionCALLink();
//            if(e == null) {
//                e = new Exception("Failed:- "+results.getErrorMessage());
//            }
//            throw e;
//        }
//    }
//
//   @BeforeClass
//    public static void childSetup() throws Exception{
//        String actorPath = "ItemTradingAPIActor";
//
//        String pipelineJson = "{\n" +
//                "  \"firstJobName\": \"com.ebay.cip.framework.samples.job.LineSplitterJob\",\n" +
//                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
//                "  \"jobs\": {\n" +
//                "    \"LineSplitterJob\": {\n" +
//                "      \"actorPath\": \""+actorPath+"\",\n" +
//                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.LineSplitterJobProcessor\",\n" +
//                "      \"nextJob\": \"com.ebay.cip.framework.samples.job.DirectComputeMaxNumberJob\"\n" +
//                "    },\n" +
//                "          \"DecodeJob\": {\n" +
//                "            \"actorPath\": \""+actorPath+"\",\n" +
//                "            \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.DecodeJobProcessor\",\n" +
//                "            \"nextJob\": \"com.ebay.cip.framework.samples.job.ComputeMaxNumberJob\"\n" +
//                "          },\n" +
//                "          \"ComputeMaxNumberJob\": {\n" +
//                "            \"actorPath\": \""+actorPath+"\",\n" +
//                "            \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ComputeMaxJobProcessor\"\n" +
//                "          },\n" +
//                "    \"DirectComputeMaxNumberJob\": {\n" +
//                "      \"actorPath\": \""+actorPath+"\",\n" +
//                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.DirectComputeMaxJobProcessor\",\n" +
//                "      \"nextJob\": \"com.ebay.cip.framework.samples.job.ResultCompareJob\"\n" +
//                "    },\n" +
//                "    \"ResultCompareJob\": {\n" +
//                "      \"actorPath\": \""+actorPath+"\",\n" +
//                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ResultCompareJobProcessor\"\n" +
//                "    }\n" +
//                "  }\n" +
//                "}";
//
//        System.out.println("Registering test pipeline:- \n"+pipelineJson);
//
//        PipelineConfiguration pipeline = JsonBasedPipelineConfiguration.getFromJson(pipelineJson);
//        Pipelines.registerPipelineConfiguration("test/queuePipelineTest", pipeline);
//    }
//
//    @Test
//    public void startTestUsingDirectPayloadSingleThread() throws Throwable {
//        String data[] = {
//                "1,2,3,100"
//                ,"1,2,3,100,99,10,1,9"
//                ,"99,99,99,99,100,0"
//                ,"200,300,900,1,9"
//
//        };
//
//
//        StringBuilder builder = new StringBuilder();
//        String nl = System.getProperty("line.separator");
//        for(String s:data){
//            String enc = Base64.encode(s.getBytes());
//            builder.append(enc);
//            builder.append(nl);
//        }
//
//        addHeader(ResultCompareJob.FINAL_RESULT, String.valueOf(900));
//        startTest("test/queuePipelineTest", builder.toString());
//
//    }
//
//}