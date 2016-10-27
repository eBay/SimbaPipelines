package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.aero.kernel.ahc.AeroAsyncHttpClient;
import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.FastFailException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.exception.ResumeCompleteProcessingException;
import com.ebay.cip.framework.exception.SimbaCircuitBreakerOpenException;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.samples.configBean.LMSConfigBean;
import com.ebay.cip.framework.samples.job.ApiJob;
import com.ebay.cip.framework.util.AsyncCompletionHandlerWithCircuitBreaker;
import com.ebay.cip.framework.samples.payload.TradingApiPayload;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.ebay.soaframework.common.exceptions.ServiceException;

import org.asynchttpclient.*;

import java.io.IOException;


/**
 * Created by hachong on 3/26/2015.
 */
public class ApiJobProcessor extends BaseJobProcessor {

    public final static ApiJobProcessor INSTANCE = new ApiJobProcessor();
    private Logger log = Logger.getInstance(ApiJobProcessor.class);
    private ListenableFuture<Response> response;

    //ToDo: Get auth token per caller from CIP_User table (after migration MIP,LMS,FE et al) set in subscription layer
    //private static String AUTH_SERVICE_TOKEN = "v^1.1#i^1#r^1#I^3#f^0#p^3#t^Ul4yX0ZCMTgyQUIxQTFFQkMyRTJDNDU0MEIwNzJDRTgxM0U2I0VeNTE2";  //This is for QA
    //private String AUTH_SERVICE_TOKEN = "v^1.1#i^1#I^3#f^0#p^3#r^1#t^Ul4yXzYzREZBQjJGRUI0RTMwOTkyOTcyNkJGODQyMzlERjIzI0VeNzcy"; // This is for L&P


