package com.ebay.cip.framework.samples.jobProcessor;

import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.JobContext;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.enumeration.JobStatusEnum;
import com.ebay.cip.framework.exception.AllChildrenCompleteException;
import com.ebay.cip.framework.exception.ExecuteException;
import com.ebay.cip.framework.exception.OneChildCompleteException;
import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.jobProcessor.BaseJobProcessor;
import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.framework.samples.job.ApiJob;
import com.ebay.cip.framework.samples.payload.TradingApiHeader;
import com.ebay.cip.framework.samples.payload.TradingApiPayload;
import com.ebay.kernel.calwrapper.CalEventHelper;
import com.ebay.kernel.context.AppBuildConfig;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import com.ebay.soaframework.common.exceptions.ServiceException;

import javax.xml.stream.*;
import javax.xml.stream.events.EndDocument;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

/**
 * Created by hachong on 3/26/2015.
 */
public class SplitterJobProcessor extends BaseJobProcessor {
    public final static SplitterJobProcessor INSTANCE = new SplitterJobProcessor();
    public final static String HEADER_TEST_ITERATION = "X-EBAY-TEST-ITERATION";
    final static Logger log = Logger.getInstance(SplitterJobProcessor.class);
    ThreadLocal<String> title = new ThreadLocal<>();


    @Override
    public void Initialize() throws ProcessingException, ServiceException {

    }

    @Override
    public void execute(Job job) throws ExecuteException {
        job.getJobMetaData().setJobStatus(JobStatusEnum.IN_PROGRESS);
        try {
            job.getJobContext().put("ApiStats", new ApiJobStats());
            ApiJob lastJob = split((job).getPayload().getInputStream(),job);
            String testIterations = job.getFeedContext().getFeedRequestData().getHeadersValue(HEADER_TEST_ITERATION);
            if(testIterations != null){
                forkMore(job, lastJob, testIterations);
            }
        } catch (XMLStreamException e) {
            throw new ExecuteException(e);
        }
        catch (IOException e) {
            throw new ExecuteException(e);
        }
    }

    @Override
    public void onFailExecute(Job failedJob,Throwable e) {

        log.log(LogLevel.INFO, "onFail Execute:" + e);
    }


    @Override
    public void onOneChildComplete(Job childJob, JobContext myContext) throws OneChildCompleteException {
        ApiJob apiJob = (ApiJob) childJob;
        FeedContext feedContext = apiJob.getFeedContext();
        int iteration = getTestIteration(childJob);
        if(iteration < 101) {
            Payload responsePayload = feedContext.getFeedResponseData().getResponsePayload();

            if (responsePayload == null) {
                feedContext.getFeedResponseData().setResponsePayload(apiJob.getResponsePayload());
            } else {
                String r = apiJob.getResponsePayload().getData();
                String responseData = responsePayload.getData() + r;
                responsePayload.setData(responseData);
            }
        }
        if(iteration >0 ) {
            ApiJobStats stats = (ApiJobStats) myContext.get("ApiStats");
            if (childJob.getJobStatus() == JobStatusEnum.SUCCESS && apiJob.getResponsePayload().getData().contains("<Item")) {
                stats.incrSuccess();
            } else {
                stats.incrFailed();
            }
            stats.addStat(((ApiJob) childJob).getApiResponseStatusCode());
            myContext.put("ApiStats", stats);
        }

    }

    @Override
    public void timeout(Job job){
        log.log(LogLevel.INFO, "Splitter job TIMEOUT HAPPEN! Check!");
        String timeoutResponse = "<Ack>timeout<Ack>";
        FeedContext feedContext = job.getFeedContext();
        Payload responsePayload = feedContext.getFeedResponseData().getResponsePayload();

        if(responsePayload == null)
            feedContext.getFeedResponseData().setResponsePayload(new Payload(timeoutResponse));
        else {
            String responseData = responsePayload.getData() + timeoutResponse;
            responsePayload.setData(responseData);
        }
    }

