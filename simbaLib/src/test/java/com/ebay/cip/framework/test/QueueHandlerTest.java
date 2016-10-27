package com.ebay.cip.framework.test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.ebay.cip.TestModule;
import com.ebay.cip.framework.handler.QueueMessageHandler;
import com.ebay.cip.framework.job.JobMetadata;
import com.ebay.cip.framework.messages.CipMessage;
import com.ebay.cip.framework.messages.QueueMessage;
import com.ebay.cip.framework.samples.configBean.FrameworkConfigBean;
import com.ebay.cip.framework.samples.job.SplitterJob;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;
import com.ebay.es.cbdataaccess.exception.ProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

/**
 * Created by jagmehta on 10/27/2015.
 */
public class QueueHandlerTest {

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
    public static void init(){
    	System.out.println("\n\n==================================== Starting Test for class QueueHandlerTest====================================");
        TestModule.init();
    }
    

    @Test
    public void testQueueNameConfig(){
        QueueMessage message = new QueueMessage("CommonActor");
        QueueMessageHandler handler = QueueMessageHandler.getInstance();
        //If below method doesn't throw exception is enough. Return value comes from raptor config and may be null.
        handler.getQueueName(message);
    }

    @Test
    public void testQueueWithCreateQueue() throws InterruptedException, ExecutionException, ProcessingException {
        System.out.println("Starting test testQueueWithCreateQueue");
        QueueMessageHandler handler = QueueMessageHandler.getInstance();
        String queueName = UUID.randomUUID().toString();
        try {
            handler.createQueue(queueName);
            testQueueHandler(queueName, handler);
        }finally {
            deleteQueue(queueName);
        }
        System.out.println("Completed test testQueueWithCreateQueue");
    }

    @Test
    public void testQueueWithRecreateQueue() throws InterruptedException, ExecutionException, ProcessingException {
        System.out.println("Starting test testQueueWithRecreateQueue");
        QueueMessageHandler handler = QueueMessageHandler.getInstance();
        String queueName = UUID.randomUUID().toString();
        try {
            handler.reCreateQueue(queueName);
            testQueueHandler(queueName, handler);
        }finally {
            deleteQueue(queueName);
        }
        System.out.println("Completed test testQueueWithRecreateQueue");
    }


    private void testQueueHandler(String queueName, QueueMessageHandler handler){
        SplitterJob originalJob = new SplitterJob();
        JobMetadata metadata = new JobMetadata("123");
        originalJob.setJobMetadata(metadata);

        CipMessage message = new CipMessage(originalJob);

        assertTrue(handler.enqueue(queueName, message));

        CipMessage copyMessage = handler.dequeue(queueName);
        assertNotNull(copyMessage);

        assertEquals(originalJob.getJobId(), copyMessage.getJobId());
    }

    public void deleteQueue(String queueName) throws InterruptedException, ExecutionException, ProcessingException {
        if (!StringUtils.isEmpty(queueName)) {
            CouchBaseWrapperDAO wrapperDAO = CouchBaseWrapperDAO.getInstance(FrameworkConfigBean.getBean().getCouchBaseBucketName());
            wrapperDAO.deleteQueue(queueName);

            /** Workaround till WrapperDAO is fixed to delete head and tail synchroniously **/
            String qhead = queueName + "_Q:head";
            String qtail = queueName + "_Q:tail";
            try {
                wrapperDAO.deleteSync(qhead);
            } catch (Exception e) { /* this means we are good */}
            try {
                wrapperDAO.deleteSync(qtail);
            } catch (Exception e) { /* this means we are good */}
            /** End workaround **/
        }
    }
}