    private ApiJobProcessor() {
        try {
            Initialize();
        } catch (ProcessingException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void Initialize() throws ProcessingException, ServiceException {

        /** In real production application, client should be created for each endpoints and should be registered as below with
         *  appropriate name. Each such registration will appear on ValidateInternal jmx section and configuration can be changed.
         *  Client should not be create for each request.
         *  Same client instance should not be used for multiple endpoints.
         *  When you need to make api call, you can get client by AsyncHttpClientRegistryImpl.getInstance().get(registeredName)
         */
        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setMaxRequestRetry(3)
                .setAllowPoolingConnection(true)
                .setConnectionTimeoutInMs(200).setRequestTimeoutInMs(60000)
                .setIdleConnectionInPoolTimeoutInMs(3000).build();
        AeroAsyncHttpClient client = (AeroAsyncHttpClient) AsyncHttpClientFactory.getAsyncHttpClient(config);
        AsyncHttpClientRegistryImpl.getInstance().registerIfNew("TradingAPI", client);
        /**********************************************************************************************************/
    }

    @Override
    public void execute(Job job) throws ExecuteException {
        executeInternal((ApiJob) job);
    }

    protected String getToken(final ApiJob apiJob){
        String token = null;
        try {
            JobContext context = apiJob.getJobContext();
            token = apiJob.getFeedContext().getFeedRequestData().getHeadersValue("X-EBAY-API-TOKEN");
        }catch(Exception e){}

        if(token == null){
            token = LMSConfigBean.getBean().getAuthToken();
        }
        return token;
    }

    public void executeInternal(final ApiJob apiJob) throws ExecuteException {
        TradingApiPayload requestPayload = (TradingApiPayload) apiJob.getRequestPayload();
        RequestBuilder builder = new RequestBuilder("POST")
                .setUrl(LMSConfigBean.getBean().getTradingApiURL())                //For QA Testing
                        //.setUrl("http://api5.pedc.qa.ebay.com/ws/websvc/eBayAPI")  // For L&P Testing
                .addHeader("X-EBAY-API-CALL-NAME", requestPayload.getApiName())
                .addHeader("X-EBAY-API-SITEID", requestPayload.getTradingApiHeader().getSiteId())
                .addHeader("X-EBAY-API-COMPATIBILITY-LEVEL", requestPayload.getTradingApiHeader().getVersion())
                .addHeader("X-EBAY-API-IAF-TOKEN",  getToken(apiJob))
                .setBody(requestPayload.getInputStream());

        final long start = System.currentTimeMillis();
        System.out.println(" [create] : " + start);
        log.log(LogLevel.INFO, apiJob.getJobId() + " [create] : " + apiJob.getRequestPayload().getRefId());
        CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
        try {
            response = AsyncHttpClientRegistryImpl.getInstance().get("TradingAPI").executeRequest(builder.build(),
                    new AsyncCompletionHandlerWithCircuitBreaker(apiJob.getFeedContext().getPipelineKey(), apiJob.getJobType()) {

                        @Override
                        public Response onCompletedWithCB(Response apiResponse) throws Exception {
                            final long end = System.currentTimeMillis();
                            log.log(LogLevel.INFO, apiJob.getJobId() + " [end] : " + end);
                            apiJob.setApiResponseStatusCode((short) apiResponse.getStatusCode());
                            String tradingApiResponsePayload = apiResponse.getResponseBody().replaceAll("><", ">\n<");
                            if (!(apiResponse.getStatusCode() > 199 && apiResponse.getStatusCode() < 299)) {
                                handleFailedResponse(apiJob, apiResponse);
                                apiJob.failure();
                                throw new Exception();
                            } else {
                                apiJob.setResponsePayload(new TradingApiPayload(tradingApiResponsePayload));
                                apiJob.success();
                            }
                            CalEventHelper.writeLog("APICall", "JobId", apiJob.getJobId(), "0");
                            CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
                            CalEventHelper.writeLog("APICall", "Time-ms", "" + (end - start), "0");
                            return apiResponse;
                        }

                        @Override
                        public void onThrowableWithCB(Throwable t) throws Exception {
                            final long end = System.currentTimeMillis();
                            log.log(LogLevel.INFO, apiJob.getJobId() + " [end] : " + end);
                            log.log(LogLevel.ERROR, t);
                            apiJob.getJobContext().put("APIException", t);
                            CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
                            CalEventHelper.writeException("APIJobProcessor", t, true, "JobId: " + apiJob.getJobId());
                            CalEventHelper.writeLog("APICall", "Time-ms", "Time taken by API http call itself (ms) " + (end - start), "0");
                            handleFailedResponse(apiJob, t);
                            apiJob.failure();
                            throw new Exception();
                        }
                    }
            );


        } catch (IOException e) {
            throw new ExecuteException(e);
        }

    }

    @Override
    public void resumeFromWaiting(Job job) throws ResumeCompleteProcessingException {
        if (job.isComplete()) {
            log.log(LogLevel.INFO,"APIJob complete");
        }
        if (response != null && response.isDone()){
            log.log(LogLevel.DEBUG, "Response done");
        }

    }


    @Override
    public void onFailExecute(Job failedJob,Throwable e) {
        handleFailedResponse(failedJob, e);
    }

    protected void handleFailedResponse(final ICipJob failedJob, Throwable e) {
    	String CB = "";
    	if(e instanceof SimbaCircuitBreakerOpenException) {
    		CB = "   <FastFail>true</FastFail>\n";
    	}
        TradingApiPayload failurePayload = new TradingApiPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<AddFixedPriceItemResponse>\n" +
                "   <Ack>Failure</Ack>\n" +
                CB +
                "   <Error>Error Response</Error>\n" +
                "   <ShortMessage>API call resulted error</ShortMessage>\n" +
                "   <LongMessage>This is cause by fail api job</LongMessage>\n" +
                "</AddFixedPriceItemResponse>");
        ((ApiJob) failedJob).setResponsePayload(failurePayload);

    }

    protected void handleFailedResponse(final ICipJob failedJob, Response apiResponse) {
        try {
            TradingApiPayload failurePayload = new TradingApiPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<AddFixedPriceItemResponse>\n" +
                    "   <Ack>Failure</Ack>\n" +
                    "   <Error>" + apiResponse.getStatusText() + "</Error>\n" +
                    "   <ShortMessage>API call resulted error</ShortMessage>\n" +
                    "   <LongMessage><![CDATA[" + apiResponse.getResponseBody() + "]]></LongMessage>\n" +
                    "</AddFixedPriceItemResponse>");
            ((ApiJob) failedJob).setResponsePayload(failurePayload);
        }catch(Exception e){
            handleFailedResponse(failedJob, e);
        }

    }
}