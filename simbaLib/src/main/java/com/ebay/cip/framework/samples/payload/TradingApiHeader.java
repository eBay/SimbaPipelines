package com.ebay.cip.framework.samples.payload;

/**
 * Created by hachong on 1/13/2015.
 */
public class TradingApiHeader {

    private String version;
    private String siteId;

    public TradingApiHeader(){}

    public TradingApiHeader(String version, String siteId)
    {
        this.version = version;
        this.siteId = siteId;
    }

    public String getVersion()
    {
        return this.version;
    }
    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getSiteId()
    {
        return this.siteId;
    }
    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }

}