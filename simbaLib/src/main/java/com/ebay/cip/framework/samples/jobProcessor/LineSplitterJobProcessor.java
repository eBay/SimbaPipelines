package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.OneChildCompleteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.job.ComputeMaxNumberJob;
import com.ebay.cip.framework.samples.job.DecodeJob;
import com.ebay.cip.framework.samples.job.LineSplitterJob;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.soaframework.common.exceptions.ServiceException;

import java.io.*;

/**
 * Created by jagmehta on 5/12/2015.
 */
public class LineSplitterJobProcessor extends BaseJobProcessor {
    /**
     * This method should be called exactly once per object creation.
     *
     * @throws ProcessingException
     * @throws ServiceException
     */
    @Override
    public void Initialize() throws ServiceException, ProcessingException {

    }

    /**
     * This is the core method to execute job. This method may be called multiple
     * times in failed scenario. Logic needs to be idempotent.
     *
     * @param job Job to be executed.
     * @throws ProcessingException
     */
    @Override
    public void execute(Job job) throws ExecuteException {
        LineSplitterJob myJob = (LineSplitterJob)job;
        BufferedReader reader = getPayloadStream(myJob);
        String line = null;
        try {
            DecodeJob decodeJob;
            while( ( line = reader.readLine()) != null){
                CalEventHelper.writeLog("LineSplitterJobProcessor","fork",line,"0");
                decodeJob = new DecodeJob();
                decodeJob.setInputString(line);
                Pipelines.fork(myJob, decodeJob);
            }
//            myJob.setJoinForkedJobs(true);
        }catch(Exception e){
            throw new ExecuteException(e);
        }finally {
            try {
                reader.close();
            } catch (IOException e) {
                //really... Java can do better.
                throw new ExecuteException(e);
            }
        }
        System.out.println("Splitter execute complete: "+System.currentTimeMillis());
    }

    private BufferedReader getPayloadStream (LineSplitterJob myJob){
        Payload payload = myJob.getPayload();
        BufferedReader reader = null;
        if(payload.getData() != null){
            //inline data is passed
            reader = new BufferedReader(new StringReader(payload.getData()));
        }else if (myJob.getFeedContext().getFeedRequestData().getHeadersValue(LineSplitterJob.FILE_PATH) != null){
            //treat this ref id as file resource
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(myJob.getFeedContext().getFeedRequestData().getHeadersValue(LineSplitterJob.FILE_PATH))));
            } catch (FileNotFoundException e) {
                new RuntimeException(e);
            }
        }else {
            reader = new BufferedReader(new StringReader(""));
        }
        return reader;
    }


    @Override
    public void onOneChildComplete(Job childJob, JobContext myJobContext) throws OneChildCompleteException {
        ComputeMaxNumberJob childCompJob = (ComputeMaxNumberJob) childJob;
        //LineSplitterJob myJob = (LineSplitterJob) childCompJob.getParentJob();
        //JobContext childJobContext = childJob.getJobContext();
        //JobContext myJobContext = childJobContext.getParentJobContext();

        String result = "Error";

        if(childJob.getJobStatus() == JobStatusEnum.SUCCESS) {
            result = (String) childJob.getJobContext().get(ComputeMaxNumberJob.RESULT);
            String childResults = (String)myJobContext.get(ComputeMaxNumberJob.CHILD_RESULT);
            childResults = childResults==null?result:childResults+","+result;
            myJobContext.put(ComputeMaxNumberJob.CHILD_RESULT, childResults);
        }else {
            //failure
            //myJob.setFailed(true);
            myJobContext.put("FAILURE",Boolean.TRUE);
        }
       // System.out.println("LineSplitter - onOncechildComplete -"+childJob.getJobId());
    }


    @Override
    public void onAllChildrenComplete(Job parentJob)  {
        LineSplitterJob myJob = (LineSplitterJob)parentJob;
        JobContext myJobContext = parentJob.getJobContext();

        Boolean failed = (Boolean) myJobContext.get("FAILURE");
        if(Boolean.TRUE == failed){
            myJob.failure();
            return;
        }else {
            String childResults = (String)myJobContext.get(ComputeMaxNumberJob.CHILD_RESULT);
            myJob.getJobContext().put(ComputeMaxNumberJob.DATA_TO_PROCESS, childResults);
            //parentJob.setJobStatus(JobStatusEnum.SUCCESS);
            parentJob.success();
        }
        System.out.println("LineSplitter - onAllchildComplete -"+parentJob.getJobId());
    }

    /**
     * This method will be called if job execution gets exception and all retries are
     * completed. This method logic should be idempotent.
     *
     * @param failedJob
     */
/*
    @Override
    public void onFail(ICipJob failedJob) {
        CalEventHelper.writeLog("LineSplitterJob", "onFail", failedJob.getJobId(), "ERROR");
    }
*/

}
