package com.ebay.cip.framework.test;


import com.ebay.cip.Cip;
import com.ebay.cip.TestModule;
import com.ebay.cip.framework.configuration.JsonBasedPipelineConfiguration;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean; 
import com.ebay.cip.framework.samples.job.LineSplitterJob;
import com.ebay.cip.framework.samples.job.ResultCompareJob;
import com.ebay.cip.framework.test.job.UnitTestParentJob;
import com.ebay.kernel.util.Base64;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

/**
 * Created by jagmehta
 */
public class PipelineTestUsingMaxFindPipeline extends BasePipelineUnitTest {

    @Override
    public void onPipelineCompleteInternal(UnitTestParentJob parentJob,ICipJob lastExecutedPipelineJob) throws Throwable {
/*
        System.out.println("TotalMsg: "+CommonUnTypedActor.count);
        System.out.println("RepeateMsg: "+ JobExecutionHandler.waitMsgcount);
        CommonUnTypedActor.count = 0;
        JobExecutionHandler.waitMsgcount = 0;
*/
        System.out.println("Ending Test:"+System.currentTimeMillis());
        ResultCompareJob results = (ResultCompareJob)lastExecutedPipelineJob;
        if(results.isPassed()) {
            System.out.println("Passed");
        }else {
            System.err.println(results.getErrorMessage());
            Throwable e = results.getException();
            if(e == null) {
                e = new Exception("Failed:- "+results.getErrorMessage());
            }
            throw e;
        }
    }

    @BeforeClass
    public static void childSetup() throws Exception{
    	System.out.println("\n\n==================================== Starting Test for class PipelineTestUsingMaxFindPipeline====================================");
    	
        /**
         * Use below registration if you want to customize and quickly test new version of changed configuration.
         * You will also need to use registered name in test case.
         * Once you are done, make sure to commit your new pipeline in raptor config.
         */
        String actorPath = "CommonActor";

        String pipelineJson = "{\n" +
                "  \"firstJobName\": \"com.ebay.cip.framework.samples.job.LineSplitterJob\",\n" +
                "  \"defaultDispatcher\": \"com.ebay.cip.framework.dispatcher.AkkaBasedSingleJVMDispatcher\",\n" +
                "  \"jobs\": {\n" +
                "    \"LineSplitterJob\": {\n" +
                "      \"actorPath\": \""+actorPath+"\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.LineSplitterJobProcessor\",\n" +
                "      \"nextJob\": \"com.ebay.cip.framework.samples.job.DirectComputeMaxNumberJob\"\n" +
                "    },\n" +
                "          \"DecodeJob\": {\n" +
                "            \"actorPath\": \""+actorPath+"\",\n" +
                "            \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.DecodeJobProcessor\",\n" +
                "            \"nextJob\": \"com.ebay.cip.framework.samples.job.ComputeMaxNumberJob\"\n" +
                "          },\n" +
                "          \"ComputeMaxNumberJob\": {\n" +
                "            \"actorPath\": \""+actorPath+"\",\n" +
                "            \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ComputeMaxJobProcessor\"\n" +
                "          },\n" +
                "    \"DirectComputeMaxNumberJob\": {\n" +
                "      \"actorPath\": \""+actorPath+"\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.DirectComputeMaxJobProcessor\",\n" +
                "      \"nextJob\": \"com.ebay.cip.framework.samples.job.ResultCompareJob\"\n" +
                "    },\n" +
                "    \"ResultCompareJob\": {\n" +
                "      \"actorPath\": \""+actorPath+"\",\n" +
                "      \"jobProcessorName\": \"com.ebay.cip.framework.samples.jobProcessor.ResultCompareJobProcessor\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        System.out.println("Registering test pipeline:- \n"+pipelineJson);

        PipelineConfiguration pipeline = JsonBasedPipelineConfiguration.getFromJson(pipelineJson);
        Pipelines.registerPipelineConfiguration("test/forkJoinPipelineTestCustomized", pipeline);
    }

    @Before
    /**
     * Make sure that default is non db backed always.
     */
    public void beforeEachTest() {
        FrameworkConfigBean.getBean().setDbBacked(false);
    }

