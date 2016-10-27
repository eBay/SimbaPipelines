package com.ebay.cip.framework.test;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.LoggerFactory;

import scala.concurrent.Await;
import scala.concurrent.Future;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.util.Timeout;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.ebay.cip.Cip;
import com.ebay.cip.Initializer;
import com.ebay.cip.framework.configuration.JobConfiguration;
import com.ebay.cip.framework.configuration.JobConfigurationImpl;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.framework.job.ICipJob;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.messages.BaseMessage;
import com.ebay.cip.framework.messages.PipelineResponse;
import com.ebay.cip.framework.messages.PipelineRequest;
import com.ebay.cip.framework.samples.FrameworkJobTypeEnum;
import com.ebay.cip.framework.samples.Module;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.test.job.UnitTestParentJob;
import com.ebay.cip.service.FeedRequestData;
import com.ebay.cip.service.actor.OrchestratorActor;

/**
 * Created by jagmehta.
 */
public abstract class BasePipelineUnitTest {
    public static ActorSystem system;
    public static ActorRef parentActor;
    protected boolean completed = false;
    protected Throwable error;
    protected static JobConfiguration unitTestJobConfig = null;
    public static UnitTestParentJob parentJobCache = null;
    public static Map<String,String> header = null;
    public static String machineName;;
    public static Map<String,Job> jobMap = new ConcurrentHashMap<String, Job>();


    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };


    @BeforeClass
    public static void setupParent(){
        if(system != null){
            return;
        }
        FrameworkConfigBean.getBean().setCreateRaptorConfigBackup(false);
        OrchestratorActor.registerOrchestrator(TestOrchestratorActor.class);
        new Initializer().init();
        system = Cip.ACTOR_SYSTEM;
        try {
            machineName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        appInit();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        }
    }

    protected static void appInit() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.WARN);
       
        Module.init();
        root.setLevel(Level.INFO);


    }

    protected void beforeSetup(){}

    /*
        //@AfterClass
        public static void tearDown(){
            system.shutdown();
        }

    */
    @Before
    public void beforeTest() {
        header = new ConcurrentHashMap<>();
    }

    /**
     * Use this method to add header before calling {@link #startTest(String, String)} or {@link #startTest(String, String, String, String)} .
     * Previous Headers are cleared before any @Test method starts.
     * <B> For {@link #startTest(FeedRequestData)} , these headers will be ignored. </B>
     * @param key
     * @param value
     */
    public void addHeader(String key, String value){
        header.put(key,value);
    }

//    /**
//     * Call this method to create your pipeline. It internally takes care of most of the complexity.
//     * It also injects UniteTestParentJob dynamically into your pipeline in JVM for testing purpose.
//     * @param pipelineKey
//     * @param jobContext
//     * @param payload
//     * @throws Throwable
//     */
 /*   protected void startTest(String pipelineKey,JobContext jobContext,String payload) throws Throwable {

        //Add unitTestJob configuration to pipeline
        UnitTestParentJob parentJob = new UnitTestParentJob(this);
        parentJobCache = parentJob;
        Pipelines.getPipelineConfiguration(pipelineKey).addJobConfiguration(parentJob.getJobType(),unitTestJobConfig);

        parentJob.setJobContext(jobContext);
        jobContext.setPipelineKey(pipelineKey);
        jobContext.addHeader(START_TIME_MILLIS,String.valueOf(System.currentTimeMillis()));
        System.out.println("Starting Test:" + System.currentTimeMillis());
        Pipelines.create(pipelineKey, parentJob, jobContext, payload);
        while(!completed){
            try {
                Thread.sleep(1000);
//                printJobTree(parentJob,"");
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        if(error != null) {
            throw error;
        }
    }
*/


