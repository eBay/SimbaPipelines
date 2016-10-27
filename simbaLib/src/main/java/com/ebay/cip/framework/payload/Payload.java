package com.ebay.cip.framework.payload;

/**
 * Created by hachong on 4/3/2015.
 */

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Payload {
    protected String data;
    protected String refId;
   

    public Payload(){}

    public Payload(String data){
        this.data = data;
    }
    
   
    
    @JsonIgnore
    public InputStream getInputStream(){
    	return new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
    }

    public void setData(String data){
        this.data = data;
    }
    public String getData(){
        return data;
    }
    
    public String getRefId(){
        return refId;
    }
    public void setRefId(String refId){ this.refId = refId;}
    
    
    
}
