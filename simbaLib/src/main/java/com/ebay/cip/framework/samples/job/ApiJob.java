package com.ebay.cip.framework.samples.job;

import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.configBean.LMSConfigBean;
import com.ebay.cip.framework.samples.payload.TradingApiPayload;

/**
 * Created by hachong on 3/26/2015.
 */
public class ApiJob extends Job {
    Payload requestPayload = null;
    Payload responsePayload = null;
    short apiResponseStatusCode = -1;

    public ApiJob(Payload requestPayload){
        this.requestPayload = requestPayload;
    }

    @Override
    public JobTypeEnum getJobType() {
        return JobTypeEnum.APIJob;
    }

    public short getApiResponseStatusCode() {
        return apiResponseStatusCode;
    }

    public void setApiResponseStatusCode(short apiResponseStatusCode) {
        this.apiResponseStatusCode = apiResponseStatusCode;
    }


    public Payload getRequestPayload(){return requestPayload;}

    @Override
    public int getMaxRetryCount(){
        return Integer.valueOf(LMSConfigBean.getBean().getMaxRetry());
    }

    public void setResponsePayload(Payload responsePayload){
        this.responsePayload = responsePayload;
    }

    @Override
    public int getTimeoutPeriod() {return LMSConfigBean.getBean().getAPIJobTimeout(); }

    public Payload getResponsePayload(){
        return this.responsePayload;
    }


    public Job clone() throws CloneNotSupportedException {

        TradingApiPayload p = (TradingApiPayload) this.getRequestPayload();
        TradingApiPayload newPayload = new TradingApiPayload(p.getTradingApiHeader(),p.getData(),p.getApiName(),p.getEntityName(),p.getEntitySequence());
        ApiJob newJob = new ApiJob(newPayload);



        return newJob;
    }

}