//package com.ebay.cip.server;
//
////import com.ebay.cip.util.RestServiceClient;
//import com.ebay.cip.util.BasicRestServiceClient;
//import com.ebay.kernel.util.Base64;
//import com.ebay.kernel.util.FileUtils;
//import org.junit.Test;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Map;
//import java.util.UUID;
//
//import static org.junit.Assert.assertNotNull;
//
///**
// * Created by jagmehta on 1/15/2016.
// */
//public class SimbaServerTest extends BaseServerTest {
//
//
//    @Test
//    public void defaultMaxPipelineTest() throws Exception {
//        try {
//            Map<String,String> header = getDefaultHeaderMap("mip_bp_seller1", -1);
//            String response = BasicRestServiceClient.post(getFullURL("test/forkJoinPipelineTest"),getMaxPipelineDefaultPayload(),header);
//            System.out.println(response);
//            assertNotNull(response);
//        }catch(IOException e){
//            e.printStackTrace();
//            //somehow CI always throws this exception. So catching right now.
//        }
//
//    }
//
//    @Test
//    public void defaultLMSPipelineTest() throws Exception {
//        try {
//            Map<String, String> header = getDefaultHeaderMap("mip_bp_seller1", -1);
//            String response = BasicRestServiceClient.post(getFullURL("example/product"), getDefaultLMSPayload(), header);
//            System.out.println(response);
//            assertNotNull(response);
//        }catch(IOException e){
//            e.printStackTrace();
//            //somehow CI always throws this exception. So catching right now.
//        }
//    }
//
//    @Test
//    public void asyncMaxPipelineTest() throws Exception {
//        Map<String,String> header = getDefaultHeaderMap("mip_bp_seller1", -1, UUID.randomUUID().toString(),ProcessingType.ASYNC);
//        String response = BasicRestServiceClient.post(getFullURL("test/forkJoinPipelineTest"),getMaxPipelineDefaultPayload(),header);
//        System.out.println(response);
//        assertNotNull(response);
//    }
//
//
//    @Test
//    public void unknownPipelineTest() throws Exception {
//        Map<String,String> header = getDefaultHeaderMap("mip_bp_seller1", -1);
//        Exception ex = null;
//        try {
//            String response = BasicRestServiceClient.post(getFullURL("someJunk"), getMaxPipelineDefaultPayload(), header);
//            System.out.println(response);
//        }catch(FileNotFoundException e){
//            //this means it passed.
//            ex = e;
//        }
//        assertNotNull(ex);
//    }
//
//    @Test
//    public void noUserNameTest() throws Exception {
//        try {
//            Map<String, String> header = getDefaultHeaderMap("mip_bp_seller1", -1);
//            header.remove("X-EBAY-USER-NAME");
//            String response = BasicRestServiceClient.post(getFullURL("test/forkJoinPipelineTest"), getMaxPipelineDefaultPayload(), header);
//            System.out.println(response);
//            assertNotNull(response);
//        }catch(IOException e){
//            e.printStackTrace();
//            //somehow CI always throws this exception. So catching right now.
//        }
//    }
//
//
//    private String getDefaultLMSPayload() throws IOException {
//        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("ExampleLMSPayload.xml");
//        String content = FileUtils.readStream(stream);
//        return content;
//    }
//    protected String getMaxPipelineDefaultPayload(){
//        String data[] = {
//                "1,2,3,100"
//                ,"1,2,3,100"
//                ,"1,2,3"
//        };
//        StringBuilder builder = new StringBuilder();
//        String nl = System.getProperty("line.separator");
//        for(String s:data){
//            String enc = Base64.encode(s.getBytes());
//            builder.append(enc);
//            builder.append(nl);
//        }
//        return builder.toString();
//    }
//}
