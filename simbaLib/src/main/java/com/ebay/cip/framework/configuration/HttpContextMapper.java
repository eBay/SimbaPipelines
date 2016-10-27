package com.ebay.cip.framework.configuration;

import java.util.concurrent.ConcurrentHashMap;

import com.ebay.raptor.kernel.context.IRaptorContext;
import com.ebay.raptor.kernel.context.impl.RaptorContext;

/**
 * Created by hachong on 4/2/2015.
 */
public class HttpContextMapper {

    ConcurrentHashMap<String, RaptorContext> httpContextObjectMap = new ConcurrentHashMap<String, RaptorContext>();

    public static HttpContextMapper INSTANCE = new HttpContextMapper();
    private HttpContextMapper(){
        System.out.println("initializing HttpContextMapper");
    }

    public void putHttpContext(String feedId, RaptorContext requestContext){
        httpContextObjectMap.put(feedId, requestContext);
    }
    public RaptorContext getHttpContext(String feedId){
        return httpContextObjectMap.remove(feedId);
    }
}
