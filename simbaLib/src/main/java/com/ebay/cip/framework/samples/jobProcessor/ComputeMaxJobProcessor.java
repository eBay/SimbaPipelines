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
import com.ebay.cip.framework.samples.job.ComputeMaxNumberJob;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.soaframework.common.exceptions.ServiceException;
import java.util.List;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class ComputeMaxJobProcessor extends BaseJobProcessor {
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

        ComputeMaxNumberJob myJob = (ComputeMaxNumberJob)job;
        //first check if we need to read from context
        if(myJob.getNumbers() == null || myJob.getNumbers().size() == 0){
            Object obj = job.getJobContext().get(ComputeMaxNumberJob.DATA_TO_PROCESS);
            if(obj instanceof String) {
                String rawData = (String)obj;

                for(String s:rawData.split(",")) {
                    myJob.getNumbers().add(s);
                }

//                myJob.setNumbers(Arrays.asList(rawData.split(",")));
            }else if(obj instanceof List) {
                myJob.setNumbers((List<String>) obj);
            }
        }


        List<String> numbers = myJob.getNumbers();
        if(numbers == null) {
            //This should never happen
            throw new ExecuteException("getNumbers returned null");
        }

        //check if we should compute max or break it further.
        if(numbers.size() > 2) {
            //fork it into two
            int mid = numbers.size() / 2;
            //first fork
            ComputeMaxNumberJob nextJob = new ComputeMaxNumberJob();
            nextJob.setNumbers(numbers.subList(0,mid));
            Pipelines.fork(myJob, nextJob);
            CalEventHelper.writeLog("ComputeMaxJobProcessor","fork","List sise="+nextJob.getNumbers().size(),"0");

            //second fork
            nextJob = new ComputeMaxNumberJob();
            nextJob.setNumbers(numbers.subList(mid,numbers.size()));
            Pipelines.fork(myJob, nextJob);
            CalEventHelper.writeLog("ComputeMaxJobProcessor","fork","List sise="+nextJob.getNumbers().size(),"0");
            //job.setJoinForkedJobs(true);

        }else {
            Long max = findMaxFromStringList(myJob.getNumbers());
            myJob.setMax(max);
            myJob.getJobContext().put(ComputeMaxNumberJob.RESULT,String.valueOf(myJob.getMax()));
            myJob.success();
        }
    }

    private Long findMaxFromStringList(String strnumbers) throws ProcessingException {

        if(strnumbers == null){
            return null;
        }

        String[] numbers = strnumbers.split(",");

        if(numbers.length == 2){
            //compute
            return Math.max(Long.valueOf(numbers[0].trim()), Long.valueOf(numbers[1].trim()));
        }else if( numbers.length == 1){
            return Long.valueOf(numbers[0].trim());
        }else {
            //This should never happen
            throw new ProcessingException("getNumbers is zero length");
        }
    }
    private Long findMaxFromStringList(List<String> numbers) throws ExecuteException {

        if(numbers.size() == 2){
            //compute
            return Math.max(Long.valueOf(numbers.get(0).trim()), Long.valueOf(numbers.get(1).trim()));
        }else if( numbers.size() == 1){
            return Long.valueOf(numbers.get(0).trim());
        }else {
            //This should never happen
            throw new ExecuteException("getNumbers is zero length");
        }
    }
/*

    private Long findMaxFromLongList(List<Long> numbers) throws ExecuteException {

        if(numbers.size() == 2){
            //compute
            return Math.max(numbers.get(0), numbers.get(1));
        }else if( numbers.size() == 1){
            return numbers.get(0);
        }else {
            //This should never happen
            throw new ExecuteException("getNumbers is zero length");
        }
    }

*/

    @Override
    /**
     * This method extracts "RESULT' from child's jobcontext.
     * It then appends it to its own jobContext for later use.
     */
    public void onOneChildComplete(Job childJob, JobContext myJobContext) throws OneChildCompleteException {
        String result = "Error";
        JobContext childJobContext = childJob.getJobContext();
//        JobContext myJobContext = childJobContext.getParentJobContext();
        /* //Pring jobcontext hierarchy
        JobContext.printJobContextChain("",childJobContext);
        */
        if(childJob.getJobStatus() == JobStatusEnum.SUCCESS) {
            result = (String) childJobContext.get(ComputeMaxNumberJob.RESULT);
            String childResults = (String)myJobContext.get(ComputeMaxNumberJob.CHILD_RESULT);
            childResults = childResults==null?result:childResults+","+result;
            myJobContext.put(ComputeMaxNumberJob.CHILD_RESULT,childResults);
        }else {
            //failed
            myJobContext.put("FAILURE",Boolean.TRUE);
        }
        CalEventHelper.writeLog("ComputeMaxJobProcessor","onOneChildComplete","Max = "+result,"0");

    }

    @Override
    /**
     * Compute max number from results got from children. Put it back in "RESULT' of my own job context for my
     * parent to use it.
     */
    public void onAllChildrenComplete(Job parentJob)  {
        ComputeMaxNumberJob myJob = (ComputeMaxNumberJob)parentJob;
        JobContext myJobContext = myJob.getJobContext();

        Boolean failed = (Boolean) myJobContext.get("FAILURE");
        if(Boolean.TRUE == failed){
            //myJob.setJobStatus(JobStatusEnum.FAILURE);
            myJob.failure();
            return;
        }

        try {
            String childResults = (String)myJobContext.get(ComputeMaxNumberJob.CHILD_RESULT);
            //Long max = findMaxFromStringList(myJob.getChildResults());
            Long max = findMaxFromStringList(childResults);
            myJob.setMax(max);
            myJobContext.put(ComputeMaxNumberJob.RESULT, String.valueOf(myJob.getMax()));
            //parentJob.setJobStatus(JobStatusEnum.SUCCESS);
            myJob.success();
            CalEventHelper.writeLog("ComputeMaxJobProcessor","onAllChildrenComplete","Max = "+max,"0");

        } catch (ProcessingException e) {
            //parentJob.setJobStatus(JobStatusEnum.FAILURE);
            myJob.failure();
        }

    }

    /**
     * This method will be called if job execution gets exception and all retries are
     * completed. This method logic should be idempotent.
     *
     * @param failedJob
     */



    public void onFail(ICipJob failedJob) {
        CalEventHelper.writeLog("ComputeMaxJobProcessor","onFail",failedJob.getJobId(),"WARNING");

        ComputeMaxNumberJob myJob = (ComputeMaxNumberJob)failedJob;
        myJob.setMax(null);
        //myJob.setJobStatus(JobStatusEnum.FAILURE);
        myJob.failure();
    }


    @Override
    public void onFailExecute(Job failedJob,Throwable e) {onFail(failedJob);}
    @Override
    public void onFailResume(Job failedJob,Throwable e){onFail(failedJob);}
    @Override
    public void onFailAllChildComplete(Job failedJob,Throwable e){onFail(failedJob);}

}
