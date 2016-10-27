package com.ebay.cip.framework.samples.payload;

import com.ebay.cip.framework.payload.Payload;

/**
 * Created by hachong on 3/24/2015.
 */
public class TradingApiPayload extends Payload {
    private TradingApiHeader header;
    private String apiName;
    private String entityName;
    private Long entitySequence;

    public TradingApiPayload(String data){
        super(data);
    }

    public TradingApiPayload(String data, String apiName){
        super(data);
        this.apiName = apiName;
    }

    public TradingApiPayload(TradingApiHeader header, String data, String apiName){
        super(data);
        this.header = header;
        this.apiName = apiName;
    }

    public TradingApiPayload(TradingApiHeader header, String data, String apiName, String entityName, Long entitySequence){
        super(data);
        this.header = header;
        this.apiName = apiName;
        this.entityName = entityName;
        this.entitySequence = entitySequence;
    }

    public TradingApiHeader getTradingApiHeader(){return header;}
    public void setTradingApiHeader(TradingApiHeader header){ this.header = header;}
    public void setApiName(String apiName){this.apiName = apiName;}
    public String getApiName(){return apiName;}

    public String getEntityName() {return entityName;}
    public void setEntityName(String entityName) {this.entityName = entityName;}

    public Long getEntitySequence() {return entitySequence;}
    public void setEntitySequence(Long entitySequence) {this.entitySequence = entitySequence;}
}
