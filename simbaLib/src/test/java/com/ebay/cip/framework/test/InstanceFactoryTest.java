package com.ebay.cip.framework.test;


import com.ebay.cip.TestModule;
import com.ebay.cip.framework.InstanceFactoryWithChangableCouchbaseDAO;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.job.Job;
import com.ebay.cip.framework.test.job.TestJob;
import com.ebay.cip.framework.util.InstanceFactory;
import com.ebay.es.cbdataaccess.CouchBaseWrapperDAO;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
/**
 * Created by hachong on 10/26/2015.
 */
public class InstanceFactoryTest {

    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };

    InstanceFactory instanceFactory;

    @BeforeClass
    public static void setup(){
    	System.out.println("\n\n==================================== Starting Test for class InstanceFactoryTest====================================");
        TestModule.init();
    }

    @Before
    /**
     * Make sure that default is non db backed always.
     */
    public void beforeEachTest() {
        instanceFactory = new InstanceFactory(true);
        instanceFactory.setFirstJobId("0");
    }

    @Test
    public void testSaveInstanceInDB() throws InstanceProcessingException {
        TestJob job = new TestJob();
        String key = "test";
        instanceFactory.saveObjectInstance(key, job);
        TestJob testJob = instanceFactory.getObjectInstance(key,Job.class);
        boolean isSame = job.equals(testJob);
        assertTrue(isSame);
    }

    @Test
    public void testSaveInstanceInMemory() throws InstanceProcessingException {
        instanceFactory = new InstanceFactory(false);
        Job job = new TestJob();
        String key = "test";
        instanceFactory.saveObjectInstance(key,job);
        TestJob testJob = instanceFactory.getObjectInstance(key,Job.class);
        boolean isSame = job.equals(testJob);
        assertTrue(isSame);
    }

    @Test
    public void testAppendSync() throws Exception{
        String key = UUID.randomUUID().toString();
        String data = "this is my data";
        InstanceFactory factory = new InstanceFactory(true);
        // first -ve test
        Boolean ret = factory.appendSync(key, Job.class, data, false);
        assertFalse(ret);

        ret = factory.appendSync(key, Job.class, data, true);
        assertTrue(ret);

        ret = factory.appendSync(key, Job.class, data, false);
        assertTrue(ret);

        String returnedData = (String) factory.getRawData(key, Job.class);
        assertEquals(returnedData, data + data);
    }
    @Test
    public void testAppendAsync() throws Exception{
        String key = UUID.randomUUID().toString();
        String data = "this is my data";
        InstanceFactory factory = new InstanceFactory(true);
        // first -ve test
        Future<Boolean> ret = factory.appendAsync(key, Job.class, null, false);
        assertFalse(ret.get(100, TimeUnit.SECONDS));
        assertTrue(ret.isDone());
        assertFalse(ret.isCancelled());
        assertFalse(ret.cancel(false));

        ret = factory.appendAsync(key, Job.class, data, true);
        assertTrue(ret.get());

        ret = factory.appendAsync(key, Job.class, data, false);
        assertTrue(ret.get());

        String returnedData = (String) factory.getRawData(key, Job.class);
        assertEquals(returnedData, data+data);
    }

    @Test
    public void testSaveInstanceWithDBExcpetion() throws InstanceProcessingException {
        InstanceFactoryWithChangableCouchbaseDAO factory = new InstanceFactoryWithChangableCouchbaseDAO(true);
        try {
            factory.setCouchbaseWrapperDAO(CouchBaseWrapperDAO.getInstance("junk"));
            TestJob job = new TestJob();
            factory.setFirstJobId(job.getJobId());
            String key = "test";
            factory.saveObjectInstance(key, job);
            TestJob testJob = factory.getObjectInstance(key, Job.class);
            boolean isSame = job.equals(testJob);
            assertTrue(isSame);
        }finally {
            factory.resetToOriginal();
        }
    }

    @Test
    public void testGetJson() throws Exception {
        assertNull(instanceFactory.getFeedJSONObject("junk", Feed.class));
        InstanceFactoryWithChangableCouchbaseDAO factory = new InstanceFactoryWithChangableCouchbaseDAO(true);
        try {
            factory.setCouchbaseWrapperDAO(CouchBaseWrapperDAO.getInstance("junk"));
            assertNull(factory.getFeedJSONObject("junk", Feed.class));
        }finally {
            factory.resetToOriginal();
        }

    }

}
