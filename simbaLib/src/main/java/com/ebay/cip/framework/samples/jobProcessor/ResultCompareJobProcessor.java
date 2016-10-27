package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.job.ComputeMaxNumberJob;
import com.ebay.cip.framework.samples.job.ResultCompareJob;
import com.ebay.soaframework.common.exceptions.ServiceException;

/**
 * Created by jagmehta on 5/12/2015.
 */
public class ResultCompareJobProcessor extends BaseJobProcessor {
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

        ResultCompareJob myJob = (ResultCompareJob) job;
        String shouldBeResult = job.getFeedContext().getFeedRequestData().getHeadersValue(ResultCompareJob.FINAL_RESULT);
        String valueComputed = String.valueOf(job.getJobContext().get(ComputeMaxNumberJob.RESULT));
        valueComputed = (valueComputed==null || "null".equals(valueComputed))?"failed":valueComputed;
        String response = valueComputed;

        if (shouldBeResult != null) {
            if (shouldBeResult.equalsIgnoreCase(valueComputed)) {
                System.out.println("SUCCESS - result is->" + valueComputed);
            } else {
                String msg = "ERROR - result is->" + valueComputed + "... Should be->" + shouldBeResult;
                System.err.println(msg);
            }
        }
        Payload payload = new Payload(response);
        job.getFeedContext().getFeedResponseData().setResponsePayload(payload);
        //job.setJobStatus(JobStatusEnum.SUCCESS);
        job.success();
    }

}