//    /**
//     * Call this method to create your pipeline. It internally takes care of most of the complexity.
//     * It also injects UniteTestParentJob dynamically into your pipeline in JVM for testing purpose.
//     * This is the core method to be called. See also other variants of startTest methods whith
//     *  @param data  FeedRequestData object. Fill any details you may need.
//     * @throws Throwable
//     */
//    protected void startTest(FeedRequestData data) throws Throwable {
//        //if the startTest called multiple times, it should clean up the completed and error variables.
//        completed = false;
//        error = null;
//        //Add unitTestJob configuration to pipeline
//        UnitTestParentJob parentJob = new UnitTestParentJob(this);
//        parentJobCache = parentJob;
//        Pipelines.getPipelineConfiguration(data.getPipelineKey()).addJobConfiguration(parentJob.getJobType(), unitTestJobConfig);
//
//        System.err.println("Starting Test: " + data.getPipelineKey());
//        long time = System.currentTimeMillis();
//        Feed feed = FeedFactory.start(data,null);
//        Pipelines.create(feed);
//        while(!completed){
//            try {
//                Thread.sleep(1000);
////                printJobTree(parentJob,"");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                FeedFactory.fail(feed,e.getMessage());
//                throw new RuntimeException(e);
//            }
//        }
//        if(error != null) {
//            FeedFactory.fail(feed,error.getMessage());
//            throw error;
//        }
//        FeedFactory.done(feed);
//        System.err.println("Ending Test: " + data.getPipelineKey());
//        time = System.currentTimeMillis()-time;
//        System.err.println("Test taken: "+time+" milliseconds");
//
//    }
    
    
    /**
     * Call this method to create your pipeline. It internally takes care of most of the complexity.
     * It also injects UniteTestParentJob dynamically into your pipeline in JVM for testing purpose.
     * This is the core method to be called. See also other variants of startTest methods whith
     *  @param data  FeedRequestData object. Fill any details you may need.
     * @throws Throwable
     */
    protected void startTest(FeedRequestData data) throws Throwable {
    	long time = System.currentTimeMillis();

        System.err.println("Starting Test: " + data.getPipelineKey());
        //if the startTest called multiple times, it should clean up the completed and error variables.
    	PipelineRequest pipelineStartMesssage = constructPipelineStartMessage(data);
    	
    	Timeout t = new Timeout(600, TimeUnit.SECONDS);
		
		PipelineResponse completeMessage = null;
		try {
			Future<Object> futureResponse = Patterns.ask(system.actorSelection("/user/HttpParentActor/orchestratorRouter"), pipelineStartMesssage, t);
			//response = (String)
			completeMessage = (PipelineResponse)Await.result(futureResponse, t.duration());
			if (completeMessage.getException() != null) {
				throw completeMessage.getException();
			}
			Job job = jobMap.get(completeMessage.getFeedId());
			onPipelineCompleteInternal(null, job);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Test fail!");
			throw e;
		}
		//System.out.println("Response:\n" + response);
		//System.err.println("Test pass!");
        System.err.println("Ending Test: " + data.getPipelineKey());
        time = System.currentTimeMillis()-time;
        System.err.println("Test taken: "+time+" milliseconds");
    }
    

    private PipelineRequest constructPipelineStartMessage(
			FeedRequestData data) {
    	
    	Map<String, List<String>> headerListMap = new HashMap<String, List<String>>();
    	Map<String,String> headerMap = data.getHeadersMap();
    	
    	for(Map.Entry<String, String> entry:headerMap.entrySet()) {
    		ArrayList<String> list = new ArrayList<>(1);
    		list.add(entry.getValue());
    		headerListMap.put(entry.getKey(), list);
    	}
    	
    	List<String> userName = new ArrayList<String>();
    	if(!headerListMap.containsKey("X-EBAY-USER-NAME")){
    		userName.add("dummyName");
    		headerListMap.put("X-EBAY-USER-NAME", userName);
    	}
    	
    	return new PipelineRequest(headerListMap, data.getPipelineKey(), data.getRequestPayload().getData());
	}

	/**
     * This method is an convenient way of create pipeline if you don't need user,correlation id and payload.
     * It is equivalent of calling {@link #startTest(FeedRequestData)}  } with FeedRequestData having user as null,auto generated co-relation id and payload data is null
     * @param pipelineKey
     * @throws Throwable
     */
    protected void startTest(String pipelineKey) throws Throwable {
        startTest(getFeedRequestData(pipelineKey));
    }

    /**
     * This method is an convenient way of create pipeline if you dont need user and correlation id.
     * It is equivalent of calling {@link #startTest(FeedRequestData)}  } with FeedRequestData having user as null and auto generated co-relation id
     * @param pipelineKey
     * @param payload
     * @throws Throwable
     */
    protected void startTest(String pipelineKey,String payload) throws Throwable {
        startTest(getFeedRequestData(pipelineKey, payload));
    }

    /**
     * This method is an convenient way of create pipeline if you dont want to create FeedRequestData.
     * It is equivalent of calling {@link #startTest(FeedRequestData)}  } with FeedRequestData having user as null and auto generated co-relation id
     * @param pipelineKey
     * @param payload
     * @param corelationId
     * @param userName
     * @throws Throwable
     * @Deprecated  Please create using {@link #startTest(FeedRequestData)} or {@link #startTest(String, String)}
     */
    protected void startTest(String pipelineKey,String payload, String corelationId, String userName) throws Throwable {
        startTest(getFeedRequestData(pipelineKey,payload,corelationId,userName));
    }


    /**
     * Utility method to create FeedRequestData object
     * @param pipelineKey
     * @param payload
     * @param corelationId
     * @param userName
     * @return
     */
    protected FeedRequestData getFeedRequestData(String pipelineKey,String payload, String corelationId, String userName){
        FeedRequestData data;
        data = new FeedRequestData(header,pipelineKey,corelationId,true,userName,payload);
        return data;
    }

    /**
     * Utility method to create FeedRequestData object. User will be null. Corelation id will be auto-generated as UUID.
     * @param pipelineKey
     * @param payload
     * @return
     */
    protected FeedRequestData getFeedRequestData(String pipelineKey,String payload){
        FeedRequestData data;
        String corelationId = header.get("X-EBAY-CORRELATION-ID");
        corelationId = corelationId==null?UUID.randomUUID().toString():corelationId;
        data = new FeedRequestData(header,pipelineKey, corelationId,true,null,payload);
        return data;
    }

    protected FeedRequestData getFeedRequestData(String pipelineKey){
        FeedRequestData data;
        data = new FeedRequestData(header, pipelineKey, UUID.randomUUID().toString(), true, null, "");
        return data;
    }

