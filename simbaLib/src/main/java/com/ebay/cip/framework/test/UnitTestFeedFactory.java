package com.ebay.cip.framework.test;

import com.ebay.cip.framework.feed.Feed;
import com.ebay.cip.framework.feed.FeedFactory;
import com.ebay.cip.service.FeedRequestData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hachong on 8/27/2015.
 */
public class UnitTestFeedFactory extends FeedFactory {

    /**
     * This method will create a feed without providing CompletePipelineActor.
     * This is use to create feed for JUnit test.
     * @param requestData
     * @return
     */
    public static Feed UnitTestStart(FeedRequestData requestData){

        //create feedId
        String feedId = UUID.randomUUID().toString();

        Feed feed = new Feed(feedId,requestData);
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        feed.setStartTime(date);
        feedsMap.put(feedId, feed);

        return feed;

    }

}
