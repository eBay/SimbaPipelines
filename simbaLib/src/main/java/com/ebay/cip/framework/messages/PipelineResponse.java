package com.ebay.cip.framework.messages;

import java.util.HashMap;
import java.util.Map;

import com.ebay.cip.framework.payload.Payload;
import com.ebay.cip.service.FeedResponseData;

public class PipelineResponse {
	String feedId;
	String corelationId;
	Exception exception;
	FeedResponseData feedResponseData;
	
	public PipelineResponse(String corelationid, String feedid, Exception ex, FeedResponseData data) {
		feedId = feedid;
		corelationId = corelationid;
		exception = ex;
		feedResponseData = data;
	}
	
	
    public void setException(Exception e){ exception = e;}
    public Exception getException() {return exception;}

    public String getCorelationId() { return this.corelationId; }
    public String getFeedId() { return this.feedId; }
    public String getContent() { 
    	if(feedResponseData != null){
    		Payload p = feedResponseData.getResponsePayload();
    		if(p != null){
    			return p.getData();
    		}
    	}
    	return null;
    }
    
    public String getContentType() {return feedResponseData.getContentType();}
    
    public Map<String, String> getHeaders() {
    	Map<String, String> headers = null;
    	if(feedResponseData != null) {
    		headers = feedResponseData.getHeaders();
    	}
    	return headers!=null?headers:new HashMap<String, String>(0);
    }

}