/*
    public static void printJobTree(ICipJob rootJob,String tab) {
        try {
            tab = tab!=null?tab:"";
            System.out.println(tab+"|-"+rootJob.getJobId()+" -> "+rootJob.getJobType().getName());
            Map<String, ICipJob> forkedJobs = rootJob.getForkedJobs();
            if(forkedJobs != null){
                for(ICipJob child:forkedJobs.values()) {
                    printJobTree(child,tab+"\t");
                }
            }

            if(rootJob.getNextJob()!=null){
                printJobTree(rootJob.getNextJob(),tab);
            }

        }catch(Exception e) {
            System.err.println(tab+"|-"+e.getMessage());
        }
    }
*/
    /**
     * This method will be called once pipeline is completed.
     * <li>UnitTestParentJob is a wrapper job created while invoking your pipeline. It will containe usefull information you can use.</li>
     * <li>lastExecutedPipelineJob is the job pipeline executed last. You may have some data there which you may want to check.</li>
     * <li>All along your jobContext is still active. You can inspect any data there</li>
     * <li>BasePipelineUnitTest will automatically add START_TIME_MILLIS and END_TIME_MILLIS in jobContext for your reference</li>
     * @param parentJob
     * @param lastExecutedPipelineJob
     * @throws Throwable 
     */
    abstract public void onPipelineCompleteInternal(UnitTestParentJob parentJob, ICipJob lastExecutedPipelineJob) throws Throwable;

}


class TestOrchestratorActor extends OrchestratorActor {

    public TestOrchestratorActor() throws ClassNotFoundException {
        System.out.println(getSelf().path().toString());
        
        try {
	        //init FeedFactory first
	        Class.forName("com.ebay.cip.framework.feed.FeedFactory");
	        //And now FeedFactoryExtended
	        Class.forName("com.ebay.cip.framework.test.FeedFactoryExtended");
        }catch(Exception e){
        	e.printStackTrace();
        }
    }
    //    UnitTestParentJob job = null;
    @Override
    public void onReceive(Object message) throws Exception {
         if(message instanceof BaseMessage){
        	BaseMessage msg = (BaseMessage)message;
            Job job = ((BaseMessage) message).getJob();
            BasePipelineUnitTest.jobMap.put(msg.getFeedId(), job);
        }
        super.onReceive(message);
    }
}

class FeedFactoryExtended extends FeedFactory {
	
	static {
		feedsMap = new TestConcurrentHashMap<>();
	}
}

class TestConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
	
	@Override
    public V remove(Object key) {
		//ignore
		//System.out.println("ignore remove");
		return get(key);
    }
	@Override
    public boolean remove(Object key, Object value) {
		//ignore
		return true;
    }
}