    @Test
    public void startTestUsingDirectPayload() throws Throwable {

        int result = -1;

/*
        String data[] = {
                "1,2,3"
        };
        result = 3;
*/
        String data[] = {
                "1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,200,99,98,1,9"
                ,"1,2,3,300,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,500,99,98,1,9"
                ,"1,2,3,600,99,98,1,9"
                ,"1,2,3,700,99,98,1,9"
                ,"1,2,3,800,99,98,1,9"
                ,"1,2,3,900,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,1200,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,100"
                ,"1,2,3,101"
                ,"1,2222"
                ,"1"
                ,"1,2,3"
        };
        result = 2222;

        StringBuilder builder = new StringBuilder();
        String nl = System.getProperty("line.separator");
        for(String s:data){
            String enc = Base64.encode(s.getBytes());
            builder.append(enc);
            builder.append(nl);
        }

        addHeader(ResultCompareJob.FINAL_RESULT, String.valueOf(result));
        startTest(getFeedRequestData("test/forkJoinPipelineTestCustomized", builder.toString(),"123",null));

    }


    @Test
    public void startTestUsingDirectPayloadDBBacked() throws Throwable {

        FrameworkConfigBean.getBean().setDbBacked(true);
        int result = -1;

/*
        String data[] = {
                "1,2,3"
        };
        result = 3;
*/
        String data[] = {
                "1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,100"
                ,"1,2,3,1200,99,98,1,9"
                ,"1,2,3,400,99,98,1,9"
                ,"1,2,3,100"
                ,"1,2,3,101"
                ,"1,2222"
                ,"1"
                ,"1,2,3"
        };
        result = 2222;

        StringBuilder builder = new StringBuilder();
        String nl = System.getProperty("line.separator");
        for(String s:data){
            String enc = Base64.encode(s.getBytes());
            builder.append(enc);
            builder.append(nl);
        }

        addHeader(ResultCompareJob.FINAL_RESULT, String.valueOf(result));
        startTest(getFeedRequestData("test/forkJoinPipelineTestCustomized", builder.toString(),"123",null));

    }

    @Test
    public void testErrorInput() throws Throwable {

        String result = "failed";
        String data[] = {
                "1,2,3"
                ,"1,2,100,a"
        };

        StringBuilder builder = new StringBuilder();
        String nl = System.getProperty("line.separator");
        for(String s:data){
            String enc = Base64.encode(s.getBytes());
            builder.append(enc);
            builder.append(nl);
        }

        addHeader(ResultCompareJob.FINAL_RESULT, result);
        startTest(getFeedRequestData("test/forkJoinPipelineTestCustomized", builder.toString(),"123",null));

    }

    @Test
    public void startTestUsingGeneratedFile() throws Throwable{
        GeneratedFile file = generatePayloadFile(10,10);
        //create jobcontext
        addHeader(LineSplitterJob.FILE_PATH, file.filePath);
        addHeader(ResultCompareJob.FINAL_RESULT,String.valueOf(file.max));
        System.out.println("Starting Test:" + System.currentTimeMillis());
        startTest("test/forkJoinPipelineTestCustomized", null);
        File fileToDelete = new File(file.filePath);
        fileToDelete.deleteOnExit();

    }

    /**
     * Generate numberOfLines x columns matrix of integer, saves it in a local disk.
     * @param numberOfLines
     * @param columns
     * @return Class containing absolute file path having data and max number in it.
     * @throws Exception
     */
    protected GeneratedFile generatePayloadFile(int numberOfLines, int columns) throws Exception{
/*
        File dir = new File("d:/tmp/cip");
        File tmp = File.createTempFile("fileWithNumbers", ".tmp",dir);
        File tmp1 = File.createTempFile("fileWithNumbers-plain", ".tmp",dir);
*/

        File tmp = File.createTempFile("fileWithNumbers", ".tmp");
        File tmp1 = File.createTempFile("fileWithNumbers-plain", ".tmp");


        Random random = new Random();
        int max = 0;
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp));
        BufferedOutputStream outPlain = new BufferedOutputStream(new FileOutputStream(tmp1));
        StringBuffer buff = new StringBuffer();
        char c = ',';
        byte[] nl = System.getProperty("line.separator").getBytes();
        for(int line=0;line<numberOfLines;line++){
//            buff = new StringBuffer();
            for(int col=0;col<columns;col++){
                int num = random.nextInt(999999999);
                if(num > max) {max = num; }
                buff.append(num).append(c);
            }
            //out.write(buff.toString().getBytes());
            out.write(Base64.encode(buff.toString().getBytes()).getBytes());
            out.write(nl);

            outPlain.write(buff.toString().getBytes());
            outPlain.write(nl);


            buff.setLength(0);
        }
        out.flush();
        out.close();

        outPlain.flush();
        outPlain.close();

        GeneratedFile f = new GeneratedFile();
        f.filePath = tmp.getAbsolutePath();
        f.max = Long.valueOf(max);

        return f;
    }

    class GeneratedFile {
        public long max = 0;
        public String filePath;
    }

}