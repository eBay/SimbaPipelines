package com.ebay.cip.framework;

import com.ebay.cip.framework.exception.FeedNotFoundException;
import com.ebay.cip.framework.exception.InstanceProcessingException;
import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.service.FeedRequestData;
import com.ebay.cip.service.FeedResponseData;
import com.ebay.kernel.calwrapper.CalEventHelper;

import java.io.FileNotFoundException;


/**
 * Created by hachong on 8/24/2015.
 */
public class BaseContext implements FeedContext,JobContext {
    protected String id;
    private String feedId;

    public BaseContext(String id, String feedId){
        this.id = id;
        this.feedId = feedId;
    }

    /**
     * Adds data to dataContainer. No key manipulation is done as each subpipeline will have its own JobContext.
     * Warning: There is no duplicate key check here. So if business logic overwrites value, it will allow it.
     * @param key
     * @param value
     */
    //Todo Throw FeedNotFoundException
    public void put(Object key, Object value) {
        String modifyKey = constructDataMapKey(key.toString());
        try {
            FeedFactory.getFeed(feedId).getInstanceFactory().saveObjectInstance(modifyKey, value);
        } catch (InstanceProcessingException e) {
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,"Unable to save context's object. Object class name: "+value.getClass().getName());
        } catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
        }
    }

    /**
     * Retrieve data stored via put method. Note that data put by subpipeline is not visible or available to parent pipeline or another subpipeline.
     * @param key
     * @return data or null
     */
    public Object get(Object key) {
        String modifyKey = constructDataMapKey(key.toString());

        try {
            Feed feed = FeedFactory.getFeed(feedId);
            return feed.getInstanceFactory().getObjectInstance(modifyKey, null);
        }
        catch (InstanceProcessingException | FeedNotFoundException e) {
            e.printStackTrace();
            if(e instanceof FeedNotFoundException){
                CalEventHelper.writeException("Framework",e,true,e.getMessage());
            }
            CalEventHelper.writeException("Framework",e,true,"Unable to get context's object. Key: "+key);
        }
        return null;
    }


    // implementation of FeedContext interface
    // Todo: throw FeedNotFoundException;
    public FeedRequestData getFeedRequestData(){
        try{
            return FeedFactory.getFeed(feedId).getFeedRequestData();
        }catch(FeedNotFoundException e){
            e.printStackTrace();
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return null;
        }
    }

    //Todo: throw FeedNotFoundException
    public FeedResponseData getFeedResponseData(){
        try {
             return FeedFactory.getFeed(feedId).getFeedResponseData();
        }
        catch(FeedNotFoundException e){
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return null;
        }
    }

    //Todo: throw FeedNotFoundException
    public String getPipelineKey() {
        try {
            return FeedFactory.getFeed(feedId).getFeedRequestData().getPipelineKey();
        }
        catch(FeedNotFoundException e){
            CalEventHelper.writeException("Framework",e,true,e.getMessage());
            return null;
        }
    }
    public String getFeedId(){return feedId;}
    public String getPipelineId(){return id;}

    protected String constructDataMapKey(String key){
        StringBuilder builder = new StringBuilder();
        builder.append(id);
        builder.append("::");
        builder.append(key);
        return builder.toString();
    }

}
