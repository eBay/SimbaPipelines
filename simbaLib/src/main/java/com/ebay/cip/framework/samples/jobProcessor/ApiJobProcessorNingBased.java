//package com.ebay.cip.framework.samples.jobProcessor;
//
//import com.ebay.cip.framework.JobContext;
//import com.ebay.cip.framework.exception.ExecuteException;
//import com.ebay.cip.framework.exception.ProcessingException;
//import com.ebay.cip.framework.exception.ResumeCompleteProcessingException;
//import com.ebay.cip.framework.job.ICipJob;
//import com.ebay.cip.framework.job.Job;
//import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
//import com.ebay.cip.framework.samples.configBean.LMSConfigBean;
//import com.ebay.cip.framework.samples.job.ApiJob;
//import com.ebay.cip.framework.samples.payload.TradingApiPayload;
//import com.ebay.kernel.calwrapper.CalEventHelper;
//import com.ebay.kernel.logger.LogLevel;
//import com.ebay.kernel.logger.Logger;
//import com.ebay.soaframework.common.exceptions.ServiceException;
//import com.ning.http.client.*;
//
//
///**
// * Created by hachong on 3/26/2015.
// */
//public class ApiJobProcessorNingBased extends BaseJobProcessor {
//
//    public final static ApiJobProcessorNingBased INSTANCE = new ApiJobProcessorNingBased();
//    private AsyncHttpClient asyncHttpClient;
//    private Logger log = Logger.getInstance(ApiJobProcessorNingBased.class);
//
//    //ToDo: Get auth token per caller from CIP_User table (after migration MIP,LMS,FE et al) set in subscription layer
//    //private static String AUTH_SERVICE_TOKEN = "v^1.1#i^1#r^1#I^3#f^0#p^3#t^Ul4yX0ZCMTgyQUIxQTFFQkMyRTJDNDU0MEIwNzJDRTgxM0U2I0VeNTE2";  //This is for QA
//    //private String AUTH_SERVICE_TOKEN = "v^1.1#i^1#I^3#f^0#p^3#r^1#t^Ul4yXzYzREZBQjJGRUI0RTMwOTkyOTcyNkJGODQyMzlERjIzI0VeNzcy"; // This is for L&P
//
//
//    private ApiJobProcessorNingBased() {
//        try {
//            Initialize();
//        } catch (ProcessingException e) {
//            e.printStackTrace();
//        } catch (ServiceException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void Initialize() throws ProcessingException, ServiceException {
//        asyncHttpClient = new AsyncHttpClient();
//    }
//
//    @Override
//    public void execute(Job job) throws ExecuteException {
//        executeInternal((ApiJob) job);
//    }
//
//    protected String getToken(final ApiJob apiJob){
//        String token = null;
//        try {
//            JobContext context = apiJob.getJobContext();
//            token = apiJob.getFeedContext().getFeedRequestData().getHeadersValue("X-EBAY-API-TOKEN");
//        }catch(Exception e){}
//
//        if(token == null){
//            token = LMSConfigBean.getBean().getAuthToken();
//        }
//        return token;
//    }
//
//    public void executeInternal(final ApiJob apiJob) throws ExecuteException {
//        TradingApiPayload requestPayload = (TradingApiPayload) apiJob.getRequestPayload();
//        RequestBuilder builder = new RequestBuilder("POST")
//                .setUrl(LMSConfigBean.getBean().getTradingApiURL())                //For QA Testing
//                        //.setUrl("http://api5.pedc.qa.ebay.com/ws/websvc/eBayAPI")  // For L&P Testing
//                .addHeader("X-EBAY-API-CALL-NAME", requestPayload.getApiName())
//                .addHeader("X-EBAY-API-SITEID", requestPayload.getTradingApiHeader().getSiteId())
//                .addHeader("X-EBAY-API-COMPATIBILITY-LEVEL", requestPayload.getTradingApiHeader().getVersion())
//                .addHeader("X-EBAY-API-IAF-TOKEN",  getToken(apiJob))
//                .setBody(requestPayload.getInputStream());
//
//        final long start = System.currentTimeMillis();
//        System.out.println(" [create] : " + start);
//        log.log(LogLevel.INFO, apiJob.getJobId() + " [create] : " + apiJob.getRequestPayload().getRefId());
//        CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
//        ListenableFuture<Response> response = asyncHttpClient.executeRequest(builder.build(),
//                new AsyncCompletionHandler() {
//
//                    @Override
//                    public Response onCompleted(Response apiResponse) throws Exception {
//                        final long end = System.currentTimeMillis();
//                        log.log(LogLevel.INFO,apiJob.getJobId() + " [end] : " + end);
//                        apiJob.setApiResponseStatusCode((short)apiResponse.getStatusCode());
//                        String tradingApiResponsePayload = apiResponse.getResponseBody().replaceAll("><", ">\n<");
//                        if(! (apiResponse.getStatusCode() > 199 && apiResponse.getStatusCode() < 299 )) {
//                            handleFailedResponse(apiJob, apiResponse);
//                            apiJob.failure();
//                        }else {
//                            apiJob.setResponsePayload(new TradingApiPayload(tradingApiResponsePayload));
//                            apiJob.success();
//                        }
//                        CalEventHelper.writeLog("APICall","JobId",apiJob.getJobId(),"0");
//                        CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
//                        CalEventHelper.writeLog("APICall","Time-ms",""+(end-start),"0");
//                        return apiResponse;
//                    }
//
//                    @Override
//                    public void onThrowable(Throwable t) {
//                        final long end = System.currentTimeMillis();
//                        log.log(LogLevel.INFO,apiJob.getJobId() + " [end] : " + end);
//                        log.log(LogLevel.ERROR,t);
//                        apiJob.getJobContext().put("APIException", t);
//                        CalEventHelper.writeLog("APICall", "Title", apiJob.getRequestPayload().getRefId(), "0");
//                        CalEventHelper.writeException("APIJobProcessor", t, true, "JobId: " + apiJob.getJobId());
//                        CalEventHelper.writeLog("APICall", "Time-ms", "Time taken by API http call itself (ms) " + (end - start), "0");
//                        handleFailedResponse(apiJob, t);
//                        apiJob.failure();
//                    }
//                }
//        );
//
//    }
//
//    @Override
//    public void resumeFromWaiting(Job job) throws ResumeCompleteProcessingException {
//        if (job.isComplete()) {
//            log.log(LogLevel.INFO,"APIJob complete");
//            return;
//        }
//
//    }
//
//
//    @Override
//    public void onFailExecute(Job failedJob,Throwable e) {
//        handleFailedResponse(failedJob, e);
//    }
//
//    protected void handleFailedResponse(final ICipJob failedJob, Throwable e) {
//        TradingApiPayload failurePayload = new TradingApiPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                "<AddFixedPriceItemResponse>\n" +
//                "   <Ack>Failure</Ack>\n" +
//                "   <Error>Error Response</Error>\n" +
//                "   <ShortMessage>API call resulted error</ShortMessage>\n" +
//                "   <LongMessage>This is cause by fail api job</LongMessage>\n" +
//                "</AddFixedPriceItemResponse>");
//        ((ApiJob) failedJob).setResponsePayload(failurePayload);
//
//    }
//
//    protected void handleFailedResponse(final ICipJob failedJob, Response apiResponse) {
//        try {
//            TradingApiPayload failurePayload = new TradingApiPayload("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                    "<AddFixedPriceItemResponse>\n" +
//                    "   <Ack>Failure</Ack>\n" +
//                    "   <Error>" + apiResponse.getStatusText() + "</Error>\n" +
//                    "   <ShortMessage>API call resulted error</ShortMessage>\n" +
//                    "   <LongMessage><![CDATA[" + apiResponse.getResponseBody() + "]]></LongMessage>\n" +
//                    "</AddFixedPriceItemResponse>");
//            ((ApiJob) failedJob).setResponsePayload(failurePayload);
//        }catch(Exception e){
//            handleFailedResponse(failedJob, e);
//        }
//
//    }
//}