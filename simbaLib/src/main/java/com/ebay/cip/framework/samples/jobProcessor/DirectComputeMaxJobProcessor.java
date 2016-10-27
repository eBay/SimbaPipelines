package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.samples.job.ComputeMaxNumberJob;
import com.ebay.cip.framework.samples.job.DirectComputeMaxNumberJob;
import com.ebay.soaframework.common.exceptions.ServiceException;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jagmehta on 5/11/2015.
 */
public class DirectComputeMaxJobProcessor extends BaseJobProcessor {
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


        DirectComputeMaxNumberJob myJob = (DirectComputeMaxNumberJob)job;
        //first check if we need to read from context
        if(myJob.getNumbers() == null){
            Object obj = job.getJobContext().get(ComputeMaxNumberJob.DATA_TO_PROCESS);
            if(obj instanceof List) {
                myJob.setNumbers((List<String>) obj);
            }else if (obj instanceof String){
                String[] numbers = ((String) obj).split(",");
                myJob.setNumbers(Arrays.asList(numbers));
            }
        }

        if(myJob.getNumbers() != null && myJob.getNumbers().size() > 0) {
            List<Long> numberList = new ArrayList<>(myJob.getNumbers().size());
            for (String s : myJob.getNumbers()) {
                numberList.add(Long.valueOf(s));
            }

            myJob.setMax((Long) Collections.max(numberList));
            myJob.getJobContext().put(ComputeMaxNumberJob.RESULT, myJob.getMax());
        }
        //myJob.setJobStatus(JobStatusEnum.SUCCESS);
        myJob.success();
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
    }
*/

}