    @Override
    public void onAllChildrenComplete(Job parentJob) throws AllChildrenCompleteException {
        log.log(LogLevel.INFO, "all children complete for splitter job");
        parentJob.success();
        //print it in cal
        if(getTestIteration(parentJob) > 0) {
            ((ApiJobStats)parentJob.getJobContext().get("ApiStats")).logIt();
        }
    }
    private ApiJob split(InputStream inputStream, Job currentJob) throws XMLStreamException, IOException {
        ApiJob lastJob = null;
        try{
            XMLInputFactory xif = XMLInputFactory.newInstance();
            XMLEventReader xer = xif.createXMLEventReader(inputStream);
            TradingApiHeader header = null;
            long sequence = 0;
            while(xer.hasNext())
            {
                XMLEvent xmlEvent = xer.nextEvent();
                //If current xmlEvent is the end document, release feed lock and send to aggregator actor with list of child job set.
                if(xmlEvent.isEndDocument()){

                }
                //If current xmlEvent is not the create element, e.g.<AddFixedPriceItemRequest> or <Header>, we continue to loop into next element of this XML.
                if(!xmlEvent.isStartElement()) {
                    continue;
                }


                StartElement breakStartElement = xmlEvent.asStartElement();
                String localName = breakStartElement.getName().getLocalPart();

                //Process contents between <Header> and </Header>, like Version and SiteID.
                if(localName.equalsIgnoreCase("Header"))
                {
                    header = processHeader(xer,breakStartElement);
                }
                else if(localName.trim() == "AddFixedPriceItemRequest") //If current create element is trading api request name, we parse the trading api request.
                {
                    sequence++;
                    if(header == null) {
                        header = new TradingApiHeader("973","0");
                    }

                    //Construct the trading api payload and job by looping through the xml request.
                    StringBuffer itemId = new StringBuffer();
                    TradingApiPayload tradingApiPayload = new TradingApiPayload(header,createXMLRequest(xer,breakStartElement,itemId).toString(),breakStartElement.getName().getLocalPart().replace("Request", ""),itemId.toString(),sequence);
                    tradingApiPayload.setRefId(this.title.get());
                    ApiJob apiJob = new ApiJob(tradingApiPayload);
                    Pipelines.fork(currentJob, apiJob);
                    lastJob = apiJob;
                    this.title.set("");
                }
            }
        }
        finally
        {
            inputStream.close();
        }
        return lastJob;
    }
    /**
     * This function parses TradingApi header payload
     * @param xer
     * @param breakStartElement
     * @return TradingApiHeader object
     * @throws XMLStreamException
     */
    private TradingApiHeader processHeader(XMLEventReader xer, StartElement breakStartElement) throws XMLStreamException {
        boolean versionFlag = false;
        boolean siteIdFlag = false;
        TradingApiHeader header = new TradingApiHeader();
        XMLEvent xmlEvent = xer.nextEvent();
        while(!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().equals(breakStartElement.getName())))
        {
            if(xmlEvent.isStartElement())
            {
                if(xmlEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("Version"))
                {
                    versionFlag = true;
                }
                else if(xmlEvent.asStartElement().getName().getLocalPart().equalsIgnoreCase("SiteID"))
                    siteIdFlag = true;
            }
            else if(xmlEvent.isCharacters())
            {
                if(versionFlag)
                {
                    String version = xmlEvent.asCharacters().getData();
                    header.setVersion(version);
                    versionFlag = false;
                }
                else if(siteIdFlag)
                {
                    String siteId = xmlEvent.asCharacters().getData();
                    header.setSiteId(siteId);
                    siteIdFlag = false;
                }
            }
            xmlEvent = xer.nextEvent();
        }
        return header;
    }

    /**
     * This function parses TradingApi header payload
     * @param xer
     * @param breakStartElement
     * @param itemId
     * @return StringBuffer of API request body
     * @throws XMLStreamException
     */
    private StringBuffer createXMLRequest(XMLEventReader xer,StartElement breakStartElement,StringBuffer itemId) throws XMLStreamException {
        XMLEventFactory xef = XMLEventFactory.newFactory();
        StartDocument startDocument = xef.createStartDocument("UTF-8", "1.0");
        EndDocument endDocument = xef.createEndDocument();
        XMLOutputFactory xof = XMLOutputFactory.newFactory();

        List<XMLEvent> cachedXMLEvents = processCachedXMLEvents(xer,breakStartElement,itemId);
        StringWriter stringWriter = new StringWriter();
        XMLEventWriter xew = xof.createXMLEventWriter(stringWriter);

        xew.add(startDocument);

        for(XMLEvent cachedEvent : cachedXMLEvents) {
            xew.add(cachedEvent);
        }

        xew.add(endDocument);

        xew.flush();
        stringWriter.flush();
        StringBuffer xmlRequest = stringWriter.getBuffer();
        return xmlRequest;
    }

    private List<XMLEvent> processCachedXMLEvents(XMLEventReader xer, StartElement breakStartElement,StringBuffer itemId) throws XMLStreamException {
        List<XMLEvent> cachedXMLEvents = new ArrayList<XMLEvent>();
        cachedXMLEvents.add(breakStartElement);
        XMLEvent xmlEvent = xer.nextEvent();

        boolean isItemIdElement = false;
        boolean title = false;
        while(!(xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().equals(breakStartElement.getName()))) {
            if(xmlEvent.isStartElement()) {
                String startElement = xmlEvent.asStartElement().getName().toString();
                if(startElement.contains("ItemID")||startElement.contains("ItemId")){
                    isItemIdElement = true;
                }else if(startElement.toLowerCase().contains("title")) {
                    title = true;
                }
            }
            else if(xmlEvent.isCharacters() && isItemIdElement){
                itemId.append(xmlEvent.asCharacters().getData());
                isItemIdElement = false;
            }
            else if (xmlEvent.isCharacters() && title){
                this.title.set(xmlEvent.asCharacters().getData());
                title = false;
            }
            cachedXMLEvents.add(xmlEvent);
            xmlEvent = xer.nextEvent();
        }
        cachedXMLEvents.add(xmlEvent);
        return cachedXMLEvents;
    }

    protected void forkMore(Job currentJob, ApiJob jobToClone, String iterations) {

        int number = getTestIteration(currentJob);

        try {
            String top = null;
            String bottom = null;
            String title = null;
            {
                String data = jobToClone.getRequestPayload().getData();
                String dataLower = data.toLowerCase();
                int start = dataLower.indexOf("<title>") + "<title>".length();
                int end = dataLower.indexOf("</title>", start);
                dataLower = null;
                String text = data.substring(start,end);
                //end = text.length();
                dataLower = text.toLowerCase();
                int i = dataLower.indexOf("cdata");
                if( i>=0) {
                    start += dataLower.indexOf("[",i)+1;
                    end = data.indexOf("]]",start);
                }

                top = data.substring(0,start);
                bottom = data.substring(end);
                title = "-"+data.substring(start,end);
            }

            Random random = new Random();
            StringBuilder builder = null;
            while (number > 0) {
                ApiJob newJob = (ApiJob)jobToClone.clone();
                builder = new StringBuilder(String.valueOf(System.currentTimeMillis())).append(random.nextInt());
                builder.append(title);
                int titleLength = builder.length();
                titleLength = titleLength>80?80:titleLength-1;
                String data = top + builder.substring(0,titleLength) +bottom;
                newJob.getRequestPayload().setData(data);
                newJob.getRequestPayload().setRefId(builder.substring(0,titleLength));
                Pipelines.fork(currentJob, newJob);
                number--;
                builder.setLength(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTestIteration(Job job) {
        int number = 0;
        AppBuildConfig config = AppBuildConfig.getInstance();
        if (config.isProduction() || config.isSandbox() || config.isPreProd()) {
            //not allowed in above non testing env.
        } else {
            String iterations = null;
            try {
                iterations = job.getFeedContext().getFeedRequestData().getHeadersValue(HEADER_TEST_ITERATION);
                if(iterations != null) {
                    number = Integer.parseInt(iterations.trim());
                }
            } catch (Exception e) {
                log.log(LogLevel.WARN, "Invalid header value passed for "+HEADER_TEST_ITERATION+" as "+iterations);
            }
        }
        return number;
    }


    class ApiJobStats {
        int failed = 0, success = 0;
        Map<Integer,Integer> stats = new HashMap<>();

        public ApiJobStats(){
            for(int i=200;i<600;i++){
                stats.put(i,0);
            }
            stats.put(-1,0);
        }

        public void addStat(int statusCode){
            stats.put(statusCode, stats.get(statusCode)+1);
        }

        public Map<Integer,Integer> getStats(){
            return stats;
        }

        public void incrFailed(){
            failed++;
        }
        public void incrSuccess() {
            success++;
        }
        public int getFailed() {
            return failed;
        }
        public int getSuccess() {
            return success;
        }

        void logIt() {
            CalEventHelper.writeLog("APICall", "Success", "" + success, "0");
            log.log(LogLevel.INFO, "Success: " + success);
            CalEventHelper.writeLog("APICall", "Failure", "" + failed, "0");
            log.log(LogLevel.INFO,"Failure: "+failed);
            for(Map.Entry<Integer,Integer> me: stats.entrySet()){
                int v = me.getValue();
                if(v!=0) {
                    CalEventHelper.writeLog("APICall", "StatusCode", me.getKey()+":"+v, "0");
                    log.log(LogLevel.INFO,"StatusCode: "+me.getKey()+":"+v);
                }
            }
        }

    }

}