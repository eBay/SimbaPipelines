package com.ebay.cip.framework.exception.test;

import com.ebay.cip.framework.exception.ProcessingException;
import com.ebay.kernel.exception.ErrorData;
import com.ebay.kernel.message.Message;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

import static org.junit.Assert.*;

/**
 * Created by jagmehta on 8/20/2015.
 */
public class ProcessingExceptionTest {

    public static final Exception e = new Exception("This is test");
    @Rule
    public MethodRule watchman = new TestWatchman() {
        public void starting(FrameworkMethod method) {
            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
        }
        public void finished(FrameworkMethod method) {
            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
        }
    };


    @Test
    public void testAll(){
        ErrorData data = new ErrorData("Test",new Message("mytest"));

        new ProcessingException();
        new ProcessingException(e.getMessage());
        new ProcessingException(e.getMessage(),data);
        new ProcessingException(e.getMessage(),data,false);
        new ProcessingException(e,data,e.getMessage(),false);
        new ProcessingException(e);
        new ProcessingException(new Throwable(e));
        new ProcessingException(e,e.getMessage(),false);
        new ProcessingException("Test",e.getMessage());
        ProcessingException p = new ProcessingException("Test",e.getMessage(),false);

        assertNotNull(p.getMessage());
        assertNotNull(p.getMethod());
        assertFalse(p.isRetry());

        p.setError(data);
        p.setRetry(true);
        p.setMethod("NewTest");
        p.setMessage("newtest");

        assertNotNull(p.getError());
    }

}
