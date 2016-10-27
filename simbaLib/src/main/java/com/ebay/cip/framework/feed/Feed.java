package com.ebay.cip.framework.feed;

import akka.actor.ActorRef;
import com.ebay.cip.akka.actors.PostJobExecutionActor;
import com.ebay.cip.framework.BaseContext;
import com.ebay.cip.framework.FeedContext;
import com.ebay.cip.framework.configuration.CipActorSystem;
import com.ebay.cip.framework.configuration.PipelineConfiguration;
import com.ebay.cip.framework.configuration.Pipelines;
import com.ebay.cip.framework.dispatcher.Dispatcher;
import com.ebay.cip.framework.dispatcher.DispatcherFactory;
import com.ebay.cip.framework.enumeration.JobTypeEnum;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.cip.service.FeedRequestData;
import com.ebay.cip.service.FeedResponseData;
import com.ebay.es.cbdataaccess.bean.CouchBaseDocument;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hachong on 7/24/2015.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feed implements CouchBaseDocument {
    private String feedId;
    private FeedStatus feedStatus;
    private String startTime;
    private String endTime;
    private String machineName;
    private String firstJobId;
    private String note;
    private List<ErrorLog> errorLogList;
    private FeedRequestData feedRequestData;
    private FeedResponseData feedResponseData;
    private boolean dbBacked;
    private static final String TYPE = "FEED";
    @JsonIgnore
    private transient FeedContext feedContext;



    @JsonIgnore
    private ConcurrentHashMap<JobTypeEnum,ActorRef> postExecutionActorMap;

    @JsonIgnore
    private InstanceFactory instanceFactory;

    @JsonIgnore
    private Dispatcher dispatcher;


    //TODO: should change the name to feed Configuration
    @JsonIgnore
    private PipelineConfiguration pipelineConfiguration;

    public Feed(){}

    public Feed(String feedId,FeedRequestData feedRequestData){
        this.feedId = feedId;
        this.feedRequestData = feedRequestData;
        this.feedResponseData = new FeedResponseData();
        this.feedStatus = FeedStatus.CREATED;
        if(feedRequestData.getPipelineKey() != null) { // this check is enable job to be UnitTested without invoking the whole feed
            this.pipelineConfiguration = Pipelines.getPipelineConfiguration(feedRequestData.getPipelineKey());
            this.dispatcher = DispatcherFactory.getInstance().getdispatcherByName(pipelineConfiguration.getDefaultDispatcher(), pipelineConfiguration.getDefaultDispatcher());
        }
        this.dbBacked = FrameworkConfigBean.getBean().isDbBacked();
        this.instanceFactory = new InstanceFactory(dbBacked);
        this.postExecutionActorMap = new ConcurrentHashMap<>();
    }


    public void setPipelineConfiguration(PipelineConfiguration configuration){
        this.pipelineConfiguration = configuration;
    }
    public PipelineConfiguration getPipelineConfiguration() {
        return pipelineConfiguration;
    }

    public FeedRequestData getFeedRequestData(){
        return feedRequestData;
    }

    public FeedResponseData getFeedResponseData(){return feedResponseData;}

    public String getFeedId(){
        return this.feedId;
    }

    public void setFeedStatus(FeedStatus feedStatus){
        this.feedStatus = feedStatus;
    }
    public FeedStatus getFeedStatus(){
        return this.feedStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public ActorRef getPostJobExecutionActor(JobTypeEnum jobType) {
        ActorRef postJobExecutionActor = postExecutionActorMap.get(jobType);
        if (postJobExecutionActor == null) {
            synchronized (Feed.class) {
                postJobExecutionActor = postExecutionActorMap.get(jobType);
                if (postJobExecutionActor == null) {
                    postJobExecutionActor = addPostJobExecutionActor(jobType);
                }
            }
        }
        return postJobExecutionActor;
    }

    private ActorRef addPostJobExecutionActor(JobTypeEnum jobType){
        ActorRef postJobExecutionActor =  CipActorSystem.getInstance().getParentActorInstance().createActor(PostJobExecutionActor.class, jobType.getName(), feedId);
        this.postExecutionActorMap.put(jobType,postJobExecutionActor);
        return postJobExecutionActor;
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }


    public boolean isDbBacked() {
        return dbBacked;
    }

    public void setIsDbBacked(boolean isDbBacked) {
        this.dbBacked = isDbBacked;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFirstJobId() {
        return firstJobId;
    }

    public void setFirstJobId(String firstJobId) {
        this.firstJobId = firstJobId;
    }


    @Override
    public void setType(String s) {
            //ignore
    }

    public String getType(){
        return TYPE;
    }

    public ConcurrentHashMap<JobTypeEnum, ActorRef> getPostExecutionActorMap() {
        return postExecutionActorMap;
    }


    public void setInstanceFactory(InstanceFactory is) { this.instanceFactory = is; }
    public InstanceFactory getInstanceFactory() {
        return instanceFactory;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public List<ErrorLog> getErrorLogList() {
        return errorLogList;
    }

    public void addErrorLog(String jobId,String calLink) {
        ErrorLog errorLog = new ErrorLog(jobId,calLink);
        if(errorLogList == null){
            errorLogList = new ArrayList<ErrorLog>();
        }
        errorLogList.add(errorLog);
    }

    public FeedContext getFeedContext() {
        if(feedContext == null)
            feedContext = new BaseContext(feedId,feedId);
        return feedContext;
    }
}
