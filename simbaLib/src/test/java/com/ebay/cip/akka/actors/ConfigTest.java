//package com.ebay.cip.akka.actors;
//
//import com.ebay.es.cipconfig.cipconfig.common.ConfigConstants;
//import com.ebay.es.cipconfig.cipconfig.core.ConfigContext;
//import com.ebay.kernel.calwrapper.CalEventHelper;
//import org.junit.AfterClass;
//import org.junit.Assert;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
///**
// * Created by kmanekar on 4/6/2015.
// */
//public class ConfigTest {
//
//    @BeforeClass
//    public static void setup() {}
//
//    @AfterClass
//    public static void tearDown() {}
//
//
//   @Test
//   public void testPipelineConfig() {
//
//        String mipProductPipeline = null;
//
//        /**** KEYS ******/
//        String MIP_PRODUCT_PIPELINE_KEY = "MIP-PRODUCT";
//
//        try{
//            mipProductPipeline = ConfigContext.getString(ConfigConstants.PIPELINE_CONFIG_ID,MIP_PRODUCT_PIPELINE_KEY);
//
//            System.out.println(mipProductPipeline);
//            Assert.assertNotNull(mipProductPipeline);
//
//
//        }catch(Throwable th){
//            String msg = "Parameters are not available: Error="+th.getMessage();
//            CalEventHelper.writeException(".getConfig", th, msg);
//        }
//
//    }
//
//    @Test
//    public void testConfig() {
//
//        boolean cip = false;
//
//
//        try{
////            cip = ConfigContext.getBoolean(ConfigConstants.APPLICATION_CONFIG_ID,"cipAuthentication");
//
//            System.out.println(cip);
//
//
//        }catch(Throwable th){
//            String msg = "Parameters are not available: Error="+th.getMessage();
//            CalEventHelper.writeException(".getConfig", th, msg);
//        }
//
//    }
//}
