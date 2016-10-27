package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.samples.job.ComputeMaxNumberJob;
import com.ebay.cip.framework.samples.job.DecodeJob;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.soaframework.common.exceptions.ServiceException;
import com.ebay.kernel.util.Base64;

/**
 * Created by jagmehta on 5/12/2015.
 */
public class DecodeJobProcessor extends BaseJobProcessor {
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
        DecodeJob myJob = (DecodeJob)job;
        String decoded = myJob.getInputString();
        if (org.apache.commons.codec.binary.Base64.isBase64(decoded)) {
            decoded = new String(Base64.decode(myJob.getInputString()));
            CalEventHelper.writeLog("DecodeJobProcessor", "execute", decoded, "0");
        }else {
            CalEventHelper.writeLog("DecodeJobProcessor", "execute", "No decoding required", "0");

        }
        myJob.getJobContext().put(ComputeMaxNumberJob.DATA_TO_PROCESS,decoded);
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
