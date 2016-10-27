//package com.ebay.cip.server;
//
//import org.junit.BeforeClass;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.MethodRule;
//import org.junit.rules.TestWatchman;
//import org.junit.runners.model.FrameworkMethod;
//import org.squbs.unicomplex.Bootstrap;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * Created by jagmehta on 1/15/2016.
// */
//public class BaseServerTest {
//
//    public enum ProcessingType {SYNC, ASYNC}
//
//
//    @Rule
//    public MethodRule watchman = new TestWatchman() {
//        public void starting(FrameworkMethod method) {
//            System.out.println("\n\n==================================== Starting test: " + method.getName()+"====================================");
//        }
//        public void finished(FrameworkMethod method) {
//            System.out.println("\n==================================== Ending test: " + method.getName()+"====================================");
//        }
//    };
//
//    @BeforeClass
//    public static void startServer(){
//        String[] argv = new String[0];
//        Bootstrap.main(argv);
//    }
//
//    protected String getFullURL(String uri){
//        uri = uri.startsWith("/")?uri:"/"+uri;
//        return "http://localhost:8080/cip"+uri;
//    }
//
//    protected Map<String,String> getDefaultHeaderMap(String userName, long cipUserId, String corelationId,ProcessingType processingType){
//        Map<String,String> headers = new HashMap<>();
//        headers.put("X-EBAY-USER-NAME",userName);
//        headers.put("X-EBAY-MIP-USER-ID",String.valueOf(cipUserId));
//        headers.put("X-EBAY-CORRELATION-ID",corelationId);
//        headers.put("X-EBAY-PROCESSING-TYPE", processingType.name());
//        return headers;
//    }
//    protected Map<String,String> getDefaultHeaderMap(String userName, long cipUserId, String corelationId) {
//        return getDefaultHeaderMap(userName,cipUserId,corelationId, ProcessingType.SYNC);
//    }
//    protected Map<String,String> getDefaultHeaderMap(String userName, long cipUserId) {
//        return getDefaultHeaderMap(userName,cipUserId, UUID.randomUUID().toString(), ProcessingType.SYNC);
//    }
//
//}